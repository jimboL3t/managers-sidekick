// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class MonthTableTest {
    @Test void calendarHasDistinctQuietColorsAndMarksUncoveredDates() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try {
                Theme.install();Team t=new Team("Υποδοχή");YearMonth ym=YearMonth.of(2026,9);
                t.month(ym).holidays.add(8);Post p=new Post("Πρωινή","08:00","16:00");t.posts.add(p);
                for(String name:java.util.List.of("Μαρία Παπαδοπούλου","Νίκος Ιωάννου","Ελένη Γεωργίου")) {
                    Employee e=new Employee(name);t.employees.add(e);
                    for(int d=1;d<=30;d++)t.month(ym).cells.put(key(e.id,d),new Cell(d%4==0?OFF:p.id,d==1));
                }
                MonthTable table=new MonthTable(()->t,()->ym,()->Set.of(9));
                table.setModel(new ScheduleTableModel(new Data(),t,ym,()->{},()->true));
                assertEquals(Theme.BACKGROUND,table.prepareRenderer(table.getCellRenderer(0,4),0,4).getBackground());
                assertEquals(Theme.WEEKEND,table.prepareRenderer(table.getCellRenderer(0,5),0,5).getBackground());
                assertEquals(Theme.HOLIDAY,table.prepareRenderer(table.getCellRenderer(0,8),0,8).getBackground());
                Component header=table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table,"09 Τετ",false,false,-1,9);
                assertEquals(Theme.RED,header.getForeground());
                JSpinner year=new JSpinner(new SpinnerNumberModel(2026,1900,2200,1));year.setEditor(new JSpinner.NumberEditor(year,"0"));
                assertEquals("2026",((JSpinner.NumberEditor)year.getEditor()).getTextField().getText());
                table.getColumnModel().getColumn(0).setPreferredWidth(235);
                for(int c=1;c<table.getColumnCount();c++)table.getColumnModel().getColumn(c).setPreferredWidth(104);
                JPanel preview=new JPanel(new BorderLayout(0,20));preview.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
                JPanel titles=new JPanel(new GridLayout(3,1,0,8));
                JLabel brand=new JLabel("Manager’s Sidekick");brand.setFont(new Font("SansSerif",Font.BOLD,24));brand.setForeground(Theme.ACCENT);titles.add(brand);
                JLabel title=new JLabel("Σεπτέμβριος 2026");title.setFont(new Font("SansSerif",Font.BOLD,22));titles.add(title);
                JLabel subtitle=new JLabel("Υποδοχή · 3 εργαζόμενοι · 9 ρεπό / άτομο");subtitle.setForeground(Theme.MUTED);titles.add(subtitle);preview.add(titles,BorderLayout.NORTH);
                JScrollPane scroll=new JScrollPane(table);scroll.setColumnHeaderView(table.getTableHeader());scroll.getViewport().setBackground(Theme.BACKGROUND);preview.add(scroll);
                JLabel legend=new JLabel("Σαββατοκύριακο: μωβ · Αργία: πράσινο · Ακάλυπτη ημερομηνία: κόκκινο");legend.setForeground(Theme.MUTED);preview.add(legend,BorderLayout.SOUTH);
                preview.setSize(1320,460);layout(preview);
                BufferedImage image=new BufferedImage(1320,460,BufferedImage.TYPE_INT_RGB);Graphics2D graphics=image.createGraphics();preview.printAll(graphics);graphics.dispose();
                ImageIO.write(image,"png",Path.of("target","calendar-preview.png").toFile());
            } catch(Exception e){throw new RuntimeException(e);}
        });
    }
    private static void layout(Container container){container.doLayout();for(Component c:container.getComponents())if(c instanceof Container child)layout(child);}
}
