// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import javax.swing.*;
import java.awt.*;

/** Painted translucent-looking surface; no platform-specific blur dependency. */
public final class GlassPanel extends JPanel {
    public GlassPanel(LayoutManager layout){super(layout);setOpaque(false);}
    @Override protected void paintComponent(Graphics graphics){
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0,0,new Color(0x302B48),getWidth(),getHeight(),new Color(0x1D1B31)));
        g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);
        g.setColor(new Color(255,255,255,35));g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);
        g.dispose();super.paintComponent(graphics);
    }
}
