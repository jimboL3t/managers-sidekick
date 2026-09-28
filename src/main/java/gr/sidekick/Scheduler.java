package gr.sidekick;

import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;

/** Bounded multi-start heuristic; never claims that failure proves infeasibility. */
public final class Scheduler {
    private boolean work(Team t,Employee e,LocalDate d) { Cell c=t.cell(e.id,d);return c!=null&&t.post(c.value)!=null; }
    private boolean safe(Team t,Employee e,LocalDate d,Post p) {
        if(!e.skills.contains(p.id)) return false;
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
        Model.Month m=t.month(ym);Map<String,Cell> fixed=new LinkedHashMap<>();
        m.cells.forEach((k,v)->{if(v.locked)fixed.put(k,v);});
        Map<String,Cell> best=null;long bestScore=Long.MAX_VALUE;
        Random random=new Random(42);
        for(int attempt=0;attempt<180;attempt++) {
            m.cells=new LinkedHashMap<>(fixed);
            List<LocalDate> days=new ArrayList<>();for(int d=1;d<=ym.lengthOfMonth();d++)days.add(ym.atDay(d));
            if(attempt%3==1) Collections.reverse(days); else if(attempt%3==2)Collections.shuffle(days,random);
            int gaps=0;
            for(LocalDate day:days) {
                List<Post> posts=new ArrayList<>(t.posts);
                Collections.shuffle(posts,random);
                posts.sort(Comparator.comparingLong(p->t.employees.stream().filter(e->e.skills.contains(p.id)).count()));
                for(Post p:posts) {
                    int filled=(int)t.employees.stream().filter(e->{Cell c=t.cell(e.id,day);return c!=null&&p.id.equals(c.value);}).count();
                    for(int n=filled;n<p.required(day);n++) {
                        List<Employee> candidates=new ArrayList<>();
                        for(Employee e:t.employees) if(t.cell(e.id,day)==null&&withinMonthlyBudget(t,e,ym)&&safe(t,e,day,p)) candidates.add(e);
                        Collections.shuffle(candidates,random);
                        candidates.sort(Comparator.comparingDouble(e->cost(t,e,day,ym)));
                        if(candidates.isEmpty()) {gaps++;continue;}
                        Employee e=candidates.getFirst();m.cells.put(key(e.id,day.getDayOfMonth()),new Cell(p.id,false));
                    }
                }
            }
            int quotaDeviation=0, unassigned=0;
            for(Employee e:t.employees) {
                int remaining=CalendarRules.offTarget(t,ym)-CalendarRules.count(t,e,ym,OFF);
                List<Integer> empty=new ArrayList<>();
                for(int d=1;d<=ym.lengthOfMonth();d++) if(t.cell(e.id,ym.atDay(d))==null) empty.add(d);
                Collections.shuffle(empty,random);
                while(remaining>0 && !empty.isEmpty()) {
                    empty.sort(Comparator.comparingInt(d->offCost(t,e,ym.atDay(d))));
                    int d=empty.removeFirst();m.cells.put(key(e.id,d),new Cell(OFF,false));remaining--;
                }
                quotaDeviation+=Math.abs(remaining);
                unassigned+=empty.size();
            }
            int isolated=0;
            if(t.preferPaired)for(Employee e:t.employees)for(int d=2;d<ym.lengthOfMonth();d++)if(!work(t,e,ym.atDay(d))&&work(t,e,ym.atDay(d-1))&&work(t,e,ym.atDay(d+1)))isolated++;
            long score=gaps*1000000L+quotaDeviation*10000L+unassigned*100L+isolated;
            if(score<bestScore) {bestScore=score;best=new LinkedHashMap<>(m.cells);}
        }
        m.cells=best==null?fixed:best;
        return validate(data,t,ym);
    }
    private boolean withinMonthlyBudget(Team t,Employee e,YearMonth ym) {
        int working=0, leave=0;
        for(int d=1;d<=ym.lengthOfMonth();d++) {
            Cell c=t.cell(e.id,ym.atDay(d));
            if(c!=null) {if(t.post(c.value)!=null) working++;else if(!OFF.equals(c.value)) leave++;}
        }
        return working < ym.lengthOfMonth()-CalendarRules.offTarget(t,ym)-leave;
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
            if(p!=null&&!safe(t,e,day,p))issues.add(day+" · "+e.name+": δεξιότητα / συνεχόμενες ημέρες / ανάπαυση");
            if(p==null&&(!leaves(data,t).containsKey(c.value)||!t.allowedLeaves.contains(c.value)))issues.add(day+" · "+e.name+": μη επιτρεπόμενη άδεια");
        }
        for(Employee e:t.employees) {
            int actual=CalendarRules.count(t,e,ym,OFF), target=CalendarRules.offTarget(t,ym);
            int empty=0;for(int d=1;d<=ym.lengthOfMonth();d++)if(t.cell(e.id,ym.atDay(d))==null)empty++;
            if(empty>0)issues.add(e.name+": "+empty+" ημέρες χωρίς ανάθεση (δεν προσμετρώνται ως ρεπό)");
            if(actual!=target)issues.add(e.name+": ρεπό "+actual+" / "+target+" του μήνα");
        }
        for(int d=1;d<=ym.lengthOfMonth();d++)for(Post p:t.posts) {
            LocalDate day=ym.atDay(d);long n=t.employees.stream().filter(e->{Cell c=t.cell(e.id,day);return c!=null&&p.id.equals(c.value);}).count();
            int demand=p.required(day);
            if(n!=demand)issues.add(day+" · "+p.name+": κάλυψη "+n+" / "+demand);
        }
        if(!t.months.containsKey(ym.minusMonths(1).toString()))issues.add("Προειδοποίηση: απουσιάζει ο προηγούμενος μήνας· δεν επαληθεύεται πλήρως το αρχικό όριο.");
        if(!t.months.containsKey(ym.plusMonths(1).toString()))issues.add("Προειδοποίηση: απουσιάζει ο επόμενος μήνας· απαιτείται έλεγχος όταν δημιουργηθεί.");
        for(Employee e:t.employees) {
            boolean missing=false;
            for(int i=1;i<=7;i++) if(t.cell(e.id,ym.atDay(1).minusDays(i))==null || t.cell(e.id,ym.atEndOfMonth().plusDays(i))==null) missing=true;
            if(missing) issues.add("Προειδοποίηση: "+e.name+": ελλιπές ιστορικό στις γειτονικές ημέρες του μήνα.");
        }
        return issues;
    }
}
