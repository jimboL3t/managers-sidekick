package gr.sidekick;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class FairnessTest {
 private final YearMonth month=YearMonth.of(2026,9);
 private Team team(Data d){Team t=TeamService.create(d,"Team");t.preferPaired=false;Post night=new Post("Night","08:00","16:00");night.nightDuty=true;t.posts.add(night);t.posts.add(new Post("Day","08:00","16:00"));for(int i=0;i<4;i++){Employee e=new Employee("Person "+i);t.employees.add(e);t.posts.forEach(p->e.skills.add(p.id));}return t;}
 @Test void strongerPreferenceImprovesBalanceWithoutSacrificingCoverage(){
  Data d=new Data();Team plain=team(d);new Scheduler().generate(d,plain,month,42);Team balanced=new Gson().fromJson(new Gson().toJson(plain),Team.class);balanced.nightBalanceWeight=10;balanced.weekendBalanceWeight=10;
  plain.nightBalanceWeight=10;plain.weekendBalanceWeight=10;double before=new Fairness.Context(plain,month).penalty();new Scheduler().generate(d,balanced,month,42);
  assertTrue(new Fairness.Context(balanced,month).penalty()<before,"Expected a measurable improvement from "+before+" to "+new Fairness.Context(balanced,month).penalty());assertTrue(CalendarRules.uncoveredDays(balanced,month).size()<=CalendarRules.uncoveredDays(plain,month).size());
  for(Employee e:balanced.employees)assertEquals(Scheduler.workTarget(balanced,e,month),Scheduler.workCount(balanced,e,month));
 }
 @Test void restrictionsPreserveManualExceptionsAndDoNotMeanExclusion(){
  Data d=new Data();Team t=team(d);Employee e=t.employees.getFirst();e.noNights=true;e.noWeekends=true;t.nightBalanceWeight=5;t.weekendBalanceWeight=5;
  t.month(month).lockedDays.add(2);
  t.month(month).cells.put(key(e.id,5),new Cell(t.posts.getFirst().id,true));new Scheduler().generate(d,t,month);
  assertTrue(t.cell(e.id,month.atDay(5)).locked);for(Employee person:t.employees)assertNull(t.cell(person.id,month.atDay(2)));
  for(int day=1;day<=30;day++){Cell c=t.cell(e.id,month.atDay(day));Post p=c==null?null:t.post(c.value);if(p!=null&&day!=5)assertTrue(Fairness.allowed(e,p,month.atDay(day)));}
  e.noNights=false;e.noWeekends=false;e.excludeNightBalance=true;assertTrue(Fairness.allowed(e,t.posts.getFirst(),month.atDay(5)));assertFalse(Fairness.included(e,true));assertTrue(Fairness.included(e,false));
 }
 @Test void historyAndOpportunityTargetsAreReadOnlyAndExcludeNonparticipants(){
  Data d=new Data();Team t=team(d);Employee a=t.employees.get(0),b=t.employees.get(1);t.employees.removeLast();t.employees.removeLast();t.fairnessLookback=1;t.nightBalanceWeight=5;
  t.month(month.minusMonths(1)).cells.put(key(a.id,1),new Cell(t.posts.getFirst().id,true));
  t.month(month).cells.put(key(a.id,1),new Cell(t.posts.getFirst().id,true));
  Fairness.Context c=new Fairness.Context(t,month);assertEquals(2,c.actual(a,true));assertEquals(1,c.target(a,true),0.001);assertTrue(c.marginal(a,t.posts.getFirst(),month.atDay(2))>c.marginal(b,t.posts.getFirst(),month.atDay(2)));
  b.excludeNightBalance=true;c=new Fairness.Context(t,month);assertEquals(2,c.target(a,true),0.001);assertEquals(0,c.target(b,true));
  String before=new Gson().toJson(t);Fairness.summary(t,month);assertEquals(before,new Gson().toJson(t));
 }
 @Test void capacityAdjustsForAvailabilityAndNightClassificationIsExplicit(){
  Data d=new Data();Team t=team(d);Employee a=t.employees.getFirst();a.availableWeekdays[0]=false;Fairness.Context c=new Fairness.Context(t,month);assertTrue(c.capacity(a,true)<c.capacity(t.employees.get(1),true));
  Post overnight=new Post("Unmarked","22:00","06:00");t.posts.add(overnight);t.month(month).cells.put(key(a.id,1),new Cell(overnight.id,true));assertEquals(0,Fairness.count(t,a,month,true));overnight.nightDuty=true;assertEquals(1,Fairness.count(t,a,month,true));assertTrue(Fairness.weekend(LocalDate.of(2026,9,5)));assertFalse(Fairness.weekend(LocalDate.of(2026,9,4)));
 }
}
