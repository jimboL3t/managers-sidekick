// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkloadTest {
    @TempDir Path temp;
    private final YearMonth month=YearMonth.of(2026,9);
    private Team team(Data data,int people){
        Team t=TeamService.create(data,"Test");Post p=new Post("Service","08:00","16:00");t.posts.add(p);
        for(int i=0;i<people;i++){Employee e=new Employee("Person "+i);e.skills.add(p.id);t.employees.add(e);}return t;
    }
    @Test void defaultsIncludeHolidaysAndLeaveAndCustomTargetIsMonthSpecific(){
        Data data=new Data();Team t=team(data,1);Employee e=t.employees.getFirst();
        assertEquals(22,Workload.target(t,e,month));t.month(month).holidays.add(1);
        String leave=t.leaveTypes.keySet().stream().filter(k->!OFF.equals(k)).findFirst().orElseThrow();
        t.month(month).cells.put(key(e.id,2),new Cell(leave,true));assertEquals(20,Workload.target(t,e,month));
        Workload.set(t,e,month,"target",12);assertEquals(12,Workload.target(t,e,month));assertEquals(17,Workload.offTarget(t,e,month));
        assertEquals(22,Workload.target(t,e,month.plusMonths(1)));Workload.set(t,e,month,"auto",0);assertEquals(20,Workload.target(t,e,month));
    }
    @Test void reducedIncreasedAndZeroTargetsAreRespected(){
        Data data=new Data();Team t=team(data,3);int[] targets={12,24,0};
        for(int i=0;i<3;i++)Workload.set(t,t.employees.get(i),month,"target",targets[i]);
        new Scheduler().generate(data,t,month);
        for(int i=0;i<3;i++){Employee e=t.employees.get(i);assertEquals(targets[i],Scheduler.workCount(t,e,month));assertEquals(30-targets[i],CalendarRules.count(t,e,month,OFF));}
    }
    @Test void reserveCoversOnlyGapsInQualifiedService(){
        Data data=new Data();Team t=team(data,2);Employee regular=t.employees.getFirst(),reserve=t.employees.getLast();
        Workload.set(t,reserve,month,"reserve",10);
        new Scheduler().generate(data,t,month);
        assertEquals(22,Scheduler.workCount(t,regular,month));assertEquals(8,Scheduler.workCount(t,reserve,month));assertTrue(CalendarRules.uncoveredDays(t,month).isEmpty());
        assertEquals(0,CalendarRules.count(t,reserve,month,OFF));
        for(int d=1;d<=30;d++){Cell c=t.cell(reserve.id,month.atDay(d));if(c!=null){assertEquals(t.posts.getFirst().id,c.value);assertEquals(OFF,t.cell(regular.id,month.atDay(d)).value);}}
        assertFalse(new Fairness.Context(t,month).participates(reserve,true));
        assertTrue(new ScheduleTableModel(data,t,month,()->{},()->true).getValueAt(1,0).toString().contains("Εφεδρικός"));
    }
    @Test void reserveIsNotFilledWhenRegularStaffCanCoverAndCapMayLeaveGaps(){
        Data data=new Data();Team t=team(data,3);Employee reserve=t.employees.getLast();Workload.set(t,reserve,month,"reserve",10);
        new Scheduler().generate(data,t,month);assertEquals(0,Scheduler.workCount(t,reserve,month));assertTrue(CalendarRules.uncoveredDays(t,month).isEmpty());
        t.employees.remove(1);Workload.set(t,reserve,month,"reserve",3);new Scheduler().generate(data,t,month);
        assertEquals(3,Scheduler.workCount(t,reserve,month));assertEquals(5,CalendarRules.uncoveredDays(t,month).size());
    }
    @Test void reserveNeverUsesUnqualifiedServiceAndManualChoicesOverrideCap(){
        Data data=new Data();Team t=team(data,1);Employee reserve=t.employees.getFirst();Post other=new Post("Other","08:00","16:00");t.posts.add(other);Workload.set(t,reserve,month,"reserve",1);
        for(int d:new int[]{1,3})t.month(month).cells.put(key(reserve.id,d),new Cell(t.posts.getFirst().id,true));t.month(month).lockedDays.add(2);
        var issues=new Scheduler().generate(data,t,month);assertEquals(2,Scheduler.workCount(t,reserve,month));assertNull(t.cell(reserve.id,month.atDay(2)));
        assertTrue(issues.stream().anyMatch(s->s.contains("βάρδιες")&&s.contains("2 / 1")));
        for(int d=1;d<=30;d++){Cell c=t.cell(reserve.id,month.atDay(d));assertTrue(c==null||!other.id.equals(c.value));}
    }
    @Test void reserveHonorsAvailabilityAndSkillsWhenActuallyScheduling(){
        Data data=new Data();Team t=team(data,1);Employee e=t.employees.getFirst();Workload.set(t,e,month,"reserve",20);
        t.posts.add(new Post("Ineligible","08:00","16:00"));e.availableWeekdays[0]=false;
        new Scheduler().generate(data,t,month);
        assertTrue(Scheduler.workCount(t,e,month)>0);
        for(int d=1;d<=30;d++){Cell c=t.cell(e.id,month.atDay(d));if(c!=null){assertEquals(t.posts.getFirst().id,c.value);assertNotEquals(DayOfWeek.MONDAY,month.atDay(d).getDayOfWeek());}}
    }
    @Test void monthlyEditorIsReadOnlyUntilSavedAndSupportsReturningToAuto()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            try{
                Theme.install();UiScale.set(100);Data data=new Data();Team t=team(data,1);Employee e=t.employees.getFirst();
                WorkloadPanel panel=new WorkloadPanel(t,e,month);assertNull(Workload.plan(t,e,month));
                javax.swing.JComboBox<?> mode=null;javax.swing.JSpinner spinner=null;
                for(var c:panel.getComponents()){if(c instanceof javax.swing.JComboBox<?> combo)mode=combo;if(c instanceof javax.swing.JSpinner count)spinner=count;}
                assertNotNull(mode);assertNotNull(spinner);assertFalse(spinner.isEnabled());
                mode.setSelectedIndex(2);spinner.setValue(7);assertNull(Workload.plan(t,e,month));panel.save(t,e,month);
                assertTrue(Workload.reserve(t,e,month));assertEquals(7,Workload.target(t,e,month));
                assertEquals(7,new Fairness.Context(t,month).capacity(e,false));
                panel.setSize(650,320);layout(panel);var image=new java.awt.image.BufferedImage(650,320,java.awt.image.BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();panel.printAll(g);g.dispose();javax.imageio.ImageIO.write(image,"png",Path.of("target","workload-preview.png").toFile());
                mode.setSelectedIndex(0);panel.save(t,e,month);assertNull(Workload.plan(t,e,month));assertEquals(22,Workload.target(t,e,month));
            }catch(Exception ex){throw new RuntimeException(ex);}
        });
    }
    private static void layout(java.awt.Container c){c.doLayout();for(var child:c.getComponents())if(child instanceof java.awt.Container nested)layout(nested);}
    @Test void plansPersistCopyAndSnapshotIndependently()throws Exception{
        Data data=new Data();Team t=team(data,1);Employee e=t.employees.getFirst();Workload.set(t,e,month,"reserve",6);
        Storage storage=new Storage(temp.resolve("test.json"));storage.save(data);Team loaded=storage.load().teams.getFirst();assertTrue(Workload.reserve(loaded,loaded.employees.getFirst(),month));
        Team copy=TeamService.duplicate(data,t,"Copy");Employee ce=copy.employees.getFirst();assertTrue(Workload.reserve(copy,ce,month));Workload.set(copy,ce,month,"target",9);assertEquals(6,Workload.target(t,e,month));
        Team frozen=History.read(History.capture(t,month,"Plan"));assertEquals(6,Workload.target(frozen,frozen.employees.getFirst(),month));
    }
}
