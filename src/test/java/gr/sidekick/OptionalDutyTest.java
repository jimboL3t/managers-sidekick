package gr.sidekick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class OptionalDutyTest {
 @TempDir Path temp;
 @Test void optionalNeverTakesMandatoryCapacityAndPersists()throws Exception{
  Data data=new Data();Team t=new Team("Test");data.teams.add(t);
  Post optional=new Post("Optional","08:00","16:00");optional.optional=true;
  Post required=new Post("Required","08:00","16:00");t.posts.add(optional);t.posts.add(required);
  Employee e=new Employee("One");e.skills.add(optional.id);e.skills.add(required.id);t.employees.add(e);
  YearMonth m=YearMonth.of(2026,9);new Scheduler().generate(data,t,m);
  assertTrue(t.month(m).cells.values().stream().noneMatch(c->optional.id.equals(c.value)));
  assertTrue(Scheduler.workCount(t,e,m)>0);
  t.posts.remove(required);t.month(m).cells.clear();
  assertTrue(CalendarRules.uncoveredDays(t,m).isEmpty());
  new Scheduler().generate(data,t,m);assertTrue(Scheduler.workCount(t,e,m)>0);
  Storage storage=new Storage(temp.resolve("test.json"));storage.save(data);
  assertTrue(storage.load().teams.getFirst().posts.getFirst().optional);
 }
 @Test void tenConsecutiveAllowedButElevenRejected(){
  Data data=new Data();Team t=new Team("Test");data.teams.add(t);Post p=new Post("Duty","08:00","16:00");t.posts.add(p);
  Employee e=new Employee("One");e.skills.add(p.id);t.employees.add(e);YearMonth m=YearMonth.of(2026,9);
  assertEquals(5,t.maxConsecutive);t.maxConsecutive=10;
  for(int d=1;d<=10;d++)t.month(m).cells.put(key(e.id,d),new Cell(p.id,true));
  Scheduler scheduler=new Scheduler();
  assertFalse(scheduler.validate(data,t,m).stream().anyMatch(s->s.contains("ανάπαυση")));
  t.month(m).cells.put(key(e.id,11),new Cell(p.id,true));
  assertTrue(scheduler.validate(data,t,m).stream().anyMatch(s->s.contains("ανάπαυση")));
 }
 @Test void quickGuideHasBothLanguages()throws Exception{
  Path pdf=temp.resolve("guide.pdf");QuickStartGuide.export(pdf);
  try(var doc=org.apache.pdfbox.Loader.loadPDF(pdf.toFile())){
   assertEquals(2,doc.getNumberOfPages());String text=new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
   assertTrue(text.contains("Quick start"));assertTrue(text.contains("Γρήγορη εκκίνηση"));assertTrue(text.contains("managersSidekick.exe"));
  }
 }
}
