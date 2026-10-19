// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.swing.*;
import java.awt.*;

/** Independent view-only axes, always computed from unscaled baselines. */
public final class CalendarZoom extends JPanel {
 private final JTable grid;
 private final FrozenNames names;
 private final Font baseFont;
 private final JSlider horizontal=new JSlider(50,200,100),vertical=new JSlider(50,200,100);
 private final JLabel xValue=new JLabel(),yValue=new JLabel();
 public CalendarZoom(JTable grid,FrozenNames names){
  super(new FlowLayout(FlowLayout.TRAILING,10,4));this.grid=grid;this.names=names;baseFont=grid.getFont();
  add(new JLabel(I18n.text("Ημέρες (Χ)")));add(horizontal);add(xValue);
  add(new JLabel(I18n.text("Εργαζόμενοι (Υ)")));add(vertical);add(yValue);
  for(JSlider slider:new JSlider[]{horizontal,vertical}){slider.setPreferredSize(new Dimension(130,24));slider.setMinorTickSpacing(10);slider.addChangeListener(e->applyZoom());}
  horizontal.getAccessibleContext().setAccessibleName(I18n.text("Ημέρες (Χ)"));vertical.getAccessibleContext().setAccessibleName(I18n.text("Εργαζόμενοι (Υ)"));
  JButton reset=new JButton(I18n.text("Επαναφορά zoom"));reset.addActionListener(e->setZoom(100,100));add(reset);
 }
 public void setZoom(int x,int y){horizontal.setValue(x);vertical.setValue(y);applyZoom();}
 public void applyZoom(){
  double x=horizontal.getValue()/100d,y=vertical.getValue()/100d;
  // Commit an active editor before changing geometry, never rebuild the model.
  if(grid.isEditing()&&!grid.getCellEditor().stopCellEditing())return;
  grid.setFont(baseFont.deriveFont((float)(baseFont.getSize2D()*UiScale.percent()/100d*Math.min(x,y))));
  grid.setRowHeight(Math.max(1,(int)Math.round(UiScale.px(38)*y)));
  grid.getTableHeader().setPreferredSize(new Dimension(0,Math.max(1,(int)Math.round(UiScale.px(52)*y))));
  for(int c=1;c<grid.getColumnCount();c++){
   var column=grid.getColumnModel().getColumn(c);int width=Math.max(1,(int)Math.round(UiScale.px(104)*x));
   column.setPreferredWidth(width);column.setWidth(width);
  }
  names.setHorizontalPercent(horizontal.getValue());names.refresh();
  xValue.setText(horizontal.getValue()+"%");yValue.setText(vertical.getValue()+"%");
  grid.revalidate();grid.getTableHeader().revalidate();grid.repaint();grid.getTableHeader().repaint();
 }
}
