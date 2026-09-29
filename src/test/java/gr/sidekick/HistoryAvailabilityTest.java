package gr.sidekick;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class HistoryAvailabilityTest {
 @TempDir Path temp;
 @Test void historyFreezesNamesRulesAndMonthAndRestoresIndependentCopy()throws Exception{
  Data d=new Data();Team t=TeamService.create(d,"Original");Employee e=new Employee("Name");t.employees.add(e);Post p=new Post("Night","22:00","06:00");t.posts.add(p);e.skills.add(p.id);YearMonth ym=YearMonth.of(2026,9);t.month(ym).cells.put(key(e.id,1),new Cell(p.id,true));t.month(ym).lockedDays.add(1);t.month(ym.plusMonths(1));
  Revision r=History.capture(t,ym,"Published");t.name="Changed";p.start="21:00";e.name="Renamed";t.month(ym).cells.clear();
  Storage storage=new Storage(temp.resolve("data.json"));storage.save(d);Data loaded=storage.load();Team frozen=History.read(loaded.teams.getFirst().history.getFirst());assertEquals("Original",frozen.name);assertEquals("22:00",frozen.posts.getFirst().start);assertEquals("Name",frozen.employees.getFirst().name);assertEquals(1,frozen.months.size());assertTrue(frozen.history.isEmpty());
  Team restored=History.restoreCopy(d,r,"Restored");assertNotEquals(e.id,restored.employees.getFirst().id);assertEquals(restored.posts.getFirst().id,restored.cell(restored.employees.getFirst().id,ym.atDay(1)).value);assertTrue(restored.month(ym).lockedDays.contains(1));assertEquals(2,d.teams.size());
 }
 @Test void availabilityOverridesAndOvernightBoundaries(){
  Employee e=new Employee("Name");e.availableWeekdays[1]=false;LocalDate monday=LocalDate.of(2026,9,7);Post night=new Post("Night","22:00","06:00"),midnight=new Post("Late","16:00","00:00");
  assertFalse(Availability.allows(e,night,monday));assertTrue(Availability.allows(e,midnight,monday));e.availabilityOverrides=Availability.parse("2026-09-08=1\n2026-09-09=0");assertTrue(Availability.allows(e,night,monday));assertFalse(Availability.day(e,monday.plusDays(2)));assertThrows(IllegalArgumentException.class,()->Availability.parse("2026-09-08=2"));assertThrows(IllegalArgumentException.class,()->Availability.parse("2026-09-08=0\n2026-09-08=1"));
 }
 @Test void generatorHonorsAvailabilityButKeepsManualException(){
  Data d=new Data();Team t=TeamService.create(d,"Team");Post p=new Post("Day","08:00","16:00");t.posts.add(p);Employee e=new Employee("Name");t.employees.add(e);e.skills.add(p.id);Arrays.fill(e.availableWeekdays,false);YearMonth ym=YearMonth.of(2026,9);t.month(ym).cells.put(key(e.id,1),new Cell(p.id,true));List<String> notes=new Scheduler().generate(d,t,ym);assertEquals(1,Scheduler.workCount(t,e,ym));assertTrue(notes.stream().anyMatch(s->s.contains("εκτός διαθεσιμότητας")));
 }
 @Test void legacyDataDefaultsToAvailableAndEmptyHistory()throws Exception{
  Path path=temp.resolve("data.json");Files.writeString(path,"{\"version\":1,\"leaves\":{},\"teams\":[{\"name\":\"Old\",\"employees\":[{\"name\":\"Person\",\"skills\":[]}],\"months\":{},\"posts\":[]}]}");Team t=new Storage(path).load().teams.getFirst();assertTrue(t.history.isEmpty());assertTrue(Availability.day(t.employees.getFirst(),LocalDate.now()));assertNotNull(t.employees.getFirst().availabilityOverrides);
 }
}
