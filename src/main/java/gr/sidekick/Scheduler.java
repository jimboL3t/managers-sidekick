// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;

/** Bounded multi-start heuristic; never claims that failure proves infeasibility. */
public final class Scheduler {
    private record Score(int gaps,int quota,int empty,int weekends,int reserves,double preferences) implements Comparable<Score>{
        public int compareTo(Score other){int c=Integer.compare(gaps,other.gaps);if(c==0)c=Integer.compare(quota,other.quota);if(c==0)c=Integer.compare(empty,other.empty);if(c==0)c=Integer.compare(reserves,other.reserves);if(c==0)c=Integer.compare(weekends,other.weekends);return c==0?Double.compare(preferences,other.preferences):c;}
    }
    private boolean work(Team t,Employee e,LocalDate d) { Cell c=t.cell(e.id,d);return c!=null&&t.post(c.value)!=null; }
    private boolean safe(Team t,Employee e,LocalDate d,Post p) {
        if(!Fairness.allowed(e,p,d)||!p.operates(d)||!Availability.allows(e,p,d)||!e.skills.contains(p.id)) return false;
        int run=1;
        for(LocalDate x=d.minusDays(1);work(t,e,x);x=x.minusDays(1)) run++;
        for(LocalDate x=d.plusDays(1);work(t,e,x);x=x.plusDays(1)) run++;
        if(run>t.maxConsecutive) return false;
        // Overnight shifts may require checking across an intervening calendar day.
        for(int delta : new int[]{-2,-1,1,2}) {
            LocalDate x=d.plusDays(delta);Cell c=t.cell(e.id,x);Post q=c==null?null:t.post(c.value);
            if(q!=null) {
                long hours=delta<0?Duration.between(q.endAt(x),p.startAt(d)).toMinutes():Duration.between(p.endAt(d),q.startAt(x)).toMinutes();
                if(hours<t.minRestHours*60L) return false;
            }
        }
        return true;
    }
    public List<String> generate(Data data,Team t,YearMonth ym) {
        return generate(data,t,ym,42L);
    }
    public List<String> generate(Data data,Team t,YearMonth ym,long seed) {
        Model.Month m=t.month(ym);Map<String,Cell> fixed=new LinkedHashMap<>();
        m.cells.forEach((k,v)->{if(v.locked||m.lockedDays.contains(Integer.parseInt(k.substring(k.lastIndexOf(':')+1))))fixed.put(k,v);});
        Map<String,Cell> best=null;Score bestScore=null;
        Fairness.Context fairness=new Fairness.Context(t,ym);boolean balancing=t.nightBalanceWeight>0||t.weekendBalanceWeight>0;
        Random random=new Random(seed);
        for(int attempt=0;attempt<(balancing?360:180);attempt++) {
            m.cells=new LinkedHashMap<>(fixed);
            if(t.preferFullWeekend&&attempt%2==1)reserveWeekends(t,ym,random);
            boolean guide=balancing&&attempt>=180;
            List<LocalDate> days=new ArrayList<>();for(int d=1;d<=ym.lengthOfMonth();d++)days.add(ym.atDay(d));
            if(attempt%3==1) Collections.reverse(days); else if(attempt%3==2)Collections.shuffle(days,random);
            int gaps=0;
            for(boolean optionalPhase:new boolean[]{false,true}) for(LocalDate day:days) {
                if(m.lockedDays.contains(day.getDayOfMonth()))continue;
                List<Post> posts=new ArrayList<>(t.posts.stream().filter(p->p.optional==optionalPhase).toList());
                Collections.shuffle(posts,random);
                posts.sort(Comparator.comparingLong(p->t.employees.stream().filter(e->e.skills.contains(p.id)).count()));
                for(Post p:posts) {
                    int filled=(int)t.employees.stream().filter(e->{Cell c=t.cell(e.id,day);return c!=null&&p.id.equals(c.value);}).count();
                    for(int n=filled;n<p.slots(day);n++) {
                        List<Employee> candidates=new ArrayList<>();
                        for(Employee e:t.employees) if(t.cell(e.id,day)==null&&withinMonthlyBudget(t,e,ym)&&safe(t,e,day,p)) candidates.add(e);
                        Collections.shuffle(candidates,random);
                        candidates.sort(Comparator.<Employee,Boolean>comparing(e->Workload.reserve(t,e,ym)).thenComparingDouble(e->cost(t,e,day,ym)+(guide?fairness.marginal(e,p,day):0)));
                        if(candidates.isEmpty()) {if(!p.optional)gaps++;continue;}
                        Employee e=candidates.getFirst();m.cells.put(key(e.id,day.getDayOfMonth()),new Cell(p.id,false));
                    }
                }
            }
            // Coverage is a minimum. Fill each employee's monthly work entitlement
            // even when more staff are available than the minimum number of slots.
            List<Employee> employees=new ArrayList<>(t.employees);Collections.shuffle(employees,random);
            for(Employee e:employees) for(LocalDate day:days) {
                if(Workload.reserve(t,e,ym)||m.lockedDays.contains(day.getDayOfMonth())||t.cell(e.id,day)!=null||!withinMonthlyBudget(t,e,ym))continue;
                List<Post> eligible=new ArrayList<>();
                for(Post p:t.posts)if(safe(t,e,day,p))eligible.add(p);
                Collections.shuffle(eligible,random);
                eligible.sort(Comparator.comparingDouble(p->t.employees.stream().filter(person->{Cell c=t.cell(person.id,day);return c!=null&&p.id.equals(c.value);}).count()+(guide?fairness.marginal(e,p,day):0)));
                if(!eligible.isEmpty())m.cells.put(key(e.id,day.getDayOfMonth()),new Cell(eligible.getFirst().id,false));
            }
            // A regular employee may have filled a reserve's slot during quota completion.
            // Remove only redundant automatic reserve assignments; fixed choices stay untouched.
            for(Employee e:t.employees)if(Workload.reserve(t,e,ym))for(LocalDate day:days){
                Cell c=t.cell(e.id,day);Post p=c==null?null:t.post(c.value);
                if(p==null||c.locked||m.lockedDays.contains(day.getDayOfMonth()))continue;
                long filled=t.employees.stream().filter(other->{Cell cell=t.cell(other.id,day);return cell!=null&&p.id.equals(cell.value);}).count();
                if(filled>p.slots(day))m.cells.remove(key(e.id,day.getDayOfMonth()));
            }
            int quotaDeviation=0, unassigned=0;
            for(Employee e:t.employees) {
                if(Workload.reserve(t,e,ym))continue;
                int remaining=Workload.offTarget(t,e,ym)-CalendarRules.count(t,e,ym,OFF);
                List<Integer> empty=new ArrayList<>();
                for(int d=1;d<=ym.lengthOfMonth();d++) if(!m.lockedDays.contains(d)&&t.cell(e.id,ym.atDay(d))==null) empty.add(d);
                Collections.shuffle(empty,random);
                while(remaining>0 && !empty.isEmpty()) {
                    empty.sort(Comparator.comparingInt(d->offCost(t,e,ym.atDay(d))));
                    int d=empty.removeFirst();m.cells.put(key(e.id,d),new Cell(OFF,false));remaining--;
                }
                quotaDeviation+=Math.abs(remaining);
                unassigned+=empty.size();
            }
            int isolated=0;
            if(t.preferPaired)for(Employee e:t.employees)if(!Workload.reserve(t,e,ym))for(int d=2;d<ym.lengthOfMonth();d++)if(!work(t,e,ym.atDay(d))&&work(t,e,ym.atDay(d-1))&&work(t,e,ym.atDay(d+1)))isolated++;
            int missingWeekends=t.preferFullWeekend?(int)t.employees.stream().filter(e->!Workload.reserve(t,e,ym)&&!CalendarRules.fullWeekendOff(t,e,ym)).count():0;
            Score score=new Score(gaps,quotaDeviation,unassigned,missingWeekends,t.employees.stream().filter(e->Workload.reserve(t,e,ym)).mapToInt(e->workCount(t,e,ym)).sum(),fairness.penalty()+isolated);
            if(bestScore==null||score.compareTo(bestScore)<0) {bestScore=score;best=new LinkedHashMap<>(m.cells);}
        }
        m.cells=best==null?fixed:best;
        return validate(data,t,ym);
    }
    private void reserveWeekends(Team team,YearMonth month,Random random){
        Model.Month schedule=team.month(month);
        List<Employee> employees=new ArrayList<>(team.employees);Collections.shuffle(employees,random);
        for(Employee e:employees){
            if(Workload.reserve(team,e,month)||CalendarRules.fullWeekendOff(team,e,month))continue;
            int budget=Workload.offTarget(team,e,month)-CalendarRules.count(team,e,month,OFF);
            List<Integer> candidates=new ArrayList<>();
            for(int d=1;d<month.lengthOfMonth();d++){
                if(month.atDay(d).getDayOfWeek()!=DayOfWeek.SATURDAY)continue;
                boolean possible=true;int needed=0;
                for(int day=d;day<=d+1;day++){
                    Cell cell=team.cell(e.id,month.atDay(day));
                    if(cell==null){needed++;if(schedule.lockedDays.contains(day))possible=false;}
                    else if(!OFF.equals(cell.value))possible=false;
                }
                if(possible&&needed<=budget)candidates.add(d);
            }
            Collections.shuffle(candidates,random);
            if(!candidates.isEmpty())for(int day=candidates.getFirst();day<=candidates.getFirst()+1;day++)
                schedule.cells.putIfAbsent(key(e.id,day),new Cell(OFF,false));
        }
    }

