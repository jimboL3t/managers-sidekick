// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;

/** Groups parallel duties; compares participating regular staff, never reserve totals. */
public final class ShiftBalance {
    private ShiftBalance() {}
    public static int band(Post p) { return p.balanceBand>=1&&p.balanceBand<=3?p.balanceBand-1:HistoryPatterns.band(p); }
    public static final class Context {
        private final Team team;
        private final YearMonth month;
        private final Map<String,double[]> capacity=new LinkedHashMap<>();
        private final Map<String,int[]> previous=new LinkedHashMap<>();
        public Context(Team team,YearMonth month) {
            this.team=team;this.month=month;
            for(Employee e:team.employees) {
                if(e.excludeShiftBalance||Workload.reserve(team,e,month))continue;
                double[] cap=new double[3];int[] past=new int[3];
                for(int back=0;back<=Math.max(0,Math.min(12,team.fairnessLookback));back++) {
                    YearMonth m=month.minusMonths(back);
                    Model.Month schedule=team.months.get(m.toString());
                    if(back>0&&(schedule==null||schedule.cells.isEmpty()||Workload.reserve(team,e,m)))continue;
                    int[] opportunities=new int[3];
                    for(int d=1;d<=m.lengthOfMonth();d++) {
                        LocalDate date=m.atDay(d);Cell cell=team.cell(e.id,date);
                        Post assigned=cell==null?null:team.post(cell.value);
                        if(back>0&&assigned!=null)past[band(assigned)]++;
                        // Ignore old automatic current-month assignments: recalculation clears them.
                        boolean fixed=cell!=null&&(back>0||cell.locked||schedule.lockedDays.contains(d));
                        if(fixed&&assigned==null)continue;
                        if(back==0&&schedule!=null&&schedule.lockedDays.contains(d)&&cell==null)continue;
                        boolean[] possible=new boolean[3];
                        for(Post p:team.posts)if(p.operates(date)&&e.skills.contains(p.id)&&Availability.allows(e,p,date)&&Fairness.allowed(e,p,date))possible[band(p)]=true;
                        for(int b=0;b<3;b++)if(possible[b])opportunities[b]++;
                    }
                    for(int b=0;b<3;b++)cap[b]+=Math.min(opportunities[b],Workload.target(team,e,m));
                }
                capacity.put(e.id,cap);previous.put(e.id,past);
            }
        }
        public boolean participates(Employee e){return capacity.containsKey(e.id);}
        public int[] current(Employee e){
            int[] counts=new int[3];for(int d=1;d<=month.lengthOfMonth();d++){
                Cell c=team.cell(e.id,month.atDay(d));Post p=c==null?null:team.post(c.value);if(p!=null)counts[band(p)]++;
            }return counts;
        }
        public int[] actual(Employee e){int[] counts=current(e),past=previous.get(e.id);if(past!=null)for(int b=0;b<3;b++)counts[b]+=past[b];return counts;}
        private double penalty(Employee added,int band){
            double[] totals=new double[3],caps=new double[3];Map<String,int[]> counts=new HashMap<>();
            for(Employee e:team.employees)if(participates(e)){
                int[] n=actual(e);if(e==added)n[band]++;counts.put(e.id,n);
                for(int b=0;b<3;b++)if(capacity.get(e.id)[b]>0){totals[b]+=n[b];caps[b]+=capacity.get(e.id)[b];}
            }
            double score=0;
            for(Employee e:team.employees)if(participates(e))for(int b=0;b<3;b++)if(capacity.get(e.id)[b]>0&&caps[b]>0){
                double delta=counts.get(e.id)[b]-totals[b]*capacity.get(e.id)[b]/caps[b];score+=delta*delta;
            }
            return score;
        }
        public double penalty(){return team.shiftBalanceWeight*penalty(null,0);}
        public double marginal(Employee employee,Post p){
            int b=band(p);if(!participates(employee)||team.shiftBalanceWeight<=0||capacity.get(employee.id)[b]<=0)return 0;
            double total=0,cap=0;Map<String,Integer> counts=new HashMap<>();
            for(Employee e:team.employees)if(participates(e)&&capacity.get(e.id)[b]>0){int n=actual(e)[b];counts.put(e.id,n);total+=n;cap+=capacity.get(e.id)[b];}
            double delta=0;
            for(Employee e:team.employees)if(counts.containsKey(e.id)){
                double proportion=capacity.get(e.id)[b]/cap;
                double before=counts.get(e.id)-total*proportion;
                double change=(e==employee?1:0)-proportion;
                delta+=2*before*change+change*change;
            }
            return team.shiftBalanceWeight*delta;
        }
        public double target(Employee e,int band){
            double total=0,cap=0;for(Employee person:team.employees)if(participates(person)&&capacity.get(person.id)[band]>0){total+=actual(person)[band];cap+=capacity.get(person.id)[band];}
            return participates(e)&&cap>0?total*capacity.get(e.id)[band]/cap:0;
        }
    }
    public static String summary(Team t,YearMonth m){
        Context c=new Context(t,m);StringBuilder s=new StringBuilder(I18n.text("\nΠρωί / Απόγευμα / Νύχτα — μήνας και στόχοι περιόδου:\n"));
        for(Employee e:t.employees){s.append(e.name).append(" · ").append(Arrays.toString(c.current(e)));
            if(!c.participates(e))s.append(I18n.text(" · εκτός κατανομής ωραρίων"));
            else {s.append(I18n.text(" · περίοδος πραγματικό/στόχος: "));int[] n=c.actual(e);for(int b=0;b<3;b++)s.append(String.format(Locale.ROOT,"%d/%.1f%s",n[b],c.target(e,b),b<2?" · ":""));}
            s.append('\n');
        }
        s.append(I18n.text("Ενδεικτικοί αναλογικοί στόχοι, όχι ίσος αριθμός ανά υπηρεσία. Οι εφεδρικοί εξαιρούνται.\n"));return s.toString();
    }
}
