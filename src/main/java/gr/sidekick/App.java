package gr.sidekick;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import static gr.sidekick.Model.*;
import gr.sidekick.ScheduleTableModel.Choice;

public final class App extends JFrame {
    private final Storage storage=new Storage(Path.of("data","sidekick.json"));
    private Data data;
    private final JComboBox<Team> teams=new JComboBox<>();
    private final JComboBox<String> month=new JComboBox<>(new String[]{"Ιανουάριος","Φεβρουάριος","Μάρτιος","Απρίλιος","Μάιος","Ιούνιος","Ιούλιος","Αύγουστος","Σεπτέμβριος","Οκτώβριος","Νοέμβριος","Δεκέμβριος"});
    private final JLabel monthTitle=new JLabel();
    private final JLabel overview=new JLabel();
    private Set<Integer> uncovered=Set.of();
    private final JSpinner year=new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(),1900,2200,1));
    private final JTable grid=new MonthTable(this::team,this::ym,()->uncovered);
    private final JTextArea report=new JTextArea(6,100);
    private final JLabel status=new JLabel(" ");
    private boolean busy;
    private final Scheduler scheduler=new Scheduler();
    public static void main(String[] args) { SwingUtilities.invokeLater(()->{try{Theme.install();new App().setVisible(true);}catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"Αδυναμία εκκίνησης",JOptionPane.ERROR_MESSAGE);}}); }
    public App() throws Exception {
        super("Manager’s Sidekick · Προγραμματισμός βαρδιών");data=storage.load();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){if(!busy&&save())dispose();}});
        month.setSelectedIndex(LocalDate.now().getMonthValue()-1);
        year.setEditor(new JSpinner.NumberEditor(year,"0"));
        year.setPreferredSize(new Dimension(85,32));
        JPanel root=new JPanel(new BorderLayout(0,18));
        root.setBorder(BorderFactory.createEmptyBorder(22,24,16,24));setContentPane(root);
        JPanel heading=new JPanel(new BorderLayout());
        JLabel brand=new JLabel("Manager’s Sidekick");brand.setFont(new Font("SansSerif",Font.BOLD,24));brand.setForeground(Theme.ACCENT);
        heading.add(brand,BorderLayout.WEST);
        JLabel subtitle=new JLabel("Οργάνωση ομάδας · Μηνιαίο πρόγραμμα");subtitle.setForeground(Theme.MUTED);heading.add(subtitle,BorderLayout.EAST);
        JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        toolbar.add(new JLabel("Ομάδα"));toolbar.add(teams);
        button(toolbar,"Νέα ομάδα",this::addTeam);button(toolbar,"Ρυθμίσεις",this::settings);
        button(toolbar,"Πόστα",this::posts);button(toolbar,"Εργαζόμενοι",this::employees);button(toolbar,"Νέο είδος άδειας",this::leave);
        JPanel navigation=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        button(navigation,"‹",()->navigate(-1));navigation.add(month);navigation.add(year);button(navigation,"›",()->navigate(1));
        button(navigation,"Τρέχων μήνας",()->selectMonth(YearMonth.now()));
        button(navigation,"Ερχόμενος μήνας",()->selectMonth(YearMonth.now().plusMonths(1)));
        button(navigation,"Αργίες μήνα",this::holidays);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        button(actions,"Υπολογισμός βαρδιών",this::generate);button(actions,"Έλεγχος",this::validateMonth);button(actions,"Αποδέσμευση κελιού",this::unlock);
        button(actions,"Αποθήκευση",this::save);button(actions,"Εξαγωγή PDF",this::pdf);
        JPanel titlePanel=new JPanel(new GridLayout(2,1,0,6));
        monthTitle.setFont(new Font("SansSerif",Font.BOLD,22));overview.setForeground(Theme.MUTED);titlePanel.add(monthTitle);titlePanel.add(overview);
        JPanel controls=new JPanel();controls.setLayout(new BoxLayout(controls,BoxLayout.Y_AXIS));
        controls.add(heading);controls.add(Box.createVerticalStrut(14));controls.add(toolbar);controls.add(navigation);controls.add(actions);controls.add(Box.createVerticalStrut(12));controls.add(titlePanel);add(controls,BorderLayout.NORTH);
        report.setEditable(false);report.setLineWrap(true);report.setWrapStyleWord(true);report.setMargin(new Insets(12,12,12,12));report.setForeground(Theme.MUTED);
        JScrollPane schedule=new JScrollPane(grid);schedule.setColumnHeaderView(grid.getTableHeader());schedule.getViewport().setBackground(Theme.BACKGROUND);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,schedule,new JScrollPane(report));split.setResizeWeight(.85);split.setBorder(null);add(split);
        JPanel footer=new JPanel(new GridLayout(2,1,0,8));
        JLabel legend=new JLabel("★ Κλειδωμένο   ·   Μωβ φόντο: Σαββατοκύριακο   ·   Πράσινο φόντο: Αργία   ·   Κόκκινη ημερομηνία: ελλιπής κάλυψη");legend.setForeground(Theme.MUTED);
        footer.add(legend);footer.add(status);add(footer,BorderLayout.SOUTH);
        for(Team t:data.teams)teams.addItem(t);
        teams.addActionListener(e->refresh());month.addActionListener(e->refresh());year.addChangeListener(e->refresh());
        setMinimumSize(new Dimension(1100,700));setSize(1440,900);setLocationRelativeTo(null);refresh();
    }
    private void button(JPanel panel,String name,Runnable action) {JButton b=new JButton(name);b.setFocusPainted(false);b.setMargin(new Insets(8,12,8,12));b.addActionListener(e->{if(!busy)try{if(grid.isEditing())grid.getCellEditor().stopCellEditing();action.run();}catch(Exception ex){error(ex);}});panel.add(b);}
    private Team team(){return (Team)teams.getSelectedItem();}
    private YearMonth ym(){return YearMonth.of((Integer)year.getValue(),month.getSelectedIndex()+1);}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Σφάλμα",JOptionPane.ERROR_MESSAGE);}
    private boolean save(){try{storage.save(data);status.setText("Αποθηκεύτηκε στο data/sidekick.json");return true;}catch(Exception e){error(e);return false;}}
    private String name(String title){String s=JOptionPane.showInputDialog(this,title);return s==null||s.isBlank()?null:s.strip();}
    private void addTeam(){String n=name("Όνομα ομάδας");if(n==null)return;Team t=new Team(n);t.allowedLeaves.addAll(data.leaves.keySet());data.teams.add(t);teams.addItem(t);teams.setSelectedItem(t);save();}
    private void settings(){Team t=team();if(t==null)return;JPanel p=new JPanel(new GridLayout(0,2,6,6));
        JTextField n=new JTextField(t.name);JSpinner max=new JSpinner(new SpinnerNumberModel(t.maxConsecutive,1,7,1));JSpinner rest=new JSpinner(new SpinnerNumberModel(t.minRestHours,0,24,1));
        p.add(new JLabel("Όνομα"));p.add(n);p.add(new JLabel("Μέγιστες συνεχόμενες εργάσιμες"));p.add(max);p.add(new JLabel("Ελάχιστη ανάπαυση (ώρες)"));p.add(rest);p.add(new JLabel("Ρεπό μήνα"));p.add(new JLabel("Πλήθος Σαββάτων + Κυριακών + αργιών"));
        JCheckBox pair=new JCheckBox("Προτίμηση συνεχόμενων ρεπό",t.preferPaired);p.add(pair);p.add(new JLabel("Επιτρεπόμενες άδειες:"));
        Map<String,JCheckBox> boxes=new LinkedHashMap<>();data.leaves.forEach((id,label)->{JCheckBox b=new JCheckBox(label,t.allowedLeaves.contains(id));if(id.equals(OFF)){b.setSelected(true);b.setEnabled(false);}boxes.put(id,b);p.add(b);});
        if(JOptionPane.showConfirmDialog(this,p,"Ρυθμίσεις ομάδας",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION||n.getText().isBlank())return;
        t.name=n.getText().strip();t.maxConsecutive=(Integer)max.getValue();t.minRestHours=(Integer)rest.getValue();t.preferPaired=pair.isSelected();t.allowedLeaves.clear();boxes.forEach((id,b)->{if(b.isSelected())t.allowedLeaves.add(id);});save();refresh();}
    private void posts(){Team t=team();if(t==null)return;List<String> opts=new ArrayList<>();opts.add("+ Νέο πόστο");t.posts.forEach(p->opts.add(p.name));Object chosen=JOptionPane.showInputDialog(this,"Δημιουργία ή επεξεργασία","Πόστα",JOptionPane.PLAIN_MESSAGE,null,opts.toArray(),opts.getFirst());if(chosen==null)return;
        int idx=opts.indexOf(chosen);Post old=idx==0?null:t.posts.get(idx-1);JPanel p=new JPanel(new GridLayout(0,2,6,6));
        JTextField n=new JTextField(old==null?"":old.name),start=new JTextField(old==null?"08:00":old.start),end=new JTextField(old==null?"16:00":old.end);
        p.add(new JLabel("Όνομα"));p.add(n);p.add(new JLabel("Έναρξη HH:mm"));p.add(start);p.add(new JLabel("Λήξη HH:mm (νυχτερινή αν μικρότερη)"));p.add(end);
        String[] days={"Δευτέρα","Τρίτη","Τετάρτη","Πέμπτη","Παρασκευή","Σάββατο","Κυριακή"};JSpinner[] demands=new JSpinner[7];for(int i=0;i<7;i++){demands[i]=new JSpinner(new SpinnerNumberModel(old==null?1:Math.max(1,old.demand[i]),1,1000,1));p.add(new JLabel("Άτομα · "+days[i]));p.add(demands[i]);}
        if(JOptionPane.showConfirmDialog(this,p,"Πόστο · αλλαγές ισχύουν και στον έλεγχο ιστορικού",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(n.getText().isBlank())throw new IllegalArgumentException("Συμπληρώστε όνομα.");String a=LocalTime.parse(start.getText().strip()).toString(),b=LocalTime.parse(end.getText().strip()).toString();if(a.equals(b))throw new IllegalArgumentException("Η βάρδια πρέπει να διαρκεί λιγότερο από 24 ώρες.");
        Post post=old==null?new Post(n.getText().strip(),a,b):old;post.name=n.getText().strip();post.start=a;post.end=b;for(int i=0;i<7;i++)post.demand[i]=(Integer)demands[i].getValue();if(old==null)t.posts.add(post);save();refresh();}
    private void employees(){Team t=team();if(t==null)return;List<String> opts=new ArrayList<>();opts.add("+ Νέος εργαζόμενος");t.employees.forEach(e->opts.add(e.name));Object chosen=JOptionPane.showInputDialog(this,"Δημιουργία ή επεξεργασία","Εργαζόμενοι",JOptionPane.PLAIN_MESSAGE,null,opts.toArray(),opts.getFirst());if(chosen==null)return;int idx=opts.indexOf(chosen);Employee old=idx==0?null:t.employees.get(idx-1);
        JPanel p=new JPanel(new GridLayout(0,1));JTextField n=new JTextField(old==null?"":old.name,25);p.add(new JLabel("Όνομα εργαζομένου"));p.add(n);p.add(new JLabel("Επιτρεπόμενα πόστα"));Map<String,JCheckBox> boxes=new LinkedHashMap<>();for(Post post:t.posts){JCheckBox c=new JCheckBox(post.name,old!=null&&old.skills.contains(post.id));boxes.put(post.id,c);p.add(c);}
        if(JOptionPane.showConfirmDialog(this,p,"Εργαζόμενος",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION||n.getText().isBlank())return;Employee e=old==null?new Employee(n.getText().strip()):old;e.name=n.getText().strip();e.skills.clear();boxes.forEach((id,c)->{if(c.isSelected())e.skills.add(id);});if(old==null)t.employees.add(e);save();refresh();}
    private void leave(){String n=name("Όνομα νέου είδους άδειας");if(n==null)return;data.leaves.put(id(),n);save();status.setText("Η άδεια προστέθηκε. Ενεργοποιήστε την από τις Ρυθμίσεις ομάδας.");}
    private void refresh(){if(grid.isEditing())grid.getCellEditor().stopCellEditing();Team t=team();YearMonth date=ym();
        monthTitle.setText(month.getSelectedItem()+" "+year.getValue());
        grid.setModel(new ScheduleTableModel(data,t,date,()->{save();validateMonth();grid.getTableHeader().repaint();},()->!busy));
        grid.getColumnModel().getColumn(0).setPreferredWidth(235);for(int c=1;c<grid.getColumnCount();c++)grid.getColumnModel().getColumn(c).setPreferredWidth(104);
        grid.setDefaultEditor(Object.class,new DefaultCellEditor(new JComboBox<Choice>()){
            public Component getTableCellEditorComponent(JTable table,Object value,boolean selected,int row,int col){JComboBox<Choice> combo=new JComboBox<>();combo.addItem(new Choice(null,"— Κενό"));Employee e=t.employees.get(row);for(Post p:t.posts)combo.addItem(new Choice(p.id,p.name+(e.skills.contains(p.id)?"":" (εκτός δεξιοτήτων)")));for(String id:t.allowedLeaves)combo.addItem(new Choice(id,data.leaves.get(id)));Cell current=t.cell(e.id,date.atDay(col));for(int i=0;i<combo.getItemCount();i++)if(Objects.equals(combo.getItemAt(i).id(),current==null?null:current.value))combo.setSelectedIndex(i);combo.addActionListener(event->stopCellEditing());editorComponent=combo;return combo;}
            public Object getCellEditorValue(){return ((JComboBox<?>)editorComponent).getSelectedItem();}
        });validateMonth();
    }
    private void navigate(int delta){selectMonth(ym().plusMonths(delta));}
    private void selectMonth(YearMonth date){year.setValue(date.getYear());month.setSelectedIndex(date.getMonthValue()-1);}
    private void holidays(){
        Team t=team();if(t==null)return;YearMonth date=ym();JPanel days=new JPanel(new GridLayout(0,7,8,8));
        Map<Integer,JCheckBox> boxes=new LinkedHashMap<>();
        for(int d=1;d<=date.lengthOfMonth();d++){
            JCheckBox box=new JCheckBox(date.atDay(d).format(DateTimeFormatter.ofPattern("dd EEE",Locale.forLanguageTag("el"))),CalendarRules.holiday(t,date.atDay(d)));
            boxes.put(d,box);days.add(box);
        }
        JPanel panel=new JPanel(new BorderLayout(0,16));panel.add(new JLabel("Επιλέξτε αργίες. Οι βάρδιες αλλάζουν μόνο με «Υπολογισμός βαρδιών»."),BorderLayout.NORTH);panel.add(days);
        if(JOptionPane.showConfirmDialog(this,panel,"Αργίες · "+monthTitle.getText(),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        t.month(date).holidays.clear();boxes.forEach((d,box)->{if(box.isSelected())t.month(date).holidays.add(d);});save();refresh();
    }
    private void validateMonth(){
        uncovered=team()==null?Set.of():CalendarRules.uncoveredDays(team(),ym());
        grid.getTableHeader().repaint();
        if(team()==null){overview.setText("Δημιουργήστε την πρώτη σας ομάδα");report.setText("Προσθέστε πόστα και εργαζομένους για να ξεκινήσετε.");return;}
        Team t=team();
        overview.setText(t.employees.size()+" εργαζόμενοι   ·   "+t.posts.size()+" πόστα   ·   Στόχος "+CalendarRules.offTarget(t,ym())+" ρεπό / άτομο   ·   "+uncovered.size()+" ημέρες με ελλιπή κάλυψη");
        List<String> issues=scheduler.validate(data,t,ym());
        report.setText(issues.isEmpty()?"Το πρόγραμμα είναι πλήρες.":"Παρατηρήσεις προγράμματος — μπορείτε να συνεχίσετε τις αλλαγές.\n"+String.join("\n",issues));report.setCaretPosition(0);
    }
    private void unlock(){int r=grid.getSelectedRow(),c=grid.getSelectedColumn();if(team()==null||r<0||c<1)return;Cell cell=team().cell(team().employees.get(r).id,ym().atDay(c));if(cell!=null)cell.locked=false;save();refresh();}
    private void generate(){Team t=team();YearMonth date=ym();if(t==null||t.posts.isEmpty()||t.employees.isEmpty()){status.setText("Προσθέστε πόστα και εργαζομένους πριν τον υπολογισμό.");return;}busy=true;teams.setEnabled(false);month.setEnabled(false);year.setEnabled(false);status.setText("Υπολογισμός…");
        new SwingWorker<List<String>,Void>(){protected List<String> doInBackground(){return scheduler.generate(data,t,date);}protected void done(){busy=false;teams.setEnabled(true);month.setEnabled(true);year.setEnabled(true);try{get();save();refresh();status.setText("Ο υπολογισμός ολοκληρώθηκε. Ελέγξτε τις αποκλίσεις πριν χρησιμοποιήσετε το πρόγραμμα.");}catch(Exception e){error(e);}}}.execute();}
    private void pdf(){if(team()==null)return;JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("sidekick-"+ym()+".pdf"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;Path file=chooser.getSelectedFile().toPath();if(Files.exists(file)&&JOptionPane.showConfirmDialog(this,"Αντικατάσταση υπάρχοντος PDF;","PDF",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{PdfExporter.export(data,team(),ym(),file);status.setText("Δημιουργήθηκε PDF: "+file.toAbsolutePath());}catch(Exception e){error(e);}}
}
