// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/** Rounded actions with the same rendering on macOS, Windows and Linux. */
public final class ActionButton extends JButton {
    public enum Style { STANDARD, PRIMARY, EXPORT, QUIET, DANGER }
    private final Style style;
    public ActionButton(String text,Style style) {
        super(text);this.style=style;
        setUI(new BasicButtonUI());setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setRolloverEnabled(true);
        setBorder(BorderFactory.createEmptyBorder(11,16,11,16));
        setFont(new Font("SansSerif",style==Style.PRIMARY||style==Style.EXPORT?Font.BOLD:Font.PLAIN,13));
        setForeground(style==Style.PRIMARY||style==Style.EXPORT?Theme.PANEL:style==Style.DANGER?Theme.RED:Theme.TEXT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override protected void paintComponent(Graphics graphics){
        Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill=switch(style){case PRIMARY->new Color(0xBEA6F6);case EXPORT->new Color(0x91D5C2);case QUIET->Theme.PANEL;case DANGER->new Color(0x352338);default->new Color(0x2A2541);};
        if(!isEnabled())fill=Theme.BACKGROUND;else if(getModel().isPressed())fill=fill.darker();else if(getModel().isRollover())fill=fill.brighter();
        int w=getWidth()-4,h=getHeight()-5;
        g.setColor(new Color(0,0,0,65));g.fillRoundRect(2,4,w,h,18,18);
        g.setPaint(new GradientPaint(0,2,fill.brighter(),0,getHeight(),fill));g.fillRoundRect(2,1,w,h,18,18);
        Shape surface=new java.awt.geom.RoundRectangle2D.Float(2,1,w,h,18,18);g.setClip(surface);
        g.setPaint(new GradientPaint(0,1,new Color(255,255,255,isEnabled()?38:10),0,getHeight()/2f,new Color(255,255,255,0)));
        g.fillRect(2,1,w,h);g.setClip(null);
        g.setColor(isFocusOwner()?Theme.ACCENT:new Color(255,255,255,isEnabled()?65:20));
        g.setStroke(new BasicStroke(isFocusOwner()?2f:1f));g.drawRoundRect(2,1,w-1,h-1,18,18);
        g.dispose();super.paintComponent(graphics);
    }
}
