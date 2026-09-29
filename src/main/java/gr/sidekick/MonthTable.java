package gr.sidekick;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.*;
import java.util.Set;
import java.util.function.Supplier;
import static gr.sidekick.Model.*;

/** Calendar rendering shared by the desktop window and headless presentation checks. */
public final class MonthTable extends JTable {
    private final Supplier<Team> team;
    private final Supplier<YearMonth> month;
    private final Supplier<Set<Integer>> uncovered;

    public MonthTable(Supplier<Team> team,Supplier<YearMonth> month,Supplier<Set<Integer>> uncovered) {
        this.team=team;this.month=month;this.uncovered=uncovered;
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);setRowHeight(38);setCellSelectionEnabled(true);
        setGridColor(new Color(0x3B3D4B));setSelectionBackground(Theme.SELECTION);setSelectionForeground(Theme.TEXT);
        getTableHeader().setReorderingAllowed(false);getTableHeader().setPreferredSize(new Dimension(0,52));
        setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int col){
                super.getTableCellRendererComponent(table,value,selected,focus,row,col);
                setBorder(BorderFactory.createEmptyBorder(0,8,0,8));setHorizontalAlignment(col==0?LEFT:CENTER);
                setBackground(selected?Theme.SELECTION:dayColor(col));setForeground(Theme.TEXT);
                Team t=team.get();
                if(col>0&&t!=null){Cell c=t.cell(t.employees.get(row).id,month.get().atDay(col));if(c!=null&&OFF.equals(c.value))setForeground(new Color(0xA9CFBA));}
                setToolTipText(String.valueOf(value));return this;
            }
        });
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int col){
                super.getTableCellRendererComponent(table,value,false,false,row,col);
                setHorizontalAlignment(CENTER);setBackground(dayColor(col));
                boolean missing=uncovered.get().contains(col);
                setForeground(missing?Theme.RED:Theme.TEXT);setFont(getFont().deriveFont(Font.BOLD));
                setBorder(BorderFactory.createMatteBorder(0,0,missing?3:1,1,missing?Theme.RED:new Color(0x444654)));
                setToolTipText(col==0?I18n.text("Εργαζόμενος · ρεπό / στόχος"):(missing?I18n.text("Ακάλυπτο πόστο · "):"")+(team.get()!=null&&CalendarRules.holiday(team.get(),month.get().atDay(col))?I18n.text("Αργία"):""));
                return this;
            }
        });
    }

    private Color dayColor(int column){
        if(column==0)return Theme.PANEL;
        LocalDate date=month.get().atDay(column);
        if(team.get()!=null&&CalendarRules.holiday(team.get(),date))return Theme.HOLIDAY;
        return CalendarRules.weekend(date)?Theme.WEEKEND:Theme.BACKGROUND;
    }
}
