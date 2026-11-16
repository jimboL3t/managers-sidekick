// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.*;
import static gr.sidekick.Model.*;

/** Staged settings: opening or cancelling this panel never changes the team. */
public final class FairnessPanel extends JPanel {
    final JSpinner bands,nights,weekends,lookback;
    private final Map<Employee,JCheckBox[]> boxes=new LinkedHashMap<>();
    public FairnessPanel(Team t,YearMonth month){
        super(new BorderLayout(10,14));
        bands=spinner(t.shiftBalanceWeight,10);nights=spinner(t.nightBalanceWeight,10);weekends=spinner(t.weekendBalanceWeight,10);lookback=spinner(t.fairnessLookback,12);
        JPanel settings=new JPanel(new GridLayout(0,2,8,8));
        String[] labels={"Πρωί / Απόγευμα / Νύχτα (0 = ανενεργό)","Επιπλέον βάρος νυχτερινών (0 = ανενεργό)","Βάρος Σαββατοκύριακων (0 = ανενεργό)","Προηγούμενοι μήνες αναφοράς (0–12)"};
        JSpinner[] fields={bands,nights,weekends,lookback};for(int i=0;i<labels.length;i++){settings.add(new JLabel(I18n.text(labels[i])));settings.add(fields[i]);}
        JTextArea help=new JTextArea(I18n.text("Για παρόμοιες υπηρεσίες ενεργοποιήστε Πρωί / Απόγευμα / Νύχτα (π.χ. 5). Οι υπηρεσίες ίδιας κατηγορίας μετρούν μαζί.\n0 μήνες = ισορροπία μόνο στον τρέχοντα μήνα. Με ιστορικό αντισταθμίζονται προηγούμενες αποκλίσεις.\nΟ εφεδρικός ορίζεται ανά μήνα στις ρυθμίσεις εργαζομένου και εξαιρείται αυτόματα. Τα πρότυπα δεν ενεργοποιούν τη δικαιοσύνη."));help.setLineWrap(true);help.setWrapStyleWord(true);help.setEditable(false);help.setRows(4);
        JPanel north=new JPanel(new BorderLayout(0,8));north.add(settings,BorderLayout.NORTH);north.add(new JScrollPane(help));add(north,BorderLayout.NORTH);
        JPanel people=new JPanel(new GridLayout(0,6,8,8));
        for(String h:new String[]{"Εργαζόμενος","Συμμετοχή ωραρίων","Συμμετοχή νυχτών","Συμμετοχή Σ/Κ","Όχι νύχτες","Όχι Σ/Κ"})people.add(new JLabel(I18n.text(h)));
        for(Employee e:t.employees){people.add(new JLabel(e.name+(Workload.reserve(t,e,month)?I18n.text(" · Εφεδρικός"):"")));JCheckBox[] row={new JCheckBox("",!e.excludeShiftBalance),new JCheckBox("",!e.excludeNightBalance),new JCheckBox("",!e.excludeWeekendBalance),new JCheckBox("",e.noNights),new JCheckBox("",e.noWeekends)};for(int i=0;i<row.length;i++){if(i<3&&Workload.reserve(t,e,month))row[i].setEnabled(false);people.add(row[i]);}boxes.put(e,row);}
        JScrollPane list=new JScrollPane(people);list.setPreferredSize(new Dimension(940,200));add(list);
        JTextArea metrics=new JTextArea(Fairness.summary(t,month),10,80);metrics.setEditable(false);add(new JScrollPane(metrics),BorderLayout.SOUTH);
    }
    private static JSpinner spinner(int value,int max){return new JSpinner(new SpinnerNumberModel(value,0,max,1));}
    public void save(Team t){t.shiftBalanceWeight=(Integer)bands.getValue();t.nightBalanceWeight=(Integer)nights.getValue();t.weekendBalanceWeight=(Integer)weekends.getValue();t.fairnessLookback=(Integer)lookback.getValue();boxes.forEach((e,b)->{e.excludeShiftBalance=!b[0].isSelected();e.excludeNightBalance=!b[1].isSelected();e.excludeWeekendBalance=!b[2].isSelected();e.noNights=b[3].isSelected();e.noWeekends=b[4].isSelected();});}
}
