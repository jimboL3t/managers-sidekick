// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import static gr.sidekick.Model.*;

/** The landing page deliberately requires opening a team before showing a calendar. */
public final class TeamPicker extends JPanel {
    private final DefaultListModel<Team> model=new DefaultListModel<>();
    private final JList<Team> list=new JList<>(model);
    private final JLabel count=new JLabel();
    private final JButton open,copy,delete,annual;

    public TeamPicker(Runnable create,Consumer<Team> onOpen,Consumer<Team> onCopy,Consumer<Team> onDelete,Consumer<Team> onAnnual){
        this(create,onOpen,onCopy,onDelete,onAnnual,language->{});
    }
    public TeamPicker(Runnable create,Consumer<Team> onOpen,Consumer<Team> onCopy,Consumer<Team> onDelete,Consumer<Team> onAnnual,Consumer<String> onLanguage){
        super(new BorderLayout(0,24));setBorder(BorderFactory.createEmptyBorder(36,36,28,36));
        JPanel title=new JPanel(new GridLayout(0,1,0,10));
        JLabel brand=new JLabel("Manager’s Sidekick",Logo.icon(42),SwingConstants.LEFT);brand.setIconTextGap(12);brand.setForeground(Theme.ACCENT);brand.setFont(new Font("SansSerif",Font.BOLD,28));title.add(brand);
        JLabel heading=new JLabel(I18n.text("Ομάδες εργασίας"));heading.setFont(new Font("SansSerif",Font.BOLD,23));title.add(heading);
        JLabel hint=new JLabel(I18n.text("Επιλέξτε ομάδα για να ανοίξετε το ημερολόγιο ή δημιουργήστε ανεξάρτητο αντίγραφο για δοκιμές."));hint.setForeground(Theme.MUTED);title.add(hint);title.add(count);
        JComboBox<String> language=new JComboBox<>(new String[]{"Ελληνικά","English"});language.setSelectedIndex(I18n.english()?1:0);
        language.setToolTipText("Γλώσσα / Language");language.getAccessibleContext().setAccessibleName("Γλώσσα / Language");
        language.addActionListener(e->onLanguage.accept(language.getSelectedIndex()==1?"en":"el"));
        JPanel languages=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));languages.add(language);
        JButton license=new ActionButton(I18n.text("Άδεια χρήσης"),ActionButton.Style.QUIET);
        license.addActionListener(e->{try(var in=TeamPicker.class.getResourceAsStream("/META-INF/sidekick/LICENSE")){
            String text="Manager’s Sidekick\nCopyright (C) 2026 Dimitrios Diamantis\nGPL-3.0-only — WITHOUT ANY WARRANTY\n\n"+new String(java.util.Objects.requireNonNull(in).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            JTextArea area=new JTextArea(text,24,80);area.setEditable(false);area.setCaretPosition(0);JOptionPane.showMessageDialog(this,new JScrollPane(area),I18n.text("Άδεια χρήσης"),JOptionPane.PLAIN_MESSAGE);
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),I18n.text("Σφάλμα"),JOptionPane.ERROR_MESSAGE);}});languages.add(license);
        JPanel top=new JPanel(new BorderLayout(16,0));top.add(title,BorderLayout.CENTER);top.add(languages,BorderLayout.EAST);add(top,BorderLayout.NORTH);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);list.setFixedCellHeight(88);
        list.setCellRenderer((items,team,index,selected,focus)->{
            JPanel row=new JPanel(new BorderLayout(12,8)){
                @Override protected void paintComponent(Graphics graphics){doLayout();for(Component child:getComponents())if(child instanceof Container container)container.doLayout();super.paintComponent(graphics);}
            };row.setBackground(selected?Theme.SELECTION:Theme.BACKGROUND);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,Theme.PANEL),BorderFactory.createEmptyBorder(14,18,14,18)));
            JPanel text=new JPanel(new GridLayout(2,1,0,8));text.setOpaque(false);
            JLabel name=new JLabel(team.name);name.setFont(new Font("SansSerif",Font.BOLD,18));text.add(name);
            JLabel detail=new JLabel(team.employees.size()+I18n.text(" εργαζόμενοι  ·  ")+team.posts.size()+I18n.text(" πόστα  ·  ")+team.months.size()+I18n.text(" μήνες"));detail.setForeground(Theme.MUTED);text.add(detail);row.add(text,BorderLayout.CENTER);
            JLabel badge=new JLabel(team.sandbox?I18n.text("ΔΟΚΙΜΑΣΤΙΚΟ ΑΝΤΙΓΡΑΦΟ"):I18n.text("ΟΜΑΔΑ ΕΡΓΑΣΙΑΣ"));badge.setForeground(Theme.ACCENT);row.add(badge,BorderLayout.EAST);return row;
        });
        add(new JScrollPane(list));
        JPanel actions=new GlassPanel(new BorderLayout(12,14));actions.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JPanel primary=new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        open=action(I18n.text("Άνοιγμα ημερολογίου"),()->withSelection(onOpen),ActionButton.Style.PRIMARY);primary.add(open);
        annual=action(I18n.text("Ετήσια εικόνα"),()->withSelection(onAnnual),ActionButton.Style.EXPORT);primary.add(annual);
        JPanel management=new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        management.add(action(I18n.text("Νέα ομάδα"),create,ActionButton.Style.STANDARD));
        copy=action(I18n.text("Αντίγραφο για δοκιμές"),()->withSelection(onCopy),ActionButton.Style.STANDARD);management.add(copy);
        delete=action(I18n.text("Διαγραφή ομάδας"),()->withSelection(onDelete),ActionButton.Style.DANGER);management.add(delete);
        primary.setOpaque(false);management.setOpaque(false);actions.add(primary,BorderLayout.NORTH);actions.add(management,BorderLayout.SOUTH);add(actions,BorderLayout.SOUTH);
        list.addListSelectionListener(e->updateButtons());
        list.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){int index=list.locationToIndex(e.getPoint());if(e.getClickCount()==2&&index>=0&&list.getCellBounds(index,index).contains(e.getPoint()))withSelection(onOpen);}});
        list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"open");list.getActionMap().put("open",new AbstractAction(){public void actionPerformed(ActionEvent e){withSelection(onOpen);}});
        updateButtons();
    }
    private JButton action(String text,Runnable run,ActionButton.Style style){JButton b=new ActionButton(text,style);b.addActionListener(e->run.run());return b;}
    private void withSelection(Consumer<Team> action){Team selected=list.getSelectedValue();if(selected!=null)action.accept(selected);}
    private void updateButtons(){boolean selected=list.getSelectedValue()!=null;open.setEnabled(selected);annual.setEnabled(selected);copy.setEnabled(selected);delete.setEnabled(selected);}
    public void refresh(Data data,Team selected){
        model.clear();for(Team t:data.teams)model.addElement(t);
        if(selected!=null)list.setSelectedValue(selected,true);
        count.setText(data.teams.isEmpty()?I18n.text("Δεν υπάρχουν ομάδες ακόμη. Ξεκινήστε με «Νέα ομάδα»."):data.teams.size()+I18n.text(" διαθέσιμες ομάδες"));count.setForeground(Theme.MUTED);updateButtons();
    }
}
