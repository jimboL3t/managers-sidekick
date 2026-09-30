// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.*;

/** Resolution-independent gear for settings and input dialogs. */
public final class SettingsIcon implements Icon {
    public int getIconWidth(){return 32;}
    public int getIconHeight(){return 32;}
    public void paintIcon(Component c,Graphics graphics,int x,int y){
        Graphics2D g=(Graphics2D)graphics.create();g.translate(x,y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Area gear=new Area(new Ellipse2D.Double(6,6,20,20));
        for(int i=0;i<8;i++){
            Shape tooth=AffineTransform.getRotateInstance(i*Math.PI/4,16,16)
                .createTransformedShape(new RoundRectangle2D.Double(13,2,6,9,2,2));
            gear.add(new Area(tooth));
        }
        gear.subtract(new Area(new Ellipse2D.Double(11,11,10,10)));
        g.setColor(Theme.BRAND_SILVER);g.fill(gear);g.dispose();
    }
}