    public static int workTarget(Team t,Employee e,YearMonth ym) {
        return Workload.target(t,e,ym);
    }
    public static int workCount(Team t,Employee e,YearMonth ym) {
        int count=0;for(int d=1;d<=ym.lengthOfMonth();d++){
            Cell c=t.cell(e.id,ym.atDay(d));if(c!=null&&t.post(c.value)!=null)count++;
        }return count;
    }
    public static void clearManual(Team t,YearMonth ym) {
        Model.Month month=t.months.get(ym.toString());
        if(month!=null)month.cells.entrySet().removeIf(entry->entry.getValue().locked&&!month.lockedDays.contains(Integer.parseInt(entry.getKey().substring(entry.getKey().lastIndexOf(':')+1))));
    }
    private boolean withinMonthlyBudget(Team t,Employee e,YearMonth ym) {
        return workCount(t,e,ym)<workTarget(t,e,ym);
    }
    private int offCost(Team t,Employee e,LocalDate day) {
        if(!t.preferPaired)return 0;
        for(int delta:new int[]{-1,1}) {
            Cell c=t.cell(e.id,day.plusDays(delta));
            if(c!=null&&OFF.equals(c.value))return -1;
        }
        return 0;
    }
    private double cost(Team t,Employee e,LocalDate d,YearMonth ym) {
        int count=0;for(int i=1;i<=ym.lengthOfMonth();i++)if(work(t,e,ym.atDay(i)))count++;
        return count+(t.preferPaired&&(work(t,e,d.minusDays(1))||work(t,e,d.plusDays(1)))?-1.5:0);
    }
    public List<String> validate(Data data,Team t,YearMonth ym) {
        List<String> issues=new ArrayList<>();
        for(Employee e:t.employees)for(int d=1;d<=ym.lengthOfMonth();d++) {
            LocalDate day=ym.atDay(d);Cell c=t.cell(e.id,day);
            if(c==null) continue;
            Post p=t.post(c.value);
            if(p!=null&&!Fairness.allowed(e,p,day))issues.add(day+" · "+e.name+I18n.text(": απαγορευμένη νύχτα ή εργασία Σαββατοκύριακου"));
            if(p!=null&&!Availability.allows(e,p,day))issues.add(day+" · "+e.name+I18n.text(": εργασία εκτός διαθεσιμότητας"));
            if(p!=null&&!p.operates(day))issues.add(day+" · "+e.name+" · "+p.name+I18n.text(": ανάθεση εκτός ημερών λειτουργίας"));
            if(p!=null&&p.operates(day)&&!safe(t,e,day,p))issues.add(day+" · "+e.name+I18n.text(": δεξιότητα / συνεχόμενες ημέρες / ανάπαυση"));
            if(p==null&&(!leaves(data,t).containsKey(c.value)||!t.allowedLeaves.contains(c.value)))issues.add(day+" · "+e.name+I18n.text(": μη επιτρεπόμενη άδεια"));
        }
        for(Employee e:t.employees) {
            if(t.posts.stream().noneMatch(p->e.skills.contains(p.id)))issues.add(e.name+I18n.text(": δεν έχουν επιλεγεί επιτρεπόμενες υπηρεσίες. Ορίστε τα από «Δεξιότητες»."));
            if(t.preferFullWeekend&&!Workload.reserve(t,e,ym)&&!CalendarRules.fullWeekendOff(t,e,ym))issues.add(e.name+I18n.text(": δεν βρέθηκε πλήρες Σαββατοκύριακο ρεπό στον μήνα"));
            int actual=CalendarRules.count(t,e,ym,OFF), target=Workload.offTarget(t,e,ym);
            int empty=0;for(int d=1;d<=ym.lengthOfMonth();d++)if(t.cell(e.id,ym.atDay(d))==null)empty++;
            int worked=workCount(t,e,ym), planned=workTarget(t,e,ym);
            if(Workload.reserve(t,e,ym)?worked>planned:worked!=planned)issues.add(e.name+I18n.text(": βάρδιες ")+worked+" / "+planned);
            if(empty>0&&!Workload.reserve(t,e,ym))issues.add(e.name+": "+empty+I18n.text(" ημέρες χωρίς ανάθεση (δεν προσμετρώνται ως ρεπό)"));
            if(actual!=target&&!Workload.reserve(t,e,ym))issues.add(e.name+I18n.text(": ρεπό ")+actual+" / "+target+I18n.text(" του μήνα"));
        }
        for(int d=1;d<=ym.lengthOfMonth();d++)for(Post p:t.posts) {
            LocalDate day=ym.atDay(d);long n=t.employees.stream().filter(e->{Cell c=t.cell(e.id,day);return c!=null&&p.id.equals(c.value);}).count();
            int demand=p.required(day);
            if(n<demand)issues.add(day+" · "+p.name+I18n.text(": κάλυψη ")+n+" / "+demand);
        }
        if(!t.months.containsKey(ym.minusMonths(1).toString()))issues.add(I18n.text("Προειδοποίηση: απουσιάζει ο προηγούμενος μήνας· δεν επαληθεύεται πλήρως το αρχικό όριο."));
        if(!t.months.containsKey(ym.plusMonths(1).toString()))issues.add(I18n.text("Προειδοποίηση: απουσιάζει ο επόμενος μήνας· απαιτείται έλεγχος όταν δημιουργηθεί."));
        for(Employee e:t.employees) {
            boolean missing=false;
            for(int i=1;i<=Math.max(7,t.maxConsecutive);i++) if(t.cell(e.id,ym.atDay(1).minusDays(i))==null || t.cell(e.id,ym.atEndOfMonth().plusDays(i))==null) missing=true;
            if(missing) issues.add(I18n.text("Προειδοποίηση: ")+e.name+I18n.text(": ελλιπές ιστορικό στις γειτονικές ημέρες του μήνα."));
        }
        return issues;
    }
}
