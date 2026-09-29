// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import org.junit.jupiter.api.Test;
import java.time.YearMonth;
import java.util.concurrent.atomic.AtomicInteger;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleTableModelTest {
    @Test void manualChoiceChangesOnlySelectedCellAndNotifiesWithoutRecalculation() {
        Data data=new Data();Team t=new Team("Ομάδα");Employee e=new Employee("Άτομο");t.employees.add(e);
        YearMonth ym=YearMonth.of(2026,9);Post p=new Post("Πρωί","08:00","16:00");t.posts.add(p);
        Cell other=new Cell(p.id,false);t.month(ym).cells.put(key(e.id,2),other);
        AtomicInteger calls=new AtomicInteger();
        ScheduleTableModel model=new ScheduleTableModel(data,t,ym,calls::incrementAndGet,()->true);
        model.setValueAt(new ScheduleTableModel.Choice(OFF,"Ρεπό"),0,1);
        assertEquals(1,calls.get());assertSame(other,t.cell(e.id,ym.atDay(2)));
        assertEquals(2,t.month(ym).cells.size());assertTrue(t.cell(e.id,ym.atDay(1)).locked);
        model.setValueAt(new ScheduleTableModel.Choice(null,"Κενό"),0,1);
        assertNull(t.cell(e.id,ym.atDay(1)));assertSame(other,t.cell(e.id,ym.atDay(2)));
    }

    @Test void leapMonthHas29DaysAndBusyModelRejectsChanges() {
        Team t=new Team("Ομάδα");t.employees.add(new Employee("Άτομο"));
        ScheduleTableModel model=new ScheduleTableModel(new Data(),t,YearMonth.of(2024,2),()->fail("Unexpected mutation"),()->false);
        assertEquals(30,model.getColumnCount());assertFalse(model.isCellEditable(0,1));
        model.setValueAt(new ScheduleTableModel.Choice(OFF,"Ρεπό"),0,1);
        assertTrue(t.months.isEmpty());
    }
}
