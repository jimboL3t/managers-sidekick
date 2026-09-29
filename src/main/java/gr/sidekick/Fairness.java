package gr.sidekick;

import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;

/** Soft, capacity-proportional objectives. Availability and rest remain hard constraints. */
public final class Fairness {
 private Fairness(){}
 public static boolean weekend(LocalDate d){return d.getDayOfWeek().getValue()>=6;}
 public static boolean allowed(Employee e,Post p,LocalDate d){return !(e.noNights&&p.nightDuty)&&!(e.noWeekends&&weekend(d));}
 public static boolean included(Employee e,boolean night){return night?!e.excludeNightBalance:!e.excludeWeekendBalance;}
 public static int count(Team t,Employee e,YearMonth month,boolean night){
  int n=0;for(int day=1;day<=month.lengthOfMonth();day++){LocalDate d=month.atDay(day);Cell c=t.cell(e.id,d);Post p=c==null?null:t.post(c.value);if(p!=null&&(night?p.nightDuty:weekend(d)))n++;}return n;
 }
 public static final class Context {
  private final Team team;private final YearMonth month;
  private final Map<String,int[]> capacity=new HashMap<>(),previous=new HashMap<>();
  public Context(Team team,YearMonth month){
   this.team=team;this.month=month;
   for(Employee e:team.employees){int[] cap=new int[2],past=new int[2];
    for(int back=0;back<=Math.max(0,Math.min(12,team.fairnessLookback));back++){
     YearMonth m=month.minusMonths(back);
     // Missing months are unknown, not zero work histories.
     if(back>0&&(!team.months.containsKey(m.toString())||team.months.get(m.toString()).cells.isEmpty()))continue;
     for(int k=0;k<2;k++){boolean night=k==0;if(back>0)past[k]+=count(team,e,m,night);
      for(int day=1;day<=m.lengthOfMonth();day++){LocalDate date=m.atDay(day);Cell fixed=team.cell(e.id,date);
       if(fixed!=null&&team.post(fixed.value)==null&&!OFF.equals(fixed.value))continue;
       if(!night&&!weekend(date))continue;
       if(team.posts.stream().anyMatch(p->(!night||p.nightDuty)&&p.operates(date)&&e.skills.contains(p.id)&&Availability.allows(e,p,date)&&allowed(e,p,date)))cap[k]++;
      }
     }
    }capacity.put(e.id,cap);previous.put(e.id,past);
   }
  }
  public int capacity(Employee e,boolean night){return capacity.get(e.id)[night?0:1];}
  public int actual(Employee e,boolean night){return previous.get(e.id)[night?0:1]+count(team,e,month,night);}
  public double target(Employee e,boolean night){double total=0,cap=0;for(Employee other:team.employees)if(included(other,night)&&capacity(other,night)>0){total+=actual(other,night);cap+=capacity(other,night);}return included(e,night)&&cap>0?total*capacity(e,night)/cap:0;}
  private double penalty(boolean night,Employee added){
   double total=0,cap=0;for(Employee e:team.employees)if(included(e,night)&&capacity(e,night)>0){total+=actual(e,night)+(e==added?1:0);cap+=capacity(e,night);}
   if(cap==0)return 0;double score=0;
   for(Employee e:team.employees)if(included(e,night)&&capacity(e,night)>0){double diff=actual(e,night)+(e==added?1:0)-total*capacity(e,night)/cap;score+=diff*diff;}
   return score;
  }
  public double penalty(){return team.nightBalanceWeight*penalty(true,null)+team.weekendBalanceWeight*penalty(false,null);}
  public double marginal(Employee e,Post p,LocalDate day){double score=0;
   if(p.nightDuty&&included(e,true)&&team.nightBalanceWeight>0)score+=team.nightBalanceWeight*(penalty(true,e)-penalty(true,null));
   if(weekend(day)&&included(e,false)&&team.weekendBalanceWeight>0)score+=team.weekendBalanceWeight*(penalty(false,e)-penalty(false,null));return score;
  }
 }
 public static String summary(Team t,YearMonth m){
  Context c=new Context(t,m);StringBuilder out=new StringBuilder(I18n.text("Κατανομή: πραγματικό / αναλογικός στόχος περιόδου\n"));
  out.append(I18n.text("Προηγούμενοι μήνες: ")).append(t.fairnessLookback).append('\n');
  out.append(I18n.text("Βάρη νύχτας / ΣΚ: ")).append(t.nightBalanceWeight).append(" / ").append(t.weekendBalanceWeight).append('\n');
  int missing=0;for(int back=1;back<=t.fairnessLookback;back++){Model.Month old=t.months.get(m.minusMonths(back).toString());if(old==null||old.cells.isEmpty())missing++;}
  if(missing>0)out.append(I18n.text("Μήνες χωρίς ιστορικό (παραλείπονται): ")).append(missing).append('\n');
  for(Employee e:t.employees){out.append(e.name);for(boolean night:new boolean[]{true,false}){
    out.append(night?I18n.text(" · Νύχτες "):I18n.text(" · Σ/Κ ")).append(c.actual(e,night)).append(" / ");
    if(!included(e,night))out.append(I18n.text("εκτός εξισορρόπησης"));else if(c.capacity(e,night)==0)out.append(I18n.text("χωρίς διαθέσιμες ευκαιρίες"));else out.append(String.format(Locale.ROOT,"%.1f",c.target(e,night)));
   }out.append('\n');}
  out.append(I18n.text("Οι στόχοι είναι ενδεικτικοί, όχι υποχρεωτικές ποσοστώσεις.\n"));return out.toString();
 }
}
