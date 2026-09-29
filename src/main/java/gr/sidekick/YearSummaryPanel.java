// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.Year;
import static gr.sidekick.Model.*;

public final class YearSummaryPanel extends JPanel {
    private final Data data;
    private final Team team;
    private final JSpinner year=new JSpinner(new SpinnerNumberModel(Year.now().getValue(),1900,2200,1));
    private final JPanel cards=new JPanel(new GridLayout(1,4,12,0));
    private final JTable table=new JTable();
    public YearSummaryPanel(Data data,Team team){
        super(new BorderLayout(0,20));this.data=data;this.team=team;setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel heading=new JPanel(new BorderLayout(16,16));
        JLabel title=new JLabel(team.name+I18n.text(" · Ετήσια εικόνα"));title.setFont(new Font("SansSerif",Font.BOLD,22));title.setForeground(Theme.ACCENT);heading.add(title,BorderLayout.WEST);
        year.setEditor(new JSpinner.NumberEditor(year,"0"));year.setPreferredSize(new Dimension(95,36));
        JPanel selection=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));selection.add(new JLabel(I18n.text("Έτος")));selection.add(year);heading.add(selection,BorderLayout.EAST);
        JPanel top=new JPanel(new BorderLayout(0,22));top.add(heading,BorderLayout.NORTH);top.add(cards);add(top,BorderLayout.NORTH);
        table.setRowHeight(38);table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);table.setGridColor(new Color(0x363049));table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0,38));
        javax.swing.table.DefaultTableCellRenderer renderer=new javax.swing.table.DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable grid,Object value,boolean selected,boolean focus,int row,int column){
                super.getTableCellRendererComponent(grid,value,selected,focus,row,column);
                boolean total=row==grid.getRowCount()-1;
                setHorizontalAlignment(column==0?LEFT:CENTER);setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                setBackground(selected?Theme.SELECTION:total?new Color(0x302943):Theme.BACKGROUND);
                setForeground(total?Theme.ACCENT:Theme.TEXT);setFont(grid.getFont().deriveFont(total?Font.BOLD:Font.PLAIN));return this;
            }
        };
        table.setDefaultRenderer(Object.class,renderer);table.setDefaultRenderer(Integer.class,renderer);
        JScrollPane scroll=new JScrollPane(table);scroll.setColumnHeaderView(table.getTableHeader());scroll.getViewport().setBackground(Theme.BACKGROUND);add(scroll);
        JLabel note=new JLabel(I18n.text("Σύνολα αποθηκευμένων αναθέσεων, όχι πραγματικής παρουσίας. Οι μήνες χωρίς δεδομένα δεν προσμετρώνται."));note.setForeground(Theme.MUTED);add(note,BorderLayout.SOUTH);
        year.addChangeListener(e->refresh());refresh();
    }
    public void selectYear(int value){year.setValue(value);}
    private JPanel card(String title,int value){
        JPanel card=new JPanel(new GridLayout(2,1,0,8));card.setBackground(Theme.BACKGROUND);card.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));
        JLabel heading=new JLabel(title);heading.setForeground(Theme.MUTED);JLabel number=new JLabel(Integer.toString(value));number.setForeground(Theme.ACCENT);number.setFont(new Font("SansSerif",Font.BOLD,27));card.add(heading);card.add(number);return card;
    }
    private void refresh(){
        YearSummary.Result result=YearSummary.calculate(data,team,(Integer)year.getValue());
        cards.removeAll();cards.add(card(I18n.text("Μήνες με αναθέσεις / 12"),result.months()));cards.add(card(I18n.text("Βάρδιες"),result.shifts()));cards.add(card(I18n.text("Ρεπό"),result.rest()));cards.add(card(I18n.text("Άδειες"),result.leave()));cards.revalidate();cards.repaint();
        table.setModel(new AbstractTableModel(){
            public int getRowCount(){return result.rows().size()+1;}
            public int getColumnCount(){return 4+result.categories().size();}
            public String getColumnName(int c){return switch(c){case 0->I18n.text("Εργαζόμενος");case 1->I18n.text("Βάρδιες");case 2->I18n.text("Ρεπό");case 3->I18n.text("Άδειες");default->label(data,team,result.categories().get(c-4));};}
            public Class<?> getColumnClass(int c){return c==0?String.class:Integer.class;}
            public Object getValueAt(int r,int c){
                if(r==result.rows().size())return switch(c){case 0->I18n.text("ΣΥΝΟΛΟ ΟΜΑΔΑΣ");case 1->result.shifts();case 2->result.rest();case 3->result.leave();default->result.rows().stream().mapToInt(row->row.categories().getOrDefault(result.categories().get(c-4),0)).sum();};
                YearSummary.Row row=result.rows().get(r);return switch(c){case 0->row.name();case 1->row.shifts();case 2->row.rest();case 3->row.leave();default->row.categories().getOrDefault(result.categories().get(c-4),0);};
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(230);for(int c=1;c<table.getColumnCount();c++)table.getColumnModel().getColumn(c).setPreferredWidth(125);
    }
}
