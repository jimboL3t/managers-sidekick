// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class OperatingDaysTest {
 @Test void defaultsAndOldFilesOperateDaily(){
  Post p=new Gson().fromJson("{\"name\":\"Old\",\"start\":\"08:00\",\"end\":\"16:00\",\"demand\":[1,1,1,1,1,1,1]}",Post.class);
  for(int i=1;i<=7;i++){assertTrue(p.operates(LocalDate.of(2026,9,i)));assertEquals(1,p.required(LocalDate.of(2026,9,i)));}
 }
 @Test void generationAndCoverageRespectOperatingDaysAndKeepManualOverrides(){
  Data data=new Data();Team t=TeamService.create(data,"Team");Post p=new Post("Monday Wednesday","08:00","16:00");p.operatingDays=new boolean[]{true,false,true,false,false,false,false};t.posts.add(p);
  Employee e=new Employee("Person");e.skills.add(p.id);t.employees.add(e);YearMonth m=YearMonth.of(2026,9);
  t.month(m).cells.put(key(e.id,1),new Cell(p.id,true));
  List<String> issues=new Scheduler().generate(data,t,m);
  assertTrue(t.cell(e.id,m.atDay(1)).locked);assertEquals(p.id,t.cell(e.id,m.atDay(1)).value);
  for(int d=2;d<=30;d++){Cell c=t.cell(e.id,m.atDay(d));if(c!=null&&p.id.equals(c.value))assertTrue(p.operates(m.atDay(d)));}
  assertTrue(CalendarRules.uncoveredDays(t,m).isEmpty());
  assertTrue(issues.stream().anyMatch(s->s.contains("εκτός ημερών λειτουργίας")));
  Team copy=TeamService.duplicate(data,t,"Copy");assertArrayEquals(p.operatingDays,copy.posts.getFirst().operatingDays);copy.posts.getFirst().operatingDays[0]=false;assertTrue(p.operatingDays[0]);
 }
 @Test void closedDutyNeverReceivesSupplementaryShifts(){
  Data data=new Data();Team t=TeamService.create(data,"Team");Post p=new Post("Closed","08:00","16:00");Arrays.fill(p.operatingDays,false);t.posts.add(p);
  Employee e=new Employee("Person");e.skills.add(p.id);t.employees.add(e);YearMonth m=YearMonth.of(2026,9);new Scheduler().generate(data,t,m);
  assertEquals(0,Scheduler.workCount(t,e,m));assertTrue(CalendarRules.uncoveredDays(t,m).isEmpty());
 }
}
