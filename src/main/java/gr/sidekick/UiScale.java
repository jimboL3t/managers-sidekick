// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.*;

/** Application-level scaling, independent of OS/Retina scaling. Uses unscaled baselines. */
public final class UiScale {
    private static int percent=100;
    private static boolean installed;
    private static final Map<LayoutManager,int[]> gaps=new WeakHashMap<>();
    private static final Map<javax.swing.table.TableColumn,Integer> widths=new WeakHashMap<>();
    private static final Map<Component,Base> bases=new WeakHashMap<>();
    private record Base(Font font,Dimension preferred,Dimension minimum,Border border,Icon icon,int rowHeight,int cellHeight,int cellWidth){}
    private UiScale(){}
    public static int normalize(int value){return Set.of(100,125,150,175,200).contains(value)?value:100;}
    public static void set(int value){percent=normalize(value);}
    public static int percent(){return percent;}
    public static int px(int value){return Math.round(value*percent/100f);}
    public static void install(){
        if(installed)return;installed=true;
        Toolkit.getDefaultToolkit().addAWTEventListener(event->{
            if(event instanceof WindowEvent w&&w.getID()==WindowEvent.WINDOW_OPENED&&w.getWindow() instanceof JDialog dialog){
                apply(dialog);dialog.pack();
                Rectangle screen=dialog.getGraphicsConfiguration().getBounds();
                if(dialog.getWidth()>screen.width-40||dialog.getHeight()>screen.height-60){
                    Container content=dialog.getContentPane();dialog.setContentPane(new JScrollPane(content));
                    dialog.setSize(Math.min(dialog.getWidth()+20,screen.width-40),Math.min(dialog.getHeight()+20,screen.height-60));
                }
                dialog.setLocationRelativeTo(dialog.getOwner());
            }
        },AWTEvent.WINDOW_EVENT_MASK);
    }
    private static Dimension scaled(Dimension d){return d==null?null:new Dimension(px(d.width),px(d.height));}
    public static void apply(Component c){
        Base b=bases.computeIfAbsent(c,k->new Base(c.getFont(),c.isPreferredSizeSet()?c.getPreferredSize():null,c.isMinimumSizeSet()?c.getMinimumSize():null,
            c instanceof JComponent j?j.getBorder():null,c instanceof JLabel l?l.getIcon():null,c instanceof JTable t?t.getRowHeight():0,
            c instanceof JList<?> l?l.getFixedCellHeight():-1,c instanceof JList<?> l?l.getFixedCellWidth():-1));
        if(b.font()!=null)c.setFont(b.font().deriveFont(b.font().getSize2D()*percent/100f));
        if(b.preferred()!=null)c.setPreferredSize(scaled(b.preferred()));
        if(b.minimum()!=null)c.setMinimumSize(scaled(b.minimum()));
        if(c instanceof JComponent j&&b.border()!=null)j.setBorder(scaleBorder(b.border()));
        if(c instanceof JLabel l&&b.icon()!=null)l.setIcon(scaleIcon(b.icon()));
        if(c instanceof JTable t){
            t.setRowHeight(px(b.rowHeight()));
            for(int i=0;i<t.getColumnCount();i++){
                var column=t.getColumnModel().getColumn(i);
                int width=widths.computeIfAbsent(column,k->k.getPreferredWidth());column.setPreferredWidth(px(width));
            }
        }
        if(c instanceof JList<?> l){if(b.cellHeight()>0)l.setFixedCellHeight(px(b.cellHeight()));if(b.cellWidth()>0)l.setFixedCellWidth(px(b.cellWidth()));}
        if(c instanceof Container container){
            LayoutManager layout=container.getLayout();
            if(layout instanceof BorderLayout l){int[] gap=gaps.computeIfAbsent(l,k->new int[]{l.getHgap(),l.getVgap()});l.setHgap(px(gap[0]));l.setVgap(px(gap[1]));}
            if(layout instanceof FlowLayout l){int[] gap=gaps.computeIfAbsent(l,k->new int[]{l.getHgap(),l.getVgap()});l.setHgap(px(gap[0]));l.setVgap(px(gap[1]));}
            if(layout instanceof GridLayout l){int[] gap=gaps.computeIfAbsent(l,k->new int[]{l.getHgap(),l.getVgap()});l.setHgap(px(gap[0]));l.setVgap(px(gap[1]));}
            for(Component child:container.getComponents())apply(child);
        }
        if(c instanceof JComponent j)j.revalidate();
        c.repaint();
    }
    private static Border scaleBorder(Border border){
        if(border instanceof EmptyBorder e){Insets i=e.getBorderInsets();return BorderFactory.createEmptyBorder(px(i.top),px(i.left),px(i.bottom),px(i.right));}
        if(border instanceof CompoundBorder c)return new CompoundBorder(c.getOutsideBorder()==null?null:scaleBorder(c.getOutsideBorder()),c.getInsideBorder()==null?null:scaleBorder(c.getInsideBorder()));
        return border;
    }
    private static Icon scaleIcon(Icon icon){return new Icon(){
        public int getIconWidth(){return px(icon.getIconWidth());}public int getIconHeight(){return px(icon.getIconHeight());}
        public void paintIcon(Component c,Graphics graphics,int x,int y){Graphics2D g=(Graphics2D)graphics.create();g.translate(x,y);g.scale(percent/100d,percent/100d);icon.paintIcon(c,g,0,0);g.dispose();}
    };}
}
