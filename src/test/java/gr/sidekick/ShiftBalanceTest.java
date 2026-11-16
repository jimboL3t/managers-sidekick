// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import com.google.gson.Gson;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class ShiftBalanceTest {
    @TempDir Path temp;
    private final YearMonth m=YearMonth.of(2026,12);
    private Team team(Data data){
        Team t=TeamService.create(data,"Synthetic");t.preferPaired=false;
        for(int b=0;b<3;b++)for(int i=0;i<2;i++){Post p=new Post("Duty "+b+"/"+i,"08:00","16:00");p.balanceBand=b+1;t.posts.add(p);}
        for(int i=0;i<6;i++){Employee e=new Employee("Person "+i);t.posts.forEach(p->e.skills.add(p.id));t.employees.add(e);}return t;
    }
    @Test void parallelDutiesShareBandsAndExplicitOverrideWins(){
        Post a=new Post("A","06:00","14:00"),b=new Post("B","07:00","15:00");assertEquals(ShiftBalance.band(a),ShiftBalance.band(b));
        b.start="15:00";assertEquals(1,ShiftBalance.band(b));b.start="23:00";assertEquals(2,ShiftBalance.band(b));b.balanceBand=1;assertEquals(0,ShiftBalance.band(b));
    }
    @Test void safeRefinementBalancesParallelDutiesWithoutChangingCoverageOrWorkdays(){
        Data data=new Data();Team t=team(data);t.shiftBalanceWeight=5;
        for(int d=1;d<=30;d++)for(int e=0;e<6;e++)t.month(m).cells.put(key(t.employees.get(e).id,d),new Cell(d%3==0?OFF:t.posts.get(e).id,false));
        t.month(m).lockedDays.add(1);t.month(m).cells.get(key(t.employees.getFirst().id,2)).locked=true;
        String locked=new Gson().toJson(t.month(m).cells.get(key(t.employees.getFirst().id,2)));
        ShiftBalance.Context c=new ShiftBalance.Context(t,m);double before=c.penalty();
        new Scheduler().refine(t,m,new Fairness.Context(t,m),c);
        assertTrue(c.penalty()<before*.1,"Expected substantial improvement: "+before+" -> "+c.penalty());
        for(Employee e:t.employees)assertEquals(20,Scheduler.workCount(t,e,m));
        for(int d=1;d<=30;d++)for(Post p:t.posts){int day=d;assertEquals(d%3==0?0:1,t.employees.stream().filter(e->{Cell v=t.cell(e.id,m.atDay(day));return v!=null&&p.id.equals(v.value);}).count());}
        assertEquals(locked,new Gson().toJson(t.month(m).cells.get(key(t.employees.getFirst().id,2))));
        for(int e=0;e<6;e++)assertEquals(t.posts.get(e).id,t.cell(t.employees.get(e).id,m.atDay(1)).value);
    }
    @Test void reserveTotalsDoNotChangeRegularTargetsAndLeaveReducesCapacity(){
        Data data=new Data();Team t=team(data);Employee reserve=t.employees.getLast();Workload.set(t,reserve,m,"reserve",22);
        Employee a=t.employees.getFirst(),b=t.employees.get(1);t.shiftBalanceWeight=5;
        for(int d=1;d<=6;d++)t.month(m).cells.put(key(a.id,d),new Cell(t.posts.getFirst().id,true));
        ShiftBalance.Context c=new ShiftBalance.Context(t,m);double target=c.target(a,0);
        for(int d=1;d<=22;d++)t.month(m).cells.put(key(reserve.id,d),new Cell(t.posts.getFirst().id,true));
        assertEquals(target,c.target(a,0));assertFalse(c.participates(reserve));
        Workload.set(t,b,m,"target",11);c=new ShiftBalance.Context(t,m);assertEquals(c.target(a,0)*11/Workload.target(t,a,m),c.target(b,0),.0001);
        a.excludeShiftBalance=true;c=new ShiftBalance.Context(t,m);assertFalse(c.participates(a));
    }
    @Test void historicalReserveMonthsAndFutureMonthsAreExcluded(){
        Data data=new Data();Team t=team(data);Employee e=t.employees.getFirst();t.fairnessLookback=1;
        for(YearMonth month:new YearMonth[]{m.minusMonths(1),m.plusMonths(1)})t.month(month).cells.put(key(e.id,1),new Cell(t.posts.getFirst().id,true));
        assertEquals(1,new ShiftBalance.Context(t,m).actual(e)[0]);Workload.set(t,e,m.minusMonths(1),"reserve",10);
        assertEquals(0,new ShiftBalance.Context(t,m).actual(e)[0]);
    }
    @Test void refinementNeverIntroducesRestAvailabilityOrSkillViolations(){
        Data data=new Data();Team t=team(data);t.shiftBalanceWeight=5;t.employees.subList(2,6).clear();t.posts.subList(2,6).clear();
        Post morning=t.posts.getFirst(),night=t.posts.getLast();morning.start="06:00";morning.end="14:00";night.start="22:00";night.end="06:00";night.nightDuty=true;night.balanceBand=3;
        Employee a=t.employees.getFirst(),b=t.employees.getLast();b.noNights=true;
        t.month(m).cells.put(key(a.id,1),new Cell(night.id,false));t.month(m).cells.put(key(b.id,1),new Cell(morning.id,false));
        t.month(m).cells.put(key(b.id,2),new Cell(morning.id,true));
        t.month(m).cells.put(key(a.id,5),new Cell(night.id,true));t.month(m).cells.put(key(b.id,5),new Cell(morning.id,true));
        String original=new Gson().toJson(t.month(m).cells);new Scheduler().refine(t,m,new Fairness.Context(t,m),new ShiftBalance.Context(t,m));assertEquals(original,new Gson().toJson(t.month(m).cells));
        b.noNights=false;new Scheduler().refine(t,m,new Fairness.Context(t,m),new ShiftBalance.Context(t,m));assertEquals(original,new Gson().toJson(t.month(m).cells));
    }
    @Test void reserveDoesNotCreateWorkJustToFillOptionalDuty(){
        Data data=new Data();Team t=team(data);t.employees.subList(1,6).clear();t.posts.forEach(p->p.optional=true);Employee e=t.employees.getFirst();Workload.set(t,e,m,"reserve",22);t.shiftBalanceWeight=5;
        new Scheduler().generate(data,t,m);assertEquals(0,Scheduler.workCount(t,e,m));assertTrue(CalendarRules.uncoveredDays(t,m).isEmpty());
    }
    @Test void generatedParallelRealShiftsImproveWithoutTradingAwayCoverageOrQuotas(){
        Data data=new Data();Team plain=team(data);String[] starts={"06:00","07:00","14:00","15:00","22:00","23:00"},ends={"14:00","15:00","22:00","23:00","06:00","07:00"};
        for(int i=0;i<6;i++){Post p=plain.posts.get(i);p.start=starts[i];p.end=ends[i];p.balanceBand=0;p.nightDuty=i>=4;}
        for(int i=6;i<10;i++){Employee e=new Employee("Person "+i);plain.posts.forEach(p->e.skills.add(p.id));plain.employees.add(e);}
        Employee reserve=plain.employees.getLast();reserve.skills.clear();reserve.skills.add(plain.posts.getLast().id);Workload.set(plain,reserve,m,"reserve",23);
        Team balanced=new Gson().fromJson(new Gson().toJson(plain),Team.class);balanced.shiftBalanceWeight=5;
        Scheduler scheduler=new Scheduler();scheduler.generate(data,plain,m,42);scheduler.generate(data,balanced,m,42);
        plain.shiftBalanceWeight=5;double before=new ShiftBalance.Context(plain,m).penalty(),after=new ShiftBalance.Context(balanced,m).penalty();
        assertTrue(after<before*.5,"Shift-mix error: "+before+" -> "+after);
        assertTrue(CalendarRules.uncoveredDays(balanced,m).size()<=CalendarRules.uncoveredDays(plain,m).size());
        for(Employee e:balanced.employees)if(!Workload.reserve(balanced,e,m))assertEquals(Workload.target(balanced,e,m),Scheduler.workCount(balanced,e,m));
        assertFalse(scheduler.validate(data,balanced,m).stream().anyMatch(s->s.contains("ανάπαυση")||s.contains("διαθεσιμότητας")));
    }
    @Test void marginalMatchesActualPenaltyChange(){
        Data d=new Data();Team t=team(d);t.shiftBalanceWeight=5;t.month(m).cells.put(key(t.employees.getFirst().id,1),new Cell(t.posts.getFirst().id,false));
        ShiftBalance.Context c=new ShiftBalance.Context(t,m);Employee e=t.employees.get(1);Post p=t.posts.getFirst();double before=c.penalty(),delta=c.marginal(e,p);t.month(m).cells.put(key(e.id,2),new Cell(p.id,false));assertEquals(delta,c.penalty()-before,.000001);
    }
    @Test void newSettingsPersistAndPanelStagesUntilSave()throws Exception{
        Data data=new Data();Team t=team(data);javax.swing.SwingUtilities.invokeAndWait(()->{
            try{Theme.install();}catch(Exception ex){throw new RuntimeException(ex);}FairnessPanel panel=new FairnessPanel(t,m);panel.bands.setValue(5);assertEquals(0,t.shiftBalanceWeight);panel.save(t);assertEquals(5,t.shiftBalanceWeight);
        });t.employees.getFirst().excludeShiftBalance=true;
        Storage s=new Storage(temp.resolve("test.json"));s.save(data);Team loaded=s.load().teams.getFirst();assertEquals(5,loaded.shiftBalanceWeight);assertEquals(1,loaded.posts.getFirst().balanceBand);assertTrue(loaded.employees.getFirst().excludeShiftBalance);
        Team copy=TeamService.duplicate(data,t,"Copy");assertEquals(5,copy.shiftBalanceWeight);copy.posts.getFirst().balanceBand=3;assertEquals(1,t.posts.getFirst().balanceBand);
    }
}
