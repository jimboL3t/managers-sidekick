// SPDX-License-Identifier: GPL-3.0-only
package gr.sidekick;
import javax.swing.*;
import java.time.YearMonth;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;
class CalendarZoomTest {
 @Test void independentAxesRemainAlignedAndDoNotModifySchedule()throws Exception{
  SwingUtilities.invokeAndWait(()->{
   try{
    Theme.install();UiScale.set(100);Data data=new Data();Team t=new Team("Demo");Employee e=new Employee("Employee");t.employees.add(e);
    YearMonth month=YearMonth.of(2026,10);Cell fixed=new Cell(OFF,true);t.month(month).cells.put(key(e.id,1),fixed);
    MonthTable grid=new MonthTable(()->t,()->month,Set::of);
    grid.setModel(new ScheduleTableModel(data,t,month,()->fail("Zoom must not save"),()->true));
    JScrollPane scroll=new JScrollPane(grid);FrozenNames names=new FrozenNames(grid,scroll);CalendarZoom zoom=new CalendarZoom(grid,names);
    float originalFont=grid.getFont().getSize2D();zoom.setZoom(50,150);
    assertEquals(52,grid.getColumnModel().getColumn(1).getWidth());assertEquals(57,grid.getRowHeight());assertEquals(57,names.table().getRowHeight());
    assertEquals(170,names.table().getPreferredScrollableViewportSize().width);
    assertEquals(originalFont*.5f,grid.getFont().getSize2D());assertTrue(grid.getFont().getTransform().isIdentity());
    var model=grid.getModel();zoom.applyZoom();assertSame(model,grid.getModel());assertSame(fixed,t.cell(e.id,month.atDay(1)));
    UiScale.set(50);zoom.applyZoom();assertEquals(26,grid.getColumnModel().getColumn(1).getWidth());assertEquals(29,grid.getRowHeight());
    zoom.setZoom(100,100);assertEquals(52,grid.getColumnModel().getColumn(1).getWidth());assertEquals(19,grid.getRowHeight());
    UiScale.set(100);zoom.applyZoom();assertEquals(104,grid.getColumnModel().getColumn(1).getWidth());assertEquals(38,grid.getRowHeight());
    assertEquals(0,grid.getColumnModel().getColumn(0).getWidth());assertSame(grid.getSelectionModel(),names.table().getSelectionModel());
   }catch(Exception ex){throw new RuntimeException(ex);}finally{UiScale.set(100);}
  });
 }
 @Test void applicationSupportsZoomOutAndRejectsInvalidScale(){
  assertEquals(50,UiScale.normalize(50));assertEquals(75,UiScale.normalize(75));assertEquals(100,UiScale.normalize(0));
 }
}
