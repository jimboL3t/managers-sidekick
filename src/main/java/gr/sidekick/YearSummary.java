// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;

/** Read-only totals of saved assignments, not a record of actual attendance. */
public final class YearSummary {
    public record Row(String name,int shifts,int rest,int leave,Map<String,Integer> categories) {}
    public record Result(List<Row> rows,List<String> categories,int months,int shifts,int rest,int leave) {}
    private YearSummary() {}
    public static Result calculate(Data data,Team team,int year){
        List<String> categories=new ArrayList<>();team.posts.forEach(p->categories.add(p.id));leaves(data,team).keySet().stream().filter(id->!OFF.equals(id)).forEach(categories::add);
        List<Row> rows=new ArrayList<>();Set<Integer> recordedMonths=new HashSet<>();int allShifts=0,allRest=0,allLeave=0;
        for(Employee employee:team.employees){
            Map<String,Integer> counts=new LinkedHashMap<>();int shifts=0,rest=0,leave=0;
            for(int month=1;month<=12;month++){
                YearMonth ym=YearMonth.of(year,month);
                for(int day=1;day<=ym.lengthOfMonth();day++){
                    Cell c=team.cell(employee.id,ym.atDay(day));if(c==null)continue;
                    recordedMonths.add(month);counts.merge(c.value,1,Integer::sum);
                    if(!OFF.equals(c.value)&&!categories.contains(c.value))categories.add(c.value);
                    if(team.post(c.value)!=null)shifts++;else if(OFF.equals(c.value))rest++;else leave++;
                }
            }
            rows.add(new Row(employee.name,shifts,rest,leave,Map.copyOf(counts)));allShifts+=shifts;allRest+=rest;allLeave+=leave;
        }
        return new Result(List.copyOf(rows),List.copyOf(categories),recordedMonths.size(),allShifts,allRest,allLeave);
    }
}
