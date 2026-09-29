// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class DayLocksTest {
 @Test void preservesWholeDayIncludingEmptyCellsAcrossSeeds(){
  Data data=new Data();Team t=TeamService.create(data,"Team");Post p=new Post("Duty","08:00","16:00");t.posts.add(p);YearMonth ym=YearMonth.of(2026,9);
  for(int i=0;i<4;i++){Employee e=new Employee("Person"+i);e.skills.add(p.id);t.employees.add(e);}
  Employee a=t.employees.get(0),b=t.employees.get(1);t.month(ym).lockedDays.addAll(Set.of(1,2));
  t.month(ym).cells.put(key(a.id,1),new Cell(p.id,false));t.month(ym).cells.put(key(b.id,1),new Cell(OFF,true));t.month(ym).cells.put(key(a.id,3),new Cell(OFF,true));
  Scheduler scheduler=new Scheduler();
  for(long seed: new long[]{42,55,77}){scheduler.generate(data,t,ym,seed);assertEquals(p.id,t.cell(a.id,ym.atDay(1)).value);assertFalse(t.cell(a.id,ym.atDay(1)).locked);assertNull(t.cell(a.id,ym.atDay(2)));assertNull(t.cell(t.employees.get(2).id,ym.atDay(1)));assertEquals(OFF,t.cell(a.id,ym.atDay(3)).value);}
  Scheduler.clearManual(t,ym);assertEquals(OFF,t.cell(b.id,ym.atDay(1)).value);assertNull(t.cell(a.id,ym.atDay(3)));
  ScheduleTableModel model=new ScheduleTableModel(data,t,ym,()->{},()->true);assertFalse(model.isCellEditable(0,1));assertTrue(model.isCellEditable(0,3));
  Team copy=TeamService.duplicate(data,t,"Copy");copy.month(ym).lockedDays.clear();assertEquals(Set.of(1,2),t.month(ym).lockedDays);
 }
}
