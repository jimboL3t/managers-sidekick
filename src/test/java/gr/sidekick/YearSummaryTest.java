package gr.sidekick;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class YearSummaryTest {
    @Test void totalsRespectYearEmployeeAndCategoriesWithoutCreatingMissingMonths(){
        Data data=new Data();Team t=TeamService.create(data,"Ομάδα");Post p=new Post("Νύχτα","22:00","06:00");t.posts.add(p);
        Employee a=new Employee("Α"),b=new Employee("Β");t.employees.add(a);t.employees.add(b);
        String leave=t.leaveTypes.keySet().stream().filter(id->!OFF.equals(id)).findFirst().orElseThrow();
        t.month(YearMonth.of(2026,1)).cells.put(key(a.id,1),new Cell(p.id,true));
        t.month(YearMonth.of(2026,12)).cells.put(key(a.id,31),new Cell(p.id,false));
        t.month(YearMonth.of(2026,2)).cells.put(key(b.id,1),new Cell(OFF,true));
        t.month(YearMonth.of(2026,2)).cells.put(key(b.id,2),new Cell(leave,false));
        t.month(YearMonth.of(2025,12)).cells.put(key(a.id,31),new Cell(p.id,true));
        t.month(YearMonth.of(2026,6));String before=new Gson().toJson(data);
        YearSummary.Result summary=YearSummary.calculate(data,t,2026);
        assertEquals(3,summary.months());assertEquals(2,summary.shifts());assertEquals(1,summary.rest());assertEquals(1,summary.leave());
        assertEquals(2,summary.rows().getFirst().categories().get(p.id));assertEquals(0,summary.rows().getLast().shifts());
        assertEquals(before,new Gson().toJson(data));
        assertEquals(0,YearSummary.calculate(data,t,2027).months());assertEquals(before,new Gson().toJson(data));
    }
    @Test void leapDayAndOtherTeamsAreHandled(){
        Data data=new Data();Team t=TeamService.create(data,"Α"),other=TeamService.create(data,"Β");Employee e=new Employee("Ε");t.employees.add(e);
        t.month(YearMonth.of(2024,2)).cells.put(key(e.id,29),new Cell(OFF,true));other.month(YearMonth.of(2024,2)).cells.put(key(e.id,1),new Cell(OFF,true));
        assertEquals(1,YearSummary.calculate(data,t,2024).rest());assertEquals(0,YearSummary.calculate(data,t,2025).rest());
    }
}
