// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/** A row header sharing the calendar model and row selection. */
public final class FrozenNames {
    private final JTable days;
    private final JTable names=new JTable();
    public FrozenNames(JTable days,JScrollPane scroll){
        this.days=days;
        names.setAutoCreateColumnsFromModel(false);
        names.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        names.setSelectionModel(days.getSelectionModel());
        names.setFocusable(false);
        names.setGridColor(days.getGridColor());
        names.setDefaultRenderer(Object.class,(table,value,selected,focus,row,col)->
            days.getDefaultRenderer(Object.class).getTableCellRendererComponent(days,value,selected,false,row,0));
        names.getTableHeader().setReorderingAllowed(false);
        names.getTableHeader().setDefaultRenderer(days.getTableHeader().getDefaultRenderer());
        scroll.setColumnHeaderView(days.getTableHeader());
        scroll.setRowHeaderView(names);
        scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER,names.getTableHeader());
        days.addPropertyChangeListener("model",e->refresh());
        days.addPropertyChangeListener("rowHeight",e->names.setRowHeight(days.getRowHeight()));
        refresh();
    }
    public void refresh(){
        names.setModel(days.getModel());
        while(names.getColumnCount()>0)names.removeColumn(names.getColumnModel().getColumn(0));
        if(days.getColumnCount()==0)return;
        TableColumn column=new TableColumn(0,UiScale.px(340));column.setHeaderValue(days.getModel().getColumnName(0));names.addColumn(column);
        names.setRowHeight(days.getRowHeight());names.setFont(days.getFont());
        names.setPreferredScrollableViewportSize(new Dimension(UiScale.px(340),0));
        names.getTableHeader().setPreferredSize(new Dimension(UiScale.px(340),days.getTableHeader().getPreferredSize().height));
        TableColumn hidden=days.getColumnModel().getColumn(0);hidden.setMinWidth(0);hidden.setMaxWidth(0);hidden.setPreferredWidth(0);hidden.setWidth(0);
    }
    JTable table(){return names;}
}
