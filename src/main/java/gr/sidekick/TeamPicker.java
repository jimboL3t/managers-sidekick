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
    private final JButton open,copy,delete;

    public TeamPicker(Runnable create,Consumer<Team> onOpen,Consumer<Team> onCopy,Consumer<Team> onDelete){
        super(new BorderLayout(0,24));setBorder(BorderFactory.createEmptyBorder(36,36,28,36));
        JPanel title=new JPanel(new GridLayout(0,1,0,10));
        JLabel brand=new JLabel("Manager’s Sidekick");brand.setForeground(Theme.ACCENT);brand.setFont(new Font("SansSerif",Font.BOLD,28));title.add(brand);
        JLabel heading=new JLabel("Ομάδες εργασίας");heading.setFont(new Font("SansSerif",Font.BOLD,23));title.add(heading);
        JLabel hint=new JLabel("Επιλέξτε ομάδα για να ανοίξετε το ημερολόγιο ή δημιουργήστε ανεξάρτητο αντίγραφο για δοκιμές.");hint.setForeground(Theme.MUTED);title.add(hint);title.add(count);add(title,BorderLayout.NORTH);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);list.setFixedCellHeight(88);
        list.setCellRenderer((items,team,index,selected,focus)->{
            JPanel row=new JPanel(new BorderLayout(12,8)){
                @Override protected void paintComponent(Graphics graphics){doLayout();for(Component child:getComponents())if(child instanceof Container container)container.doLayout();super.paintComponent(graphics);}
            };row.setBackground(selected?Theme.SELECTION:Theme.BACKGROUND);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,Theme.PANEL),BorderFactory.createEmptyBorder(14,18,14,18)));
            JPanel text=new JPanel(new GridLayout(2,1,0,8));text.setOpaque(false);
            JLabel name=new JLabel(team.name);name.setFont(new Font("SansSerif",Font.BOLD,18));text.add(name);
            JLabel detail=new JLabel(team.employees.size()+" εργαζόμενοι  ·  "+team.posts.size()+" πόστα  ·  "+team.months.size()+" μήνες");detail.setForeground(Theme.MUTED);text.add(detail);row.add(text,BorderLayout.CENTER);
            JLabel badge=new JLabel(team.sandbox?"ΔΟΚΙΜΑΣΤΙΚΟ ΑΝΤΙΓΡΑΦΟ":"ΟΜΑΔΑ ΕΡΓΑΣΙΑΣ");badge.setForeground(Theme.ACCENT);row.add(badge,BorderLayout.EAST);return row;
        });
        add(new JScrollPane(list));
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        actions.add(action("Νέα ομάδα",create));
        open=action("Άνοιγμα ημερολογίου",()->withSelection(onOpen));actions.add(open);
        copy=action("Αντίγραφο για δοκιμές",()->withSelection(onCopy));actions.add(copy);
        delete=action("Διαγραφή ομάδας",()->withSelection(onDelete));delete.setForeground(Theme.RED);actions.add(delete);
        add(actions,BorderLayout.SOUTH);
        list.addListSelectionListener(e->updateButtons());
        list.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){int index=list.locationToIndex(e.getPoint());if(e.getClickCount()==2&&index>=0&&list.getCellBounds(index,index).contains(e.getPoint()))withSelection(onOpen);}});
        list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"open");list.getActionMap().put("open",new AbstractAction(){public void actionPerformed(ActionEvent e){withSelection(onOpen);}});
        updateButtons();
    }
    private JButton action(String text,Runnable run){JButton b=new JButton(text);b.setFocusPainted(false);b.addActionListener(e->run.run());return b;}
    private void withSelection(Consumer<Team> action){Team selected=list.getSelectedValue();if(selected!=null)action.accept(selected);}
    private void updateButtons(){boolean selected=list.getSelectedValue()!=null;open.setEnabled(selected);copy.setEnabled(selected);delete.setEnabled(selected);}
    public void refresh(Data data,Team selected){
        model.clear();for(Team t:data.teams)model.addElement(t);
        if(selected!=null)list.setSelectedValue(selected,true);
        count.setText(data.teams.isEmpty()?"Δεν υπάρχουν ομάδες ακόμη. Ξεκινήστε με «Νέα ομάδα».":data.teams.size()+" διαθέσιμες ομάδες");count.setForeground(Theme.MUTED);updateButtons();
    }
}
