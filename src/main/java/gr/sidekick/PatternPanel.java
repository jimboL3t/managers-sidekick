// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.*;
import static gr.sidekick.Model.*;

/** All edits are staged until the administrator accepts the dialog. */
public final class PatternPanel extends JPanel {
 private final Team source;private final YearMonth target;
 private final JCheckBox enabled,renew;
 private final JSpinner lookback;
 private final JComboBox<String> strength;
 private final Map<String,JCheckBox> months=new LinkedHashMap<>(),people=new LinkedHashMap<>();
 private final JTextArea preview=new JTextArea(12,70);
 public PatternPanel(Team team,YearMonth month){
  super(new BorderLayout(12,12));source=team;target=month;
  enabled=new JCheckBox(I18n.text("Χρήση προτύπων από εγκεκριμένους μήνες"),team.patternsEnabled);
  lookback=new JSpinner(new SpinnerNumberModel(Math.max(1,Math.min(12,team.patternLookback)),1,12,1));
  strength=new JComboBox<>(new String[]{I18n.text("Μικρή"),I18n.text("Μεσαία"),I18n.text("Μεγάλη")});strength.setSelectedIndex(Math.max(0,Math.min(2,team.patternStrength-1)));
  JPanel settings=new JPanel(new GridLayout(0,2,8,8));settings.add(enabled);settings.add(new JLabel(I18n.text("Δεν αλλάζει το πρόγραμμα μέχρι τον υπολογισμό")));
  settings.add(new JLabel(I18n.text("Προηγούμενοι μήνες αναφοράς (1–12)")));settings.add(lookback);
  settings.add(new JLabel(I18n.text("Επιρροή προτύπων")));settings.add(strength);add(settings,BorderLayout.NORTH);
  JPanel approved=new JPanel();approved.setLayout(new BoxLayout(approved,BoxLayout.Y_AXIS));
  Set<String> keys=new TreeSet<>(team.months.keySet());keys.addAll(team.patternMonths.keySet());
  for(String key:keys)if(YearMonth.parse(key).isBefore(month)){
   JCheckBox box=new JCheckBox(key,team.patternMonths.containsKey(key));months.put(key,box);approved.add(box);
  }
  if(months.isEmpty())approved.add(new JLabel(I18n.text("Συμπληρώστε πρώτα έναν προηγούμενο μήνα.")));
  renew=new JCheckBox("<html><body style='width:230px'>"+I18n.text("Ανανέωση επιλεγμένων στιγμιοτύπων από το τρέχον ιστορικό")+"</body></html>");
  JPanel workers=new JPanel();workers.setLayout(new BoxLayout(workers,BoxLayout.Y_AXIS));
  for(Employee e:team.employees){JCheckBox box=new JCheckBox(e.name,!team.patternExcludedEmployees.contains(e.id));people.put(e.id,box);workers.add(box);}
  JTabbedPane tabs=new JTabbedPane();tabs.addTab(I18n.text("Μήνες για μάθηση"),new JScrollPane(approved));tabs.addTab(I18n.text("Συμμετοχή εργαζομένων"),new JScrollPane(workers));
  JPanel training=new JPanel(new BorderLayout(8,8));training.add(tabs);training.add(renew,BorderLayout.SOUTH);
  preview.setEditable(false);preview.setLineWrap(true);preview.setWrapStyleWord(true);
  JPanel explanation=new JPanel(new BorderLayout(8,8));JButton refresh=new JButton(I18n.text("Τι αναγνωρίζει το πρόγραμμα;"));refresh.addActionListener(e->updatePreview());explanation.add(refresh,BorderLayout.NORTH);explanation.add(new JScrollPane(preview));
  JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,training,explanation);split.setResizeWeight(.35);split.setPreferredSize(new Dimension(940,400));add(split);
  JTextArea help=new JTextArea(I18n.text("Επιλέξτε μόνο οριστικοποιημένους μήνες. Η έγκριση κρατά στιγμιότυπο· μεταγενέστερες αλλαγές δεν το αλλάζουν αυτόματα. Άδειες, ρεπό και αργίες δεν διδάσκουν πρότυπα. Αποεπιλογή μήνα αφαιρεί την έγκρισή του."));help.setEditable(false);help.setLineWrap(true);help.setWrapStyleWord(true);help.setRows(4);JScrollPane helpScroll=new JScrollPane(help);helpScroll.setPreferredSize(new Dimension(900,90));add(helpScroll,BorderLayout.SOUTH);
  updatePreview();
 }
 private Team staged(){
  Team t=new Team(source.name);t.posts=source.posts;t.employees=source.employees;t.months=source.months;
  t.patternsEnabled=enabled.isSelected();t.patternLookback=(Integer)lookback.getValue();t.patternStrength=strength.getSelectedIndex()+1;
  // Hidden approvals (target/future months) are not revoked by editing an older month.
  t.patternMonths.putAll(source.patternMonths);
  months.forEach((key,box)->{if(box.isSelected()){
   PatternMonth existing=source.patternMonths.get(key);
   t.patternMonths.put(key,existing==null||renew.isSelected()?HistoryPatterns.capture(source,YearMonth.parse(key)):existing);
  }else t.patternMonths.remove(key);});
  people.forEach((id,box)->{if(!box.isSelected())t.patternExcludedEmployees.add(id);});return t;
 }
 private void updatePreview(){preview.setText(new HistoryPatterns.Context(staged(),target).summary());preview.setCaretPosition(0);}
 public void save(){Team t=staged();source.patternsEnabled=t.patternsEnabled;source.patternLookback=t.patternLookback;source.patternStrength=t.patternStrength;source.patternMonths=t.patternMonths;source.patternExcludedEmployees=t.patternExcludedEmployees;}
}
