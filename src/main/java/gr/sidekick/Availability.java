package gr.sidekick;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
/** Whole-day availability; a shift must fit every calendar day it touches. */
public final class Availability {
 private Availability(){}
 public static boolean day(Employee e,LocalDate date){boolean weekly=e.availableWeekdays==null||e.availableWeekdays[date.getDayOfWeek().getValue()-1];return e.availabilityOverrides==null?weekly:e.availabilityOverrides.getOrDefault(date.toString(),weekly);}
 public static boolean allows(Employee e,Post p,LocalDate date){
  LocalDate last=p.endAt(date).minusNanos(1).toLocalDate();
  for(LocalDate d=date;!d.isAfter(last);d=d.plusDays(1))if(!day(e,d))return false;
  return true;
 }
 public static Map<String,Boolean> parse(String text){
  Map<String,Boolean> result=new TreeMap<>();
  for(String line:text.split("\\R")){if(line.isBlank())continue;String[] parts=line.strip().split("=",-1);
   if(parts.length!=2||!Set.of("0","1").contains(parts[1].strip()))throw new IllegalArgumentException(I18n.text("Χρησιμοποιήστε YYYY-MM-DD=0 ή YYYY-MM-DD=1."));
   String date=LocalDate.parse(parts[0].strip()).toString();if(result.put(date,parts[1].strip().equals("1"))!=null)throw new IllegalArgumentException(I18n.text("Διπλή ημερομηνία: ")+date);
  }return result;
 }
}
