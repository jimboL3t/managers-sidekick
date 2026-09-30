// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** Small company signature, independent of the application's primary monogram. */
public final class CompanyBrand {
    private static final BufferedImage IMAGE=load();
    private CompanyBrand() {}
    private static BufferedImage load(){
        try(var stream=CompanyBrand.class.getResourceAsStream("/brand/nervz.png")){
            if(stream==null)throw new IllegalStateException("Missing company logo");
            return ImageIO.read(stream);
        }catch(IOException e){throw new IllegalStateException("Cannot read company logo",e);}
    }
    public static JLabel label(){
        Icon icon=new Icon(){
            public int getIconWidth(){return 108;}
            public int getIconHeight(){return 40;}
            public void paintIcon(Component c,Graphics graphics,int x,int y){
                Graphics2D g=(Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int height=(int)Math.round(108d*IMAGE.getHeight()/IMAGE.getWidth());
                g.drawImage(IMAGE,x,y+(40-height)/2,108,height,null);g.dispose();
            }
        };
        JLabel label=new JLabel(icon);label.setToolTipText("nervZ");
        label.getAccessibleContext().setAccessibleName("nervZ");
        label.setVerticalAlignment(SwingConstants.BOTTOM);return label;
    }
}
