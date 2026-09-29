package gr.sidekick;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class YearSummaryPanelTest {
 @Test void yearSelectionRefreshesReadOnlyTableAndRendersActions() throws Exception {
  SwingUtilities.invokeAndWait(()->{try{
   Theme.install();Data data=new Data();Team t=TeamService.create(data,"Ομάδα υποστήριξης");Post p=new Post("Πρωινή","08:00","16:00");t.posts.add(p);
   for(String name:java.util.List.of("Μαρία Παπαδοπούλου","Νίκος Ιωάννου","Ελένη Γεωργίου")){
    Employee e=new Employee(name);t.employees.add(e);
    for(int m=1;m<=3;m++)for(int d=1;d<=28;d++)t.month(YearMonth.of(2026,m)).cells.put(key(e.id,d),new Cell(d%7<2?OFF:p.id,false));
   }
   YearSummaryPanel panel=new YearSummaryPanel(data,t);panel.selectYear(2026);JTable table=find(panel);
   assertEquals(4,table.getRowCount());assertEquals(60,table.getValueAt(0,1));assertFalse(table.isCellEditable(0,1));
   panel.selectYear(2025);assertEquals(0,table.getValueAt(0,1));panel.selectYear(2026);
   panel.setSize(1150,600);layout(panel);write(panel,"annual-preview.png");
   JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,14,18));
   actions.add(new ActionButton("Υπολογισμός βαρδιών",ActionButton.Style.STANDARD));actions.add(new ActionButton("Αποθήκευση",ActionButton.Style.PRIMARY));actions.add(new ActionButton("Εξαγωγή PDF",ActionButton.Style.EXPORT));
   ActionButton focused=new ActionButton("Έλεγχος",ActionButton.Style.STANDARD);focused.getModel().setRollover(true);actions.add(focused);
   actions.setSize(1000,90);layout(actions);write(actions,"actions-preview.png");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
 private static JTable find(Container c){for(Component x:c.getComponents()){if(x instanceof JTable table)return table;if(x instanceof Container child){JTable found=find(child);if(found!=null)return found;}}return null;}
 private static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container child)layout(child);}
 private static void write(JComponent c,String name)throws Exception{BufferedImage image=new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();c.printAll(g);g.dispose();ImageIO.write(image,"png",Path.of("target",name).toFile());}
}
