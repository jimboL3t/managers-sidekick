// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import com.google.gson.Gson;
import java.util.*;
import static gr.sidekick.Model.*;

/** Team operations never share mutable objects between working copies. */
public final class TeamService {
    private TeamService() {}

    public static Team create(Data data,String name) {
        Team team=new Team(requireName(name));
        Data defaults=new Data();team.leaveTypes.putAll(defaults.leaves);team.builtInLeaves.putAll(defaults.builtInLeaves);
        team.allowedLeaves.addAll(team.leaveTypes.keySet());
        data.teams.add(team);
        return team;
    }

    public static Team duplicate(Data data,Team source,String name) {
        if(!data.teams.contains(source))throw new IllegalArgumentException(I18n.text("Η ομάδα δεν υπάρχει."));
        Gson gson=new Gson();
        Team copy=gson.fromJson(gson.toJson(source),Team.class);
        copy.id=id();copy.name=requireName(name);copy.sandbox=true;
        copy.leaveTypes=new LinkedHashMap<>(leaves(data,source));
        if(source.leaveTypes==null||source.leaveTypes.isEmpty())copy.builtInLeaves=new LinkedHashMap<>(data.builtInLeaves);
        Map<String,String> posts=new HashMap<>(),employees=new HashMap<>();
        for(Post p:copy.posts){String old=p.id;p.id=id();posts.put(old,p.id);}
        for(Employee e:copy.employees){
            String old=e.id;e.id=id();employees.put(old,e.id);
            Set<String> skills=new LinkedHashSet<>();
            for(String skill:e.skills)skills.add(posts.getOrDefault(skill,skill));
            e.skills=skills;
        }
        for(Model.Month month:copy.months.values()){
            Map<String,Cell> cells=new LinkedHashMap<>();
            month.cells.forEach((key,cell)->{
                int separator=key.lastIndexOf(':');
                String employee=key.substring(0,separator);
                cell.value=posts.getOrDefault(cell.value,cell.value);
                cells.put(employees.getOrDefault(employee,employee)+key.substring(separator),cell);
            });
            month.cells=cells;
            if(month.workPlans!=null){
                Map<String,WorkPlan> plans=new LinkedHashMap<>();
                month.workPlans.forEach((employee,plan)->plans.put(employees.getOrDefault(employee,employee),plan));
                month.workPlans=plans;
            }
        }
        data.teams.add(copy);
        return copy;
    }

    public static boolean delete(Data data,Team team){return data.teams.remove(team);}

    private static String requireName(String name){
        if(name==null||name.isBlank())throw new IllegalArgumentException(I18n.text("Συμπληρώστε όνομα ομάδας."));
        return name.strip();
    }
}
