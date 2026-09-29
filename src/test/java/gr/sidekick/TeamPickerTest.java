// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class TeamPickerTest {
    @Test void homeRequiresExplicitOpenAndRendersDarkControls() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try{
                Theme.install();Data data=new Data();Team a=TeamService.create(data,"Ομάδα Α · Υποδοχή");Team b=TeamService.create(data,"Ομάδα Β · Υποστήριξη");Team copy=TeamService.duplicate(data,b,"Ομάδα Β · Δοκιμή");
                AtomicReference<Team> opened=new AtomicReference<>();
                AtomicReference<Team> annual=new AtomicReference<>();
                TeamPicker picker=new TeamPicker(()->{},opened::set,t->{},t->{},annual::set);
                picker.refresh(data,null);assertNull(opened.get());assertFalse(find(picker,"Άνοιγμα ημερολογίου").isEnabled());
                picker.refresh(data,b);assertNull(opened.get());find(picker,"Ετήσια εικόνα").doClick();assertSame(b,annual.get());assertNull(opened.get());find(picker,"Άνοιγμα ημερολογίου").doClick();assertSame(b,opened.get());
                picker.refresh(data,copy);picker.setSize(1200,650);layout(picker);
                BufferedImage home=render(picker);
                ImageIO.write(home,"png",Path.of("target","team-picker-preview.png").toFile());
                JList<?> teamList=findList(picker);Point origin=SwingUtilities.convertPoint(teamList,0,0,picker);
                int visibleText=0;for(int y=Math.max(0,origin.y);y<Math.min(home.getHeight(),origin.y+teamList.getHeight());y++)for(int x=50;x<1100;x++)if(new Color(home.getRGB(x,y)).getRed()>140)visibleText++;
                assertTrue(visibleText>200,"Team rows must show readable names and details");
                JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT,16,20));
                JButton button=new JButton("Υπολογισμός βαρδιών");controls.add(button);controls.add(new JComboBox<>(new String[]{"Σεπτέμβριος","Οκτώβριος"}));
                JSpinner year=new JSpinner(new SpinnerNumberModel(2026,1900,2200,1));year.setEditor(new JSpinner.NumberEditor(year,"0"));controls.add(year);
                controls.add(new JCheckBox("Πρωινή βάρδια",true));controls.add(new JTextField("Όνομα ομάδας",15));controls.setSize(1100,100);layout(controls);
                ImageIO.write(render(controls),"png",Path.of("target","controls-preview.png").toFile());
                BufferedImage image=render(button);int dark=0,total=0;
                for(int y=4;y<image.getHeight()-4;y++)for(int x=4;x<image.getWidth()-4;x++){
                    Color c=new Color(image.getRGB(x,y));if((c.getRed()+c.getGreen()+c.getBlue())/3<110)dark++;total++;
                }
                assertTrue(dark>total*.70,"Button must not paint Ocean's bright gradient");
                assertNull(UIManager.get("Button.gradient"));
            }catch(Exception e){throw new RuntimeException(e);}
        });
    }
    private static JList<?> findList(Container parent){for(Component c:parent.getComponents()){if(c instanceof JList<?> list)return list;if(c instanceof Container child){JList<?> found=findList(child);if(found!=null)return found;}}return null;}
    private static JButton find(Container parent,String text){
        for(Component c:parent.getComponents()){
            if(c instanceof JButton b&&text.equals(b.getText()))return b;
            if(c instanceof Container child){JButton b=find(child,text);if(b!=null)return b;}
        }return null;
    }
    private static BufferedImage render(JComponent c){BufferedImage image=new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();c.printAll(g);g.dispose();return image;}
    private static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container container)layout(container);}
}
