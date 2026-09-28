package gr.sidekick;

import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;

/** Calendar calculations shared by the scheduler and the interface. */
public final class CalendarRules {
    private CalendarRules() {}

    public static boolean weekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public static boolean holiday(Team team, LocalDate date) {
        Model.Month month = team.months.get(YearMonth.from(date).toString());
        return month != null && month.holidays.contains(date.getDayOfMonth());
    }

    public static int weekendDays(YearMonth month) {
        int count = 0;
        for (int day = 1; day <= month.lengthOfMonth(); day++) if (weekend(month.atDay(day))) count++;
        return count;
    }

    public static int offTarget(Team team, YearMonth month) {
        int count = weekendDays(month);
        // Each declared holiday adds one rest day, including holidays on weekends.
        for (int day = 1; day <= month.lengthOfMonth(); day++)
            if (holiday(team, month.atDay(day))) count++;
        return count;
    }

    public static int count(Team team, Employee employee, YearMonth month, String value) {
        int count = 0;
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            Cell cell = team.cell(employee.id, month.atDay(day));
            if (cell != null && value.equals(cell.value)) count++;
        }
        return count;
    }

    public static Set<Integer> uncoveredDays(Team team, YearMonth month) {
        Set<Integer> days = new LinkedHashSet<>();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            for (Post post : team.posts) {
                long count = team.employees.stream().filter(e -> {
                    Cell c = team.cell(e.id, date);
                    return c != null && post.id.equals(c.value);
                }).count();
                if (count < post.required(date)) days.add(day);
            }
        }
        return days;
    }
}
