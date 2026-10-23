// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;

/** Explainable time-band frequencies and adjacent-day transitions from approved snapshots. */
public final class HistoryPatterns {
 private HistoryPatterns(){}
 public static void normalize(Team t){if(t.patternMonths==null)t.patternMonths=new TreeMap<>();if(t.patternExcludedEmployees==null)t.patternExcludedEmployees=new LinkedHashSet<>();if(t.patternLookback<1)t.patternLookback=6;if(t.patternStrength<1)t.patternStrength=2;}
 // Explicit night marking wins; other duties are classified by their start time.
 public static int band(Post p){int h=LocalTime.parse(p.start).getHour();return p.nightDuty||h>=22||h<6?2:h<14?0:1;}
 public static String bandName(int band){return I18n.text(new String[]{"Πρωί","Απόγευμα","Νύχτα"}[band]);}
 public static PatternMonth capture(Team t,YearMonth month){
  PatternMonth snapshot=new PatternMonth();
  for(Employee e:t.employees){int[] days=new int[month.lengthOfMonth()];Arrays.fill(days,-1);
   for(int d=1;d<=days.length;d++){LocalDate date=month.atDay(d);Cell c=t.cell(e.id,date);Post p=c==null?null:t.post(c.value);
    if(p!=null&&!CalendarRules.holiday(t,date))days[d-1]=band(p);
   }snapshot.shifts.put(e.id,days);
  }return snapshot;
 }
 private static final class Counts {
  int[] bands=new int[3];int[][] pairs=new int[3][3];int[][][] triples=new int[3][3][3];int total;
 }
 public static final class Context {
  private final Team team;private final YearMonth target;private final Map<String,Counts> learned=new HashMap<>();
  private int months;
  public Context(Team team,YearMonth target){
   normalize(team);this.team=team;this.target=target;
   for(Employee e:team.employees)learned.put(e.id,new Counts());
   for(int back=1;back<=Math.max(1,Math.min(12,team.patternLookback));back++){
    PatternMonth approved=team.patternMonths.get(target.minusMonths(back).toString());if(approved==null)continue;months++;
    for(Employee e:team.employees){int[] days=approved.shifts.get(e.id);if(days==null)continue;Counts c=learned.get(e.id);
     for(int i=0;i<days.length;i++){int b=days[i];if(!valid(b))continue;c.bands[b]++;c.total++;
      if(i>0&&valid(days[i-1]))c.pairs[days[i-1]][b]++;
      if(i>1&&valid(days[i-1])&&valid(days[i-2]))c.triples[days[i-2]][days[i-1]][b]++;
     }
    }
   }
  }
  private static boolean valid(int b){return b>=0&&b<3;}
  public int samples(Employee e){Counts c=learned.get(e.id);return c==null?0:c.total;}
  public boolean active(Employee e){return team.patternsEnabled&&!team.patternExcludedEmployees.contains(e.id)&&samples(e)>=5;}
  private int assignedBand(Employee e,LocalDate d){
   if(CalendarRules.holiday(team,d))return -1;
   Cell c=team.cell(e.id,d);Post p=c==null?null:team.post(c.value);return p==null?-1:band(p);
  }
  private double score(Employee e,Post p,LocalDate day){
   if(!active(e)||CalendarRules.holiday(team,day))return 0;
   Counts c=learned.get(e.id);int b=band(p);double cost=-Math.log((c.bands[b]+1d)/(c.total+3d));
   int previous=assignedBand(e,day.minusDays(1)),before=assignedBand(e,day.minusDays(2));
   if(valid(previous)){
    int[] transitions=valid(before)?c.triples[before][previous]:c.pairs[previous];int sum=Arrays.stream(transitions).sum();
    if(sum<3){transitions=c.pairs[previous];sum=Arrays.stream(transitions).sum();}
    if(sum>=3)cost-=Math.log((transitions[b]+1d)/(sum+3d));
   }return Math.max(1,Math.min(3,team.patternStrength))*cost;
  }
  public double preference(Employee e,Post p,LocalDate day){return score(e,p,day);}
  public double penalty(){if(!team.patternsEnabled)return 0;double total=0;for(Employee e:team.employees)for(int d=1;d<=target.lengthOfMonth();d++){
   LocalDate date=target.atDay(d);Cell c=team.cell(e.id,date);Post p=c==null?null:team.post(c.value);if(p!=null)total+=score(e,p,date);
  }return total;}
  public String summary(){
   StringBuilder s=new StringBuilder(I18n.text(team.patternsEnabled?"Πρότυπα: ενεργά\n":"Πρότυπα: ανενεργά (προεπισκόπηση μόνο)\n")).append(I18n.text("Εγκεκριμένοι μήνες εντός περιόδου: ")).append(months).append('\n');
   s.append(I18n.text("Πρωί 06:00–13:59 · Απόγευμα 14:00–21:59 · Νύχτα 22:00–05:59 ή νυχτερινή υπηρεσία.\n"));
   for(Employee e:team.employees){Counts c=learned.get(e.id);s.append(e.name).append(" · ").append(c.total).append(I18n.text(" δείγματα"));
    if(team.patternExcludedEmployees.contains(e.id))s.append(I18n.text(" · εκτός προτύπων"));
    else if(c.total<5)s.append(I18n.text(" · χρειάζονται τουλάχιστον 5 βάρδιες"));
    else for(int b=0;b<3;b++)s.append(" · ").append(bandName(b)).append(' ').append(Math.round(100d*c.bands[b]/c.total)).append('%');
    s.append('\n');
    if(c.total>=5&&!team.patternExcludedEmployees.contains(e.id)){
     int best=0,a=-1,b=-1,next=-1;
     for(int i=0;i<3;i++)for(int j=0;j<3;j++)for(int k=0;k<3;k++)if(c.triples[i][j][k]>best){best=c.triples[i][j][k];a=i;b=j;next=k;}
     if(best>=3)s.append("  ").append(bandName(a)).append(" → ").append(bandName(b)).append(" → ").append(bandName(next)).append(" (×").append(best).append(")\n");
    }
   }
   return s.append(I18n.text("Προτιμήσεις, όχι υποχρεώσεις. Κανόνες, κάλυψη και δίκαιη κατανομή προηγούνται.\n")).toString();
  }
 }
}
