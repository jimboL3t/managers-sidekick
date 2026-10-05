// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import static gr.sidekick.Model.*;

/** Edits only the displayed month; canceling the employee dialog changes nothing. */
public final class WorkloadPanel extends JPanel {
    private final JComboBox<String> mode;
    private final JSpinner count;
    private static final String[] MODES={"auto","target","reserve"};
    public WorkloadPanel(Team team,Employee employee,YearMonth month){
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(I18n.text("Βάρδιες μόνο για τον μήνα: ")+month));
        mode=new JComboBox<>(new String[]{I18n.text("Αυτόματος στόχος"),I18n.text("Συγκεκριμένος στόχος βαρδιών"),I18n.text("Εφεδρικός — μόνο κενά κάλυψης")});
        WorkPlan plan=Workload.plan(team,employee,month);
        int selected=plan==null?0:"target".equals(plan.mode)?1:"reserve".equals(plan.mode)?2:0;
        mode.setSelectedIndex(selected);
        count=new JSpinner(new SpinnerNumberModel(Workload.target(team,employee,month),0,month.lengthOfMonth(),1));
        JLabel caption=new JLabel();
        Runnable update=()->{count.setEnabled(mode.getSelectedIndex()!=0);caption.setText(I18n.text(mode.getSelectedIndex()==2?"Ανώτατο όριο (όχι υποχρεωτικός στόχος)":"Στόχος βαρδιών"));};
        mode.addActionListener(e->update.run());update.run();
        add(new JLabel(I18n.text("Αυτόματος στόχος με τις τρέχουσες άδειες: ")+Workload.automatic(team,employee,month)));add(mode);add(caption);add(count);
        JTextArea note=new JTextArea(I18n.text("Ο συγκεκριμένος στόχος προσαρμόζει τα ρεπό. Ο εφεδρικός καλύπτει μόνο κενά έως το όριο· οι υπόλοιπες ημέρες μένουν χωρίς ανάθεση. Τα κλειδώματα και οι κανόνες ανάπαυσης διατηρούνται. Δεν αλλάζει η ρύθμιση άλλων μηνών."),4,42);
        note.setLineWrap(true);note.setWrapStyleWord(true);note.setEditable(false);note.setOpaque(false);add(note);
        for(Component child:getComponents())if(child instanceof JComponent component){
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            if(child instanceof JComboBox<?>||child instanceof JSpinner)component.setMaximumSize(new Dimension(Integer.MAX_VALUE,component.getPreferredSize().height));
        }
    }
    public void save(Team team,Employee employee,YearMonth month){
        try{count.commitEdit();}catch(java.text.ParseException ex){throw new IllegalArgumentException(I18n.text("Μη έγκυρος αριθμός βαρδιών"),ex);}
        Workload.set(team,employee,month,MODES[mode.getSelectedIndex()],(Integer)count.getValue());
    }
}
