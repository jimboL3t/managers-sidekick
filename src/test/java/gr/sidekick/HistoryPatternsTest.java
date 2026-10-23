// SPDX-License-Identifier: GPL-3.0-only
package gr.sidekick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import javax.swing.*;
import com.google.gson.Gson;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class HistoryPatternsTest {
 @TempDir Path temp;
 final YearMonth past=YearMonth.of(2026,9),next=past.plusMonths(1);
 Team team(){Team t=new Team("Test");t.posts.add(new Post("Morning","08:00","16:00"));t.posts.add(new Post("Afternoon","16:00","23:00"));Employee e=new Employee("One");t.posts.forEach(p->e.skills.add(p.id));t.employees.add(e);return t;}
 void fill(Team t,int days){for(int d=1;d<=days;d++)t.month(past).cells.put(key(t.employees.getFirst().id,d),new Cell(t.posts.getFirst().id,false));}
 @Test void onlyApprovedPastWithinWindowAndImmutableSnapshots(){
  Team t=team();Employee e=t.employees.getFirst();fill(t,10);t.patternsEnabled=true;
  assertEquals(0,new HistoryPatterns.Context(t,next).samples(e));
  t.month(past).holidays.add(1);t.month(past).cells.put(key(e.id,2),new Cell(OFF,true));
  t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));assertEquals(8,new HistoryPatterns.Context(t,next).samples(e));
  t.month(past).cells.clear();assertEquals(8,new HistoryPatterns.Context(t,next).samples(e));
  assertEquals(0,new HistoryPatterns.Context(t,past).samples(e));t.patternLookback=1;
  assertEquals(0,new HistoryPatterns.Context(t,next.plusMonths(1)).samples(e));
 }
 @Test void preferencesRespectOptOutDisabledAndMinimumEvidence(){
  Team t=team();Employee e=t.employees.getFirst();fill(t,10);t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));
  HistoryPatterns.Context context=new HistoryPatterns.Context(t,next);
  assertEquals(0,context.preference(e,t.posts.getFirst(),next.atDay(1)));t.patternsEnabled=true;
  assertTrue(context.preference(e,t.posts.getFirst(),next.atDay(1))<context.preference(e,t.posts.getLast(),next.atDay(1)));
  t.patternExcludedEmployees.add(e.id);assertEquals(0,context.preference(e,t.posts.getFirst(),next.atDay(1)));
  t.patternExcludedEmployees.clear();t.month(past).cells.clear();fill(t,4);t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));assertFalse(new HistoryPatterns.Context(t,next).active(e));
 }
 @Test void secondOrderTransitionsDetectRotationWithoutBridgingGaps(){
  Team t=team();t.patternsEnabled=true;Employee e=t.employees.getFirst();Post morning=t.posts.getFirst(),afternoon=t.posts.getLast();
  for(int d=1;d<=30;d++){int index=(d-1)%5;t.month(past).cells.put(key(e.id,d),new Cell(index<2?morning.id:index<4?afternoon.id:OFF,false));}
  t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));
  t.month(next).cells.put(key(e.id,1),new Cell(morning.id,true));t.month(next).cells.put(key(e.id,2),new Cell(morning.id,true));
  HistoryPatterns.Context c=new HistoryPatterns.Context(t,next);
  assertTrue(c.preference(e,afternoon,next.atDay(3))<c.preference(e,morning,next.atDay(3)));
  t.month(next).cells.put(key(e.id,2),new Cell(OFF,true));
  assertEquals(c.preference(e,afternoon,next.atDay(3)),c.preference(e,morning,next.atDay(3)),.00001);
  t.month(next).holidays.add(3);assertEquals(0,c.preference(e,afternoon,next.atDay(3)));
 }
 @Test void savesCopiesAndRestoresLegacyHistory()throws Exception{
  Data data=new Data();Team t=team();data.teams.add(t);fill(t,10);t.patternsEnabled=true;t.patternExcludedEmployees.add(t.employees.getFirst().id);t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));
  Storage storage=new Storage(temp.resolve("data.json"));storage.save(data);Team loaded=storage.load().teams.getFirst();assertTrue(loaded.patternsEnabled);assertEquals(10,new HistoryPatterns.Context(loaded,next).samples(loaded.employees.getFirst()));
  Team copy=TeamService.duplicate(data,t,"Copy");Employee other=copy.employees.getFirst();assertEquals(10,new HistoryPatterns.Context(copy,next).samples(other));assertTrue(copy.patternExcludedEmployees.contains(other.id));
  copy.patternMonths.get(past.toString()).shifts.get(other.id)[0]=-1;assertEquals(10,new HistoryPatterns.Context(t,next).samples(t.employees.getFirst()));
  Revision revision=History.capture(t,past,"old");var json=com.google.gson.JsonParser.parseString(revision.snapshot).getAsJsonObject();json.remove("patternMonths");json.remove("patternExcludedEmployees");json.remove("patternLookback");revision.snapshot=json.toString();
  assertNotNull(History.restoreCopy(data,revision,"Legacy").patternMonths);
 }
 @Test void schedulerKeepsMandatoryCoverageAndFixedChoices(){
  Data data=new Data();Team t=team();data.teams.add(t);Employee e=t.employees.getFirst();
  for(int d=1;d<=30;d++)t.month(past).cells.put(key(e.id,d),new Cell(t.posts.getLast().id,false));
  t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));t.patternsEnabled=true;t.patternStrength=3;t.posts.getLast().optional=true;
  Cell fixed=new Cell(OFF,true);t.month(next).cells.put(key(e.id,2),fixed);new Scheduler().generate(data,t,next);
  assertSame(fixed,t.cell(e.id,next.atDay(2)));
  assertTrue(t.month(next).cells.values().stream().noneMatch(c->c.value.equals(t.posts.getLast().id)));
 }
 @Test void learnedPreferenceActuallyChangesGeneratedSchedule(){
  Data data=new Data();Team t=team();data.teams.add(t);fill(t,20);t.posts.forEach(p->p.optional=true);t.preferPaired=false;
  t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));Employee e=t.employees.getFirst();
  new Scheduler().generate(data,t,next);int before=CalendarRules.count(t,e,next,t.posts.getFirst().id);
  t.patternsEnabled=true;t.patternStrength=3;new Scheduler().generate(data,t,next);
  assertTrue(CalendarRules.count(t,e,next,t.posts.getFirst().id)>before);
 }
 @Test void enablingWithoutEvidenceDoesNotChangeSchedule(){
  Data data=new Data();Team t=team();Team copy=new Gson().fromJson(new Gson().toJson(t),Team.class);copy.patternsEnabled=true;
  new Scheduler().generate(data,t,next);new Scheduler().generate(data,copy,next);
  assertEquals(new Gson().toJson(t.month(next).cells),new Gson().toJson(copy.month(next).cells));
 }
 @Test void learnedNightsNeverOverrideAvailabilityRestSkillsOrLocks(){
  for(long seed:new long[]{1,42,99}){
   Data data=new Data();Team t=team();Post night=t.posts.getLast();night.start="22:00";night.end="06:00";night.nightDuty=true;
   Employee first=t.employees.getFirst();first.noNights=true;first.availableWeekdays[6]=false;
   for(int i=0;i<2;i++){Employee e=new Employee("Other "+i);t.posts.forEach(p->e.skills.add(p.id));t.employees.add(e);}
   for(Employee e:t.employees)for(int day=1;day<=20;day++)t.month(past).cells.put(key(e.id,day),new Cell(night.id,false));
   t.patternMonths.put(past.toString(),HistoryPatterns.capture(t,past));t.patternsEnabled=true;t.patternStrength=3;
   Cell locked=new Cell(OFF,true);t.month(next).cells.put(key(first.id,1),locked);t.month(next).lockedDays.add(3);
   new Scheduler().generate(data,t,next,seed);assertSame(locked,t.cell(first.id,next.atDay(1)));
   for(Employee e:t.employees){int run=0;Post previous=null;LocalDate previousDay=null;
    for(int day=1;day<=next.lengthOfMonth();day++){LocalDate date=next.atDay(day);Cell c=t.cell(e.id,date);Post p=c==null?null:t.post(c.value);
     if(day==3)assertNull(c);
     if(p==null){run=0;continue;}
     assertTrue(++run<=t.maxConsecutive);assertTrue(e.skills.contains(p.id));assertTrue(Availability.allows(e,p,date));assertTrue(Fairness.allowed(e,p,date));
     if(previous!=null)assertTrue(Duration.between(previous.endAt(previousDay),p.startAt(date)).toMinutes()>=t.minRestHours*60L);
     previous=p;previousDay=date;
    }
   }
  }
 }
 @Test void editingEarlierMonthPreservesHiddenApprovals()throws Exception{
  SwingUtilities.invokeAndWait(()->{try{Theme.install();Team t=team();fill(t,10);PatternMonth snapshot=HistoryPatterns.capture(t,past);t.patternMonths.put(past.toString(),snapshot);
   PatternPanel panel=new PatternPanel(t,past);panel.save();assertSame(snapshot,t.patternMonths.get(past.toString()));
   PatternPanel later=new PatternPanel(t,next);checkbox(later,past.toString()).setSelected(false);later.save();assertFalse(t.patternMonths.containsKey(past.toString()));
  }catch(Exception e){throw new RuntimeException(e);}});
 }
 private static JCheckBox checkbox(java.awt.Container c,String label){for(var child:c.getComponents()){if(child instanceof JCheckBox box&&box.getText().equals(label))return box;if(child instanceof java.awt.Container nested){JCheckBox result=checkbox(nested,label);if(result!=null)return result;}}return null;}
 private static void layout(java.awt.Container c){c.doLayout();for(var child:c.getComponents())if(child instanceof java.awt.Container nested)layout(nested);}
 @Test void panelPreviewDoesNotMutateTeam()throws Exception{
  SwingUtilities.invokeAndWait(()->{try{Theme.install();Team t=team();fill(t,10);String before=new Gson().toJson(t);PatternPanel panel=new PatternPanel(t,next);assertEquals(before,new Gson().toJson(t));checkbox(panel,past.toString()).setSelected(true);assertEquals(before,new Gson().toJson(t));panel.setSize(1150,650);layout(panel);assertTrue(java.util.Arrays.stream(panel.getComponents()).anyMatch(c->c instanceof JSplitPane&&c.getHeight()>100));var image=new java.awt.image.BufferedImage(1150,650,java.awt.image.BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();panel.printAll(g);g.dispose();javax.imageio.ImageIO.write(image,"png",Path.of("target/patterns-preview.png").toFile());}catch(Exception e){throw new RuntimeException(e);}});
 }
}
