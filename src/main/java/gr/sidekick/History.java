// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;
import com.google.gson.*;
import java.time.*;
import static gr.sidekick.Model.*;
/** Immutable serialized monthly snapshots include the rules and names in force. */
public final class History {
 private static final Gson GSON=new Gson();
 private History(){}
 public static Revision capture(Team team,YearMonth month,String note){
  JsonObject tree=GSON.toJsonTree(team).getAsJsonObject();tree.add("history",new JsonArray());
  JsonObject months=new JsonObject();months.add(month.toString(),GSON.toJsonTree(team.month(month)));tree.add("months",months);
  Revision revision=new Revision();revision.created=Instant.now().toString();revision.month=month.toString();revision.note=note;revision.snapshot=tree.toString();team.history.add(revision);return revision;
 }
 public static Team read(Revision revision){return GSON.fromJson(revision.snapshot,Team.class);}
 public static Team restoreCopy(Data data,Revision revision,String name){
  Team frozen=read(revision);data.teams.add(frozen);
  try{return TeamService.duplicate(data,frozen,name);}finally{data.teams.remove(frozen);}
 }
}
