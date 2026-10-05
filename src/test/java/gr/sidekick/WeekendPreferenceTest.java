package gr.sidekick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class WeekendPreferenceTest {
    @TempDir Path temp;
    private Team fixture(Data data){
        Team t=TeamService.create(data,"Team");Post p=new Post("Day","08:00","16:00");t.posts.add(p);
        for(int i=0;i<5;i++){Employee e=new Employee("Employee "+i);e.skills.add(p.id);t.employees.add(e);}
        return t;
    }
    @Test void givesFullWeekendsWithoutLosingCoverageOrMonthlyTargets(){
        Data data=new Data();Team t=fixture(data);t.preferFullWeekend=true;YearMonth month=YearMonth.of(2026,10);
        new Scheduler().generate(data,t,month);
        assertTrue(CalendarRules.uncoveredDays(t,month).isEmpty());
        for(Employee e:t.employees){assertTrue(CalendarRules.fullWeekendOff(t,e,month),e.name);assertEquals(9,CalendarRules.count(t,e,month,OFF));assertEquals(22,Scheduler.workCount(t,e,month));}
    }
    @Test void preservesManualAssignmentsLockedBlanksAndOtherMonths(){
        Data data=new Data();Team t=fixture(data);t.preferFullWeekend=true;YearMonth month=YearMonth.of(2026,10);Employee e=t.employees.getFirst();
        for(int day:new int[]{3,10,17,24,31})t.month(month).cells.put(key(e.id,day),new Cell(t.posts.getFirst().id,true));
        t.month(month).lockedDays.add(4);
        Cell neighbor=new Cell(OFF,false);t.month(month.plusMonths(1)).cells.put(key(e.id,1),neighbor);
        var issues=new Scheduler().generate(data,t,month);
        for(int day:new int[]{3,10,17,24,31})assertTrue(t.cell(e.id,month.atDay(day)).locked);
        assertNull(t.cell(e.id,month.atDay(4)));assertSame(neighbor,t.cell(e.id,month.plusMonths(1).atDay(1)));
        assertFalse(CalendarRules.fullWeekendOff(t,e,month));assertTrue(issues.stream().anyMatch(s->s.contains(e.name)&&s.contains("Σαββατοκύριακο")));
    }
    @Test void preferencePersistsAndCopiesWithoutChangingDefault()throws Exception{
        Data data=new Data();Team t=fixture(data);assertFalse(t.preferFullWeekend);t.preferFullWeekend=true;
        assertTrue(TeamService.duplicate(data,t,"Copy").preferFullWeekend);
        Storage storage=new Storage(temp.resolve("data.json"));storage.save(data);assertTrue(storage.load().teams.getFirst().preferFullWeekend);
    }
}
