package gr.sidekick;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.time.*;
import java.util.Set;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class DisplayTest {
    @TempDir Path temp;
    @Test void scalePersistsAndLegacyDataDefaultsTo100()throws Exception{
        Storage storage=new Storage(temp.resolve("test.json"));Data data=new Data();data.uiScale=150;storage.save(data);
        assertEquals(150,storage.load().uiScale);
        Files.writeString(temp.resolve("test.json"),"{\"version\":1,\"teams\":[],\"leaves\":{}}");
        assertEquals(100,storage.load().uiScale);
        assertEquals(100,UiScale.normalize(999));
    }
    @Test void scaleIsReversibleWithoutCompounding()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                Theme.install();UiScale.set(100);
                JPanel panel=new JPanel();JButton button=new ActionButton("Save",ActionButton.Style.PRIMARY);panel.add(button);
                JTable table=new JTable(2,3);table.setRowHeight(38);panel.add(table);
                UiScale.apply(panel);float original=button.getFont().getSize2D();Dimension normal=button.getPreferredSize();
                UiScale.set(150);UiScale.apply(panel);assertEquals(original*1.5f,button.getFont().getSize2D());assertEquals(57,table.getRowHeight());assertTrue(button.getPreferredSize().height>normal.height);
                UiScale.apply(panel);assertEquals(original*1.5f,button.getFont().getSize2D());
                UiScale.set(100);UiScale.apply(panel);assertEquals(original,button.getFont().getSize2D());assertEquals(normal,button.getPreferredSize());
            }catch(Exception e){throw new RuntimeException(e);}finally{UiScale.set(100);}
        });
    }
    @Test void scaledCalendarPreview()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                Theme.install();UiScale.set(100);Data data=new Data();Team t=new Team("Demo");YearMonth month=YearMonth.of(2026,10);
                Post post=new Post("Helpdesk","08:00","16:00");t.posts.add(post);
                for(int i=0;i<8;i++){Employee e=new Employee("Employee "+(i+1));t.employees.add(e);for(int d=1;d<=31;d++)t.month(month).cells.put(key(e.id,d),new Cell(d%7<2?OFF:post.id,false));}
                MonthTable table=new MonthTable(()->t,()->month,()->Set.of(8));table.setModel(new ScheduleTableModel(data,t,month,()->{},()->true));
                JScrollPane scroll=new JScrollPane(table);FrozenNames names=new FrozenNames(table,scroll);
                JPanel panel=new JPanel(new BorderLayout(0,12));JPanel buttons=new JPanel();buttons.add(new ActionButton("Save",ActionButton.Style.PRIMARY));buttons.add(new ActionButton("Export PDF",ActionButton.Style.EXPORT));panel.add(buttons,BorderLayout.NORTH);panel.add(scroll);
                for(int scale:new int[]{100,150,200}){
                    UiScale.set(scale);UiScale.apply(panel);for(int i=1;i<table.getColumnCount();i++)table.getColumnModel().getColumn(i).setPreferredWidth(UiScale.px(104));names.refresh();
                    panel.setSize(1440,800);layout(panel);scroll.getViewport().setViewPosition(new Point(400,0));
                    var image=new java.awt.image.BufferedImage(1440,800,java.awt.image.BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();panel.printAll(g);g.dispose();javax.imageio.ImageIO.write(image,"png",Path.of("target","display-"+scale+".png").toFile());
                    assertEquals(UiScale.px(38),names.table().getRowHeight());assertEquals(UiScale.px(340),names.table().getPreferredScrollableViewportSize().width);
                }
            }catch(Exception e){throw new RuntimeException(e);}finally{UiScale.set(100);}
        });
    }
    private static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container nested)layout(nested);}
    @Test void namesStayVisibleDuringScrollAndFollowModelAndSelection()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                Theme.install();UiScale.set(100);Team t=new Team("Demo");YearMonth month=YearMonth.of(2026,10);
                for(int i=0;i<25;i++)t.employees.add(new Employee("Employee "+i));
                Data data=new Data();MonthTable days=new MonthTable(()->t,()->month,Set::of);
                days.setModel(new ScheduleTableModel(data,t,month,()->{},()->true));
                JScrollPane scroll=new JScrollPane(days);FrozenNames frozen=new FrozenNames(days,scroll);
                for(int i=1;i<days.getColumnCount();i++)days.getColumnModel().getColumn(i).setPreferredWidth(104);
                scroll.setSize(850,300);scroll.doLayout();days.setSize(days.getPreferredSize());
                scroll.getViewport().setViewPosition(new Point(600,76));
                assertEquals(0,scroll.getRowHeader().getViewPosition().x);
                assertEquals(scroll.getViewport().getViewPosition().y,scroll.getRowHeader().getViewPosition().y);
                assertEquals(0,days.getColumnModel().getColumn(0).getWidth());
                assertEquals(days.getValueAt(0,0),frozen.table().getValueAt(0,0));
                days.setRowSelectionInterval(3,3);assertEquals(3,frozen.table().getSelectedRow());
                days.setModel(new ScheduleTableModel(data,t,month.plusMonths(1),()->{},()->true));
                assertSame(days.getModel(),frozen.table().getModel());assertEquals(1,frozen.table().getColumnCount());
                Color even=days.prepareRenderer(days.getCellRenderer(0,1),0,1).getBackground();
                Color odd=days.prepareRenderer(days.getCellRenderer(1,1),1,1).getBackground();assertNotEquals(even,odd);
                days.changeSelection(1,1,false,false);assertEquals(Theme.SELECTION,days.prepareRenderer(days.getCellRenderer(1,1),1,1).getBackground());
            }catch(Exception e){throw new RuntimeException(e);}
        });
    }
}
