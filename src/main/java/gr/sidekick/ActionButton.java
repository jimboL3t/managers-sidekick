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
        g.setColor(fill);g.fillRoundRect(1,1,getWidth()-2,getHeight()-2,14,14);
        g.setColor(isFocusOwner()?Theme.ACCENT:new Color(0x49405F));g.setStroke(new BasicStroke(isFocusOwner()?2f:1f));g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);g.dispose();super.paintComponent(graphics);
    }
}
