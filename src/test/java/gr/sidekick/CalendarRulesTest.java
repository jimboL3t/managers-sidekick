// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class CalendarRulesTest {
    @TempDir Path temp;

    @Test void countsActualWeekendDatesAcrossMonthEdgesAndLeapYears() {
        assertEquals(8,CalendarRules.weekendDays(YearMonth.of(2026,2)));
        assertEquals(9,CalendarRules.weekendDays(YearMonth.of(2026,10)));
        assertEquals(10,CalendarRules.weekendDays(YearMonth.of(2026,5)));
        assertEquals(8,CalendarRules.weekendDays(YearMonth.of(2024,2)));
    }

    @Test void twoHolidaysGiveEveryoneTenRestDaysEvenWhenOneIsOnWeekend() {
        Team t=new Team("Ομάδα");YearMonth ym=YearMonth.of(2026,9);
        t.month(ym).holidays.addAll(Set.of(5,8));
        assertEquals(10,CalendarRules.offTarget(t,ym));
        assertEquals(9,CalendarRules.offTarget(t,ym.plusMonths(1)));
    }

    @Test void holidaysRemainWorkingDaysWithNormalCoverage() {
        Data data=new Data();Team t=new Team("Ομάδα");YearMonth ym=YearMonth.of(2026,9);
        Post p=new Post("Πρωί","08:00","16:00");t.posts.add(p);
        for(int i=0;i<2;i++){Employee e=new Employee("Άτομο "+i);e.skills.add(p.id);t.employees.add(e);}
        t.month(ym).holidays.addAll(Set.of(5,8));
        // Both work and rest on a holiday are valid locked choices.
        Employee first=t.employees.getFirst(),second=t.employees.getLast();
        t.month(ym).cells.put(key(first.id,5),new Cell(p.id,true));
        t.month(ym).cells.put(key(second.id,5),new Cell(OFF,true));
        new Scheduler().generate(data,t,ym);
        assertTrue(CalendarRules.uncoveredDays(t,ym).isEmpty());
        for(Employee e:t.employees)assertEquals(10,CalendarRules.count(t,e,ym,OFF));
        assertEquals(p.id,t.cell(first.id,ym.atDay(5)).value);
        assertEquals(OFF,t.cell(second.id,ym.atDay(5)).value);
    }

    @Test void shortageLeavesPartialScheduleAndCorrectMonthlyRestBudget() {
        Team t=new Team("Ομάδα");YearMonth ym=YearMonth.of(2026,9);
        Post p=new Post("Πρωί","08:00","16:00");t.posts.add(p);
        Employee e=new Employee("Άτομο");e.skills.add(p.id);t.employees.add(e);
        t.month(ym).holidays.addAll(Set.of(5,8));
        List<String> notes=new Scheduler().generate(new Data(),t,ym);
        assertEquals(10,CalendarRules.count(t,e,ym,OFF));
        assertEquals(20,CalendarRules.count(t,e,ym,p.id));
        assertEquals(10,CalendarRules.uncoveredDays(t,ym).size());
        assertFalse(notes.stream().anyMatch(s->s.contains("δεξιότητα")));
    }

    @Test void approvedLeaveDoesNotConsumeRestEntitlement() {
        Data data=new Data();Team t=new Team("Ομάδα");YearMonth ym=YearMonth.of(2026,9);
        Post p=new Post("Πρωί","08:00","16:00");t.posts.add(p);Employee e=new Employee("Άτομο");e.skills.add(p.id);t.employees.add(e);
        String leave=data.leaves.keySet().stream().filter(id->!OFF.equals(id)).findFirst().orElseThrow();t.allowedLeaves.add(leave);
        t.month(ym).cells.put(key(e.id,1),new Cell(leave,true));
        new Scheduler().generate(data,t,ym);
        assertEquals(8,CalendarRules.count(t,e,ym,OFF));assertEquals(1,CalendarRules.count(t,e,ym,leave));assertEquals(21,CalendarRules.count(t,e,ym,p.id));
    }

    @Test void holidaysPersistAndOldJsonNeedsNoMigration() throws Exception {
        Storage storage=new Storage(temp.resolve("schedule.json"));
        Files.writeString(temp.resolve("schedule.json"),"{\"version\":1,\"leaves\":{\"off\":\"Ρεπό\"},\"teams\":[{\"name\":\"Ομάδα\",\"months\":{\"2026-09\":{\"cells\":{}}}}]}");
        Data data=storage.load();Team t=data.teams.getFirst();YearMonth ym=YearMonth.of(2026,9);
        assertTrue(t.month(ym).holidays.isEmpty());t.month(ym).holidays.add(8);storage.save(data);
        assertTrue(CalendarRules.holiday(storage.load().teams.getFirst(),ym.atDay(8)));
    }
}
