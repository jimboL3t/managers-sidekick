package gr.sidekick;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

/** Restrained Dracula-inspired palette, without extra runtime dependencies. */
public final class Theme {
    public static final Color BACKGROUND = new Color(0x1B1932);
    public static final Color PANEL = new Color(0x141226);
    public static final Color TEXT = new Color(0xEAE5FF);
    public static final Color MUTED = new Color(0xAAA0CE);
    public static final Color ACCENT = new Color(0xB7A1F1);
    public static final Color WEEKEND = new Color(0x25213D);
    public static final Color HOLIDAY = new Color(0x233B3C);
    public static final Color RED = new Color(0xFF7979);
    public static final Color SELECTION = new Color(0x393052);

    private Theme() {}
    public static void install() throws Exception {
        // Metal's default Ocean theme paints bright gradients over component backgrounds.
        // Install a complete dark base theme before setting any component defaults.
        javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.DefaultMetalTheme(){
            @Override public String getName(){return "Sidekick Midnight";}
            @Override protected ColorUIResource getPrimary1(){return new ColorUIResource(SELECTION);}
            @Override protected ColorUIResource getPrimary2(){return new ColorUIResource(ACCENT);}
            @Override protected ColorUIResource getPrimary3(){return new ColorUIResource(BACKGROUND);}
            @Override protected ColorUIResource getSecondary1(){return new ColorUIResource(new Color(0x49405F));}
            @Override protected ColorUIResource getSecondary2(){return new ColorUIResource(SELECTION);}
            @Override protected ColorUIResource getSecondary3(){return new ColorUIResource(BACKGROUND);}
            @Override protected ColorUIResource getBlack(){return new ColorUIResource(TEXT);}
            @Override protected ColorUIResource getWhite(){return new ColorUIResource(PANEL);}
        });
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        for (String name : new String[]{"Panel", "OptionPane", "Viewport", "ScrollPane", "TabbedPane"}) {
            UIManager.put(name+".background", new ColorUIResource(PANEL));
            UIManager.put(name+".foreground", new ColorUIResource(TEXT));
        }
        for (String name : new String[]{"Button", "ToggleButton", "ComboBox", "Spinner", "TextField", "FormattedTextField", "TextArea", "Table", "TableHeader", "List", "CheckBox", "Label", "Menu", "MenuItem", "PopupMenu", "ToolTip"}) {
            UIManager.put(name+".background", new ColorUIResource(BACKGROUND));
            UIManager.put(name+".foreground", new ColorUIResource(TEXT));
            UIManager.put(name+".font", new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, 13));
            UIManager.put(name+".selectionBackground", new ColorUIResource(SELECTION));
            UIManager.put(name+".selectionForeground", new ColorUIResource(TEXT));
        }
        for(String key:new String[]{"Button.gradient","ToggleButton.gradient","CheckBox.gradient","RadioButton.gradient","ScrollBar.gradient"})UIManager.put(key,null);
        UIManager.put("Button.background",new ColorUIResource(new Color(0x302943)));
        UIManager.put("Button.disabledText",new ColorUIResource(MUTED));
        UIManager.put("ComboBox.disabledForeground",new ColorUIResource(MUTED));
        UIManager.put("Button.border",BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x4D4266)),BorderFactory.createEmptyBorder(8,12,8,12)));
        UIManager.put("SplitPane.background",PANEL);
        UIManager.put("SplitPaneDivider.border",BorderFactory.createEmptyBorder());
        UIManager.put("SplitPane.dividerSize",8);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("FormattedTextField.caretForeground", TEXT);
        UIManager.put("Button.select", SELECTION);
        UIManager.put("ComboBox.selectionBackground", SELECTION);
        UIManager.put("ScrollBar.thumb", new ColorUIResource(SELECTION));
        UIManager.put("ScrollBar.track", new ColorUIResource(PANEL));
        UIManager.put("ScrollBarUI", DarkScrollBarUI.class.getName());
        UIManager.put("OptionPane.messageForeground", TEXT);
    }

    public static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        public static javax.swing.plaf.ComponentUI createUI(JComponent component){return new DarkScrollBarUI();}
        @Override protected void paintTrack(Graphics g,JComponent c,Rectangle r){g.setColor(PANEL);g.fillRect(r.x,r.y,r.width,r.height);}
        @Override protected void paintThumb(Graphics g,JComponent c,Rectangle r){
            Graphics2D graphics=(Graphics2D)g.create();graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(isThumbRollover()?new Color(0x6C6280):new Color(0x4C4D60));graphics.fillRoundRect(r.x+2,r.y+2,Math.max(0,r.width-4),Math.max(0,r.height-4),8,8);graphics.dispose();
        }
        private JButton arrow(){JButton button=new JButton();button.setPreferredSize(new Dimension(0,0));button.setBorder(null);button.setFocusable(false);return button;}
        @Override protected JButton createDecreaseButton(int direction){return arrow();}
        @Override protected JButton createIncreaseButton(int direction){return arrow();}
    }
}
