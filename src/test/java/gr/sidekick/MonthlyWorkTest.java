package gr.sidekick;
import org.junit.jupiter.api.Test;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class MonthlyWorkTest {
 @Test void fourEmployeesReceive22ShiftsWithLockedChoicesPreserved(){
  Data data=new Data();Team t=new Team("Ομάδα");YearMonth ym=YearMonth.of(2026,9);
  t.posts.add(new Post("Πρωί","08:00","16:00"));t.posts.add(new Post("Απόγευμα","16:00","00:00"));t.posts.add(new Post("Νύχτα","00:00","08:00"));
  for(int i=0;i<4;i++){Employee e=new Employee("Εργαζόμενος "+i);for(Post p:t.posts)e.skills.add(p.id);t.employees.add(e);}
  Employee e=t.employees.getFirst();Cell off=new Cell(OFF,true),post=new Cell(t.posts.getFirst().id,true);
  t.month(ym).cells.put(key(e.id,1),off);t.month(ym).cells.put(key(e.id,3),post);
  new Scheduler().generate(data,t,ym);
  for(Employee person:t.employees){assertEquals(22,Scheduler.workCount(t,person,ym),person.name);assertEquals(8,CalendarRules.count(t,person,ym,OFF));}
  assertSame(off,t.cell(e.id,ym.atDay(1)));assertSame(post,t.cell(e.id,ym.atDay(3)));
 }
 @Test void clearManualOnlyRemovesSelectedMonthsLockedCells(){
  Team t=new Team("Α");YearMonth ym=YearMonth.of(2026,9);Employee e=new Employee("Ε");t.employees.add(e);
  Cell auto=new Cell(OFF,false),locked=new Cell(OFF,true);
  t.month(ym).cells.put(key(e.id,1),locked);t.month(ym).cells.put(key(e.id,2),auto);t.month(ym).holidays.add(8);
  t.month(ym.plusMonths(1)).cells.put(key(e.id,1),locked);
  Scheduler.clearManual(t,ym);
  assertNull(t.cell(e.id,ym.atDay(1)));assertSame(auto,t.cell(e.id,ym.atDay(2)));assertTrue(t.month(ym).holidays.contains(8));assertSame(locked,t.cell(e.id,ym.plusMonths(1).atDay(1)));
 }
}
