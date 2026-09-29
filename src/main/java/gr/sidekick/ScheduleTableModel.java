// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import javax.swing.table.AbstractTableModel;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BooleanSupplier;
import static gr.sidekick.Model.*;

/** Editing one cell never invokes the scheduler or modifies any other cell. */
public final class ScheduleTableModel extends AbstractTableModel {
    public record Choice(String id,String label) {
        @Override public String toString(){return label;}
    }
    private final Data data;
    private final Team team;
    private final YearMonth month;
    private final Runnable changed;
    private final BooleanSupplier editable;

    public ScheduleTableModel(Data data,Team team,YearMonth month,Runnable changed,BooleanSupplier editable) {
        this.data=data;this.team=team;this.month=month;this.changed=changed;this.editable=editable;
    }
    @Override public int getRowCount(){return team==null?0:team.employees.size();}
    @Override public int getColumnCount(){return month.lengthOfMonth()+1;}
    @Override public String getColumnName(int column){
        return column==0?I18n.text("Εργαζόμενος · Ρεπό · Βάρδιες"):month.atDay(column).format(DateTimeFormatter.ofPattern("dd EEE",I18n.locale()));
    }
    @Override public Object getValueAt(int row,int column){
        Employee e=team.employees.get(row);
        if(column==0)return e.name+"   ·   "+CalendarRules.count(team,e,month,OFF)+"/"+CalendarRules.offTarget(team,month)+I18n.text(" Ρ · ")+Scheduler.workCount(team,e,month)+"/"+Scheduler.workTarget(team,e,month)+I18n.text(" Β");
        Cell cell=team.cell(e.id,month.atDay(column));
        return cell==null?"—":(cell.locked?"★ ":"")+label(data,team,cell.value);
    }
    @Override public boolean isCellEditable(int row,int column){return column>0&&(team.months.get(month.toString())==null||!team.months.get(month.toString()).lockedDays.contains(column))&&editable.getAsBoolean();}
    @Override public void setValueAt(Object value,int row,int column){
        if(!(value instanceof Choice choice)||!isCellEditable(row,column))return;
        Employee employee=team.employees.get(row);
        Cell before=team.cell(employee.id,month.atDay(column));
        if(Objects.equals(choice.id,before==null?null:before.value)&&(before==null||before.locked))return;
        String key=key(employee.id,column);
        if(choice.id==null)team.month(month).cells.remove(key);
        else team.month(month).cells.put(key,new Cell(choice.id,true));
        changed.run();fireTableRowsUpdated(row,row);
    }
}
