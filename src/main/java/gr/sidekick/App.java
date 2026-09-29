package gr.sidekick;

import javax.swing.*;
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
    private final Storage storage=new Storage(DataPaths.file());
    private Data data;
    private Team activeTeam;
    private final CardLayout screens=new CardLayout();
    private final JPanel pages=new JPanel(screens);
    private TeamPicker teamPicker;
    private final JLabel teamHeading=new JLabel();
    private final JComboBox<String> month=new JComboBox<>();
    private final JLabel monthTitle=new JLabel();
    private final JLabel overview=new JLabel();
    private Set<Integer> uncovered=Set.of();
    private final JSpinner year=new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(),1900,2200,1));
    private final JTable grid=new MonthTable(this::team,this::ym,()->uncovered);
    private final JTextArea report=new JTextArea(6,100);
    private final JLabel status=new JLabel(" ");
    private boolean busy;
    private final JCheckBox dayLocks=new JCheckBox();
    private final Scheduler scheduler=new Scheduler();
    public static void main(String[] args) { SwingUtilities.invokeLater(()->{try{Theme.install();new App().setVisible(true);}catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),I18n.text("Αδυναμία εκκίνησης"),JOptionPane.ERROR_MESSAGE);}}); }
    public App() throws Exception {this(new Storage(DataPaths.file()).load());}
    private App(Data initial) throws Exception {
        super("Manager’s Sidekick");data=initial;I18n.setLanguage(data.language);
        setTitle(I18n.text("Manager’s Sidekick · Προγραμματισμός βαρδιών"));
        for(java.time.Month m:java.time.Month.values())month.addItem(m.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE,I18n.locale()));setIconImages(java.util.List.of(Logo.image(32),Logo.image(64),Logo.image(256)));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){if(!busy){if(grid.isEditing())grid.getCellEditor().stopCellEditing();if(save())dispose();}}});
        month.setSelectedIndex(LocalDate.now().getMonthValue()-1);
        year.setEditor(new JSpinner.NumberEditor(year,"0"));
        year.setPreferredSize(new Dimension(85,32));
        JPanel root=new JPanel(new BorderLayout(0,18));
        root.setBorder(BorderFactory.createEmptyBorder(22,24,16,24));pages.add(root,"calendar");setContentPane(pages);
        JPanel heading=new JPanel(new BorderLayout());
        JLabel brand=new JLabel("Manager’s Sidekick",Logo.icon(42),SwingConstants.LEFT);brand.setIconTextGap(12);brand.setFont(new Font("SansSerif",Font.BOLD,24));brand.setForeground(Theme.ACCENT);
        heading.add(brand,BorderLayout.WEST);
        teamHeading.setForeground(Theme.MUTED);heading.add(teamHeading,BorderLayout.EAST);
        JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        button(toolbar,I18n.text("‹ Ομάδες εργασίας"),this::showTeams);button(toolbar,I18n.text("Ρυθμίσεις"),this::settings);
        button(toolbar,I18n.text("Πόστα"),this::posts);button(toolbar,I18n.text("Δεξιότητες"),this::skills);button(toolbar,I18n.text("Εργαζόμενοι"),this::employees);button(toolbar,I18n.text("Νέο είδος άδειας"),this::leave);
        button(toolbar,I18n.text("Δίκαιη κατανομή"),this::fairness);
        JPanel navigation=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        button(navigation,"‹",()->navigate(-1));navigation.add(month);navigation.add(year);button(navigation,"›",()->navigate(1));
        button(navigation,I18n.text("Τρέχων μήνας"),()->selectMonth(YearMonth.now()));
        button(navigation,I18n.text("Ερχόμενος μήνας"),()->selectMonth(YearMonth.now().plusMonths(1)));
        button(navigation,I18n.text("Αργίες μήνα"),this::holidays);
        JPanel actions=new GlassPanel(new BorderLayout(16,10));
        JPanel planning=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        button(planning,I18n.text("Υπολογισμός βαρδιών"),this::generate);button(planning,I18n.text("Έλεγχος"),this::validateMonth);
        button(planning,I18n.text("Άλλος συνδυασμός"),()->generate(true));
        dayLocks.setText(I18n.text("Λουκέτα ημερών"));navigation.add(dayLocks);
        dayLocks.addActionListener(e->{if(!dayLocks.isSelected()&&team()!=null&&!team().month(ym()).lockedDays.isEmpty()){dayLocks.setSelected(true);status.setText(I18n.text("Ξεκλειδώστε πρώτα τις ημέρες από τις επικεφαλίδες."));}grid.getTableHeader().repaint();});
        ((MonthTable)grid).setLockControls(dayLocks::isSelected);
        grid.getTableHeader().addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){
            if(busy||team()==null||!dayLocks.isSelected())return;
            int column=grid.columnAtPoint(e.getPoint());if(column<1)return;int day=grid.convertColumnIndexToModel(column);
            if(grid.isEditing())grid.getCellEditor().stopCellEditing();
            Set<Integer> locks=team().month(ym()).lockedDays;if(!locks.remove(day))locks.add(day);save();refresh();
        }});
        JPanel output=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        button(output,I18n.text("Αποθήκευση"),this::save);button(output,I18n.text("Εξαγωγή PDF"),this::pdf);
        JPanel adjustments=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        button(adjustments,I18n.text("Διαθεσιμότητα"),this::availability);button(adjustments,I18n.text("Ιστορικό"),this::history);
        button(adjustments,I18n.text("Αποδέσμευση κελιού"),this::unlock);button(adjustments,I18n.text("Καθαρισμός χειροκίνητων"),this::clearManual);
        planning.setOpaque(false);output.setOpaque(false);adjustments.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(14,12,14,12));actions.add(planning,BorderLayout.WEST);actions.add(output,BorderLayout.EAST);actions.add(adjustments,BorderLayout.SOUTH);
        JPanel titlePanel=new JPanel(new GridLayout(2,1,0,6));
        monthTitle.setFont(new Font("SansSerif",Font.BOLD,22));overview.setForeground(Theme.MUTED);titlePanel.add(monthTitle);titlePanel.add(overview);
        JPanel controls=new JPanel();controls.setLayout(new BoxLayout(controls,BoxLayout.Y_AXIS));
        controls.add(heading);controls.add(Box.createVerticalStrut(14));controls.add(toolbar);controls.add(navigation);controls.add(actions);controls.add(Box.createVerticalStrut(12));controls.add(titlePanel);root.add(controls,BorderLayout.NORTH);
        report.setEditable(false);report.setLineWrap(true);report.setWrapStyleWord(true);report.setMargin(new Insets(12,12,12,12));report.setForeground(Theme.MUTED);
        JScrollPane schedule=new JScrollPane(grid);schedule.setColumnHeaderView(grid.getTableHeader());schedule.getViewport().setBackground(Theme.BACKGROUND);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,schedule,new JScrollPane(report));split.setResizeWeight(.85);split.setBorder(null);root.add(split);
        JPanel footer=new JPanel(new GridLayout(2,1,0,8));
        JLabel legend=new JLabel(I18n.text("★ Κλειδωμένο   ·   Μωβ φόντο: Σαββατοκύριακο   ·   Πράσινο φόντο: Αργία   ·   Κόκκινη ημερομηνία: ελλιπής κάλυψη"));legend.setForeground(Theme.MUTED);
        footer.add(legend);footer.add(status);root.add(footer,BorderLayout.SOUTH);
        teamPicker=new TeamPicker(this::addTeam,this::openTeam,this::copyTeam,this::deleteTeam,this::annualSummary,this::changeLanguage);
        pages.add(teamPicker,"teams");
        month.addActionListener(e->refresh());year.addChangeListener(e->refresh());
        setMinimumSize(new Dimension(1100,700));setSize(1440,900);setLocationRelativeTo(null);refresh();showTeams();
    }
    private void button(JPanel panel,String name,Runnable action) {ActionButton.Style style=switch(I18n.greek(name)){case "Αποθήκευση"->ActionButton.Style.PRIMARY;case "Εξαγωγή PDF"->ActionButton.Style.EXPORT;case "Καθαρισμός χειροκίνητων"->ActionButton.Style.DANGER;case "Αποδέσμευση κελιού","‹ Ομάδες εργασίας"->ActionButton.Style.QUIET;default->ActionButton.Style.STANDARD;};JButton b=new ActionButton(name,style);
        if(name.equals(I18n.text("Αποθήκευση")))b.setToolTipText(I18n.text("Αποθήκευση όλων των δεδομένων στον υπολογιστή"));if(name.equals(I18n.text("Εξαγωγή PDF")))b.setToolTipText(I18n.text("Μηνιαίο πρόγραμμα και σύνολα σε οριζόντιο Α4"));b.addActionListener(e->{if(!busy)try{if(grid.isEditing())grid.getCellEditor().stopCellEditing();action.run();}catch(Exception ex){error(ex);}});panel.add(b);}
    private Team team(){return activeTeam;}
    private YearMonth ym(){return YearMonth.of((Integer)year.getValue(),month.getSelectedIndex()+1);}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),I18n.text("Σφάλμα"),JOptionPane.ERROR_MESSAGE);}
    private boolean save(){try{storage.save(data);status.setText(I18n.text("Αποθηκεύτηκε · ")+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))+" · "+DataPaths.file().toAbsolutePath());return true;}catch(Exception e){error(e);return false;}}
    private String name(String title){String s=JOptionPane.showInputDialog(this,title);return s==null||s.isBlank()?null:s.strip();}
    private void changeLanguage(String language){
        if(busy||language.equals(data.language))return;
        String previous=data.language;data.language=language;I18n.setLanguage(language);
        if(!save()){data.language=previous;I18n.setLanguage(previous);return;}
        try{App replacement=new App(data);replacement.setBounds(getBounds());replacement.setVisible(true);dispose();}
        catch(Exception e){data.language=previous;I18n.setLanguage(previous);save();error(e);}
    }
    private void annualSummary(Team selected){
        JDialog dialog=new JDialog(this,I18n.text("Ετήσια εικόνα · ")+selected.name,true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new YearSummaryPanel(data,selected));dialog.setSize(1100,650);dialog.setMinimumSize(new Dimension(850,450));dialog.setLocationRelativeTo(this);dialog.setVisible(true);
    }
    private void showTeams(){
        if(busy)return;
        if(grid.isEditing())grid.getCellEditor().stopCellEditing();
        Team previous=activeTeam;activeTeam=null;
        teamPicker.refresh(data,previous);screens.show(pages,"teams");
        setTitle(I18n.text("Manager’s Sidekick · Ομάδες εργασίας"));
    }
    private void openTeam(Team team){
        if(!data.teams.contains(team))return;
        activeTeam=team;dayLocks.setSelected(!team.month(ym()).lockedDays.isEmpty());refresh();screens.show(pages,"calendar");
        setTitle("Manager’s Sidekick · "+team.name+(team.sandbox?I18n.text(" · Δοκιμαστικό αντίγραφο"):""));
    }
    private void addTeam(){
        String n=name(I18n.text("Όνομα νέας ομάδας"));if(n==null)return;
        Team t=TeamService.create(data,n);
        if(!save()){data.teams.remove(t);return;}
        teamPicker.refresh(data,t);
    }
    private void copyTeam(Team source){
        String n=JOptionPane.showInputDialog(this,I18n.text("Όνομα ανεξάρτητου αντιγράφου"),source.name+I18n.text(" — Δοκιμή"));
        if(n==null||n.isBlank())return;
        Team copy=TeamService.duplicate(data,source,n);
        if(!save()){data.teams.remove(copy);return;}
        teamPicker.refresh(data,copy);
    }
    private void deleteTeam(Team selected){
        String message=I18n.text("Διαγραφή της ομάδας «")+selected.name+I18n.text("»;\nΘα αφαιρεθούν ")+selected.employees.size()+I18n.text(" εργαζόμενοι, ")+selected.posts.size()+I18n.text(" πόστα και ")+selected.months.size()+I18n.text(" μήνες προγράμματος.\nΟι άλλες ομάδες και τα αντίγραφά τους δεν επηρεάζονται.");
        Object[] options={I18n.text("Ακύρωση"),I18n.text("Διαγραφή ομάδας")};
        if(JOptionPane.showOptionDialog(this,message,I18n.text("Διαγραφή ομάδας"),JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0])!=1)return;
        int index=data.teams.indexOf(selected);if(index<0)return;
        TeamService.delete(data,selected);
        if(!save()){data.teams.add(index,selected);return;}
        teamPicker.refresh(data,null);
    }
    private void settings(){Team t=team();if(t==null)return;JPanel p=new JPanel(new GridLayout(0,2,6,6));
        JTextField n=new JTextField(t.name);JSpinner max=new JSpinner(new SpinnerNumberModel(t.maxConsecutive,1,7,1));JSpinner rest=new JSpinner(new SpinnerNumberModel(t.minRestHours,0,24,1));
        p.add(new JLabel(I18n.text("Όνομα")));p.add(n);p.add(new JLabel(I18n.text("Μέγιστες συνεχόμενες εργάσιμες")));p.add(max);p.add(new JLabel(I18n.text("Ελάχιστη ανάπαυση (ώρες)")));p.add(rest);p.add(new JLabel(I18n.text("Ρεπό μήνα")));p.add(new JLabel(I18n.text("Πλήθος Σαββάτων + Κυριακών + αργιών")));
        JCheckBox pair=new JCheckBox(I18n.text("Προτίμηση συνεχόμενων ρεπό"),t.preferPaired);p.add(pair);p.add(new JLabel(I18n.text("Επιτρεπόμενες άδειες:")));
        Map<String,JCheckBox> boxes=new LinkedHashMap<>();leaves(data,t).forEach((id,label)->{JCheckBox b=new JCheckBox(Model.label(data,t,id),t.allowedLeaves.contains(id));if(id.equals(OFF)){b.setSelected(true);b.setEnabled(false);}boxes.put(id,b);p.add(b);});
        if(JOptionPane.showConfirmDialog(this,p,I18n.text("Ρυθμίσεις ομάδας"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION||n.getText().isBlank())return;
        t.name=n.getText().strip();t.maxConsecutive=(Integer)max.getValue();t.minRestHours=(Integer)rest.getValue();t.preferPaired=pair.isSelected();t.allowedLeaves.clear();boxes.forEach((id,b)->{if(b.isSelected())t.allowedLeaves.add(id);});save();refresh();}
    private void posts(){Team t=team();if(t==null)return;List<String> opts=new ArrayList<>();opts.add(I18n.text("+ Νέο πόστο"));t.posts.forEach(p->opts.add(p.name));Object chosen=JOptionPane.showInputDialog(this,I18n.text("Δημιουργία ή επεξεργασία"),I18n.text("Πόστα"),JOptionPane.PLAIN_MESSAGE,null,opts.toArray(),opts.getFirst());if(chosen==null)return;
        int idx=opts.indexOf(chosen);Post old=idx==0?null:t.posts.get(idx-1);JPanel p=new JPanel(new GridLayout(0,2,6,6));
        JTextField n=new JTextField(old==null?"":old.name),start=new JTextField(old==null?"08:00":old.start),end=new JTextField(old==null?"16:00":old.end);
        p.add(new JLabel(I18n.text("Όνομα")));p.add(n);p.add(new JLabel(I18n.text("Έναρξη HH:mm")));p.add(start);p.add(new JLabel(I18n.text("Λήξη HH:mm (νυχτερινή αν μικρότερη)")));p.add(end);
        JCheckBox night=new JCheckBox(I18n.text("Νυχτερινό πόστο"),old!=null&&old.nightDuty);p.add(night);p.add(new JLabel(I18n.text("Μετρά στην εξισορρόπηση νυχτερινών")));
        String[] days={I18n.text("Δευτέρα"),I18n.text("Τρίτη"),I18n.text("Τετάρτη"),I18n.text("Πέμπτη"),I18n.text("Παρασκευή"),I18n.text("Σάββατο"),I18n.text("Κυριακή")};JSpinner[] demands=new JSpinner[7];JCheckBox[] operating=new JCheckBox[7];
        JPanel presets=new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));
        JButton daily=new JButton(I18n.text("Κάθε μέρα")),weekdays=new JButton(I18n.text("Μόνο καθημερινές"));presets.add(daily);presets.add(weekdays);
        p.add(new JLabel(I18n.text("Ημέρες λειτουργίας")));p.add(presets);
        for(int i=0;i<7;i++){
            boolean enabled=old==null||old.operatingDays==null||old.operatingDays[i];
            operating[i]=new JCheckBox(days[i],enabled);demands[i]=new JSpinner(new SpinnerNumberModel(old==null?1:Math.max(1,old.demand[i]),1,1000,1));demands[i].setEnabled(enabled);
            JSpinner spinner=demands[i];JCheckBox box=operating[i];box.addActionListener(e->spinner.setEnabled(box.isSelected()));
            p.add(box);p.add(spinner);
        }
        daily.addActionListener(e->{for(int i=0;i<7;i++){operating[i].setSelected(true);demands[i].setEnabled(true);}});
        weekdays.addActionListener(e->{for(int i=0;i<7;i++){operating[i].setSelected(i<5);demands[i].setEnabled(i<5);}});
        p.add(new JLabel(I18n.text("Επιλεγμένη ημέρα: απαιτούμενα άτομα")));p.add(new JLabel(I18n.text("Χωρίς επιλογή: το πόστο δεν λειτουργεί")));
        if(JOptionPane.showConfirmDialog(this,p,I18n.text("Πόστο · αλλαγές ισχύουν και στον έλεγχο ιστορικού"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(n.getText().isBlank())throw new IllegalArgumentException(I18n.text("Συμπληρώστε όνομα."));String a=LocalTime.parse(start.getText().strip()).toString(),b=LocalTime.parse(end.getText().strip()).toString();if(a.equals(b))throw new IllegalArgumentException(I18n.text("Η βάρδια πρέπει να διαρκεί λιγότερο από 24 ώρες."));
        Post post=old==null?new Post(n.getText().strip(),a,b):old;post.name=n.getText().strip();post.nightDuty=night.isSelected();post.start=a;post.end=b;post.operatingDays=new boolean[7];for(int i=0;i<7;i++){post.demand[i]=(Integer)demands[i].getValue();post.operatingDays[i]=operating[i].isSelected();}if(old==null)t.posts.add(post);save();refresh();}
    private void employees(){Team t=team();if(t==null)return;List<String> opts=new ArrayList<>();opts.add(I18n.text("+ Νέος εργαζόμενος"));t.employees.forEach(e->opts.add(e.name));Object chosen=JOptionPane.showInputDialog(this,I18n.text("Δημιουργία ή επεξεργασία"),I18n.text("Εργαζόμενοι"),JOptionPane.PLAIN_MESSAGE,null,opts.toArray(),opts.getFirst());if(chosen==null)return;int idx=opts.indexOf(chosen);Employee old=idx==0?null:t.employees.get(idx-1);
        JPanel p=new JPanel(new GridLayout(0,1));JTextField n=new JTextField(old==null?"":old.name,25);p.add(new JLabel(I18n.text("Όνομα εργαζομένου")));p.add(n);p.add(new JLabel(I18n.text("Επιτρεπόμενα πόστα")));Map<String,JCheckBox> boxes=new LinkedHashMap<>();for(Post post:t.posts){JCheckBox c=new JCheckBox(post.name,old!=null&&old.skills.contains(post.id));boxes.put(post.id,c);p.add(c);}
        if(JOptionPane.showConfirmDialog(this,p,I18n.text("Εργαζόμενος"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION||n.getText().isBlank())return;Employee e=old==null?new Employee(n.getText().strip()):old;e.name=n.getText().strip();e.skills.clear();boxes.forEach((id,c)->{if(c.isSelected())e.skills.add(id);});if(old==null)t.employees.add(e);save();refresh();}
    private void leave(){Team t=team();if(t==null)return;String n=name(I18n.text("Όνομα νέου είδους άδειας"));if(n==null)return;String id=id();t.leaveTypes.put(id,n);t.allowedLeaves.add(id);save();refresh();status.setText(I18n.text("Η άδεια προστέθηκε μόνο στην ομάδα «")+t.name+"».");}
    private void refresh(){
        if(team()!=null&&!team().month(ym()).lockedDays.isEmpty())dayLocks.setSelected(true);if(grid.isEditing())grid.getCellEditor().stopCellEditing();Team t=team();YearMonth date=ym();
        teamHeading.setText(t==null?"":t.name+(t.sandbox?I18n.text(" · Δοκιμαστικό αντίγραφο"):I18n.text(" · Ομάδα εργασίας")));
        monthTitle.setText(month.getSelectedItem()+" "+year.getValue());
        grid.setModel(new ScheduleTableModel(data,t,date,()->{save();validateMonth();grid.getTableHeader().repaint();},()->!busy));
        grid.getColumnModel().getColumn(0).setPreferredWidth(340);for(int c=1;c<grid.getColumnCount();c++)grid.getColumnModel().getColumn(c).setPreferredWidth(104);
        grid.setDefaultEditor(Object.class,new DefaultCellEditor(new JComboBox<Choice>()){
            public Component getTableCellEditorComponent(JTable table,Object value,boolean selected,int row,int col){JComboBox<Choice> combo=new JComboBox<>();combo.addItem(new Choice(null,I18n.text("— Κενό")));Employee e=t.employees.get(row);for(Post p:t.posts)combo.addItem(new Choice(p.id,p.name+(p.operates(date.atDay(col))?"":I18n.text(" (εκτός ημερών λειτουργίας)"))+(e.skills.contains(p.id)?"":I18n.text(" (εκτός δεξιοτήτων)"))));for(String id:t.allowedLeaves)combo.addItem(new Choice(id,label(data,t,id)));Cell current=t.cell(e.id,date.atDay(col));for(int i=0;i<combo.getItemCount();i++)if(Objects.equals(combo.getItemAt(i).id(),current==null?null:current.value))combo.setSelectedIndex(i);combo.addActionListener(event->stopCellEditing());editorComponent=combo;return combo;}
            public Object getCellEditorValue(){return ((JComboBox<?>)editorComponent).getSelectedItem();}
        });validateMonth();
    }
    private void navigate(int delta){selectMonth(ym().plusMonths(delta));}
    private void selectMonth(YearMonth date){year.setValue(date.getYear());month.setSelectedIndex(date.getMonthValue()-1);}
    private void holidays(){
        Team t=team();if(t==null)return;YearMonth date=ym();JPanel days=new JPanel(new GridLayout(0,7,8,8));
        Map<Integer,JCheckBox> boxes=new LinkedHashMap<>();
        for(int d=1;d<=date.lengthOfMonth();d++){
            JCheckBox box=new JCheckBox(date.atDay(d).format(DateTimeFormatter.ofPattern("dd EEE",I18n.locale())),CalendarRules.holiday(t,date.atDay(d)));
            boxes.put(d,box);days.add(box);
        }
        JPanel panel=new JPanel(new BorderLayout(0,16));panel.add(new JLabel(I18n.text("Επιλέξτε αργίες. Οι βάρδιες αλλάζουν μόνο με «Υπολογισμός βαρδιών».")),BorderLayout.NORTH);panel.add(days);
        if(JOptionPane.showConfirmDialog(this,panel,I18n.text("Αργίες · ")+monthTitle.getText(),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        t.month(date).holidays.clear();boxes.forEach((d,box)->{if(box.isSelected())t.month(date).holidays.add(d);});save();refresh();
    }
    private void validateMonth(){
        uncovered=team()==null?Set.of():CalendarRules.uncoveredDays(team(),ym());
        grid.getTableHeader().repaint();
        if(team()==null){overview.setText(I18n.text("Δημιουργήστε την πρώτη σας ομάδα"));report.setText(I18n.text("Προσθέστε πόστα και εργαζομένους για να ξεκινήσετε."));return;}
        Team t=team();
        long missingSkills=t.employees.stream().filter(e->t.posts.stream().noneMatch(p->e.skills.contains(p.id))).count();
        overview.setText(t.employees.size()+I18n.text(" εργαζόμενοι   ·   ")+t.posts.size()+I18n.text(" πόστα   ·   Στόχος ")+CalendarRules.offTarget(t,ym())+I18n.text(" ρεπό / άτομο   ·   ")+uncovered.size()+I18n.text(" ημέρες με ελλιπή κάλυψη")+(missingSkills>0?"   ·   "+missingSkills+I18n.text(" άτομα χωρίς επιτρεπόμενα πόστα"):""));
        List<String> issues=scheduler.validate(data,t,ym());
        report.setText(issues.isEmpty()?I18n.text("Το πρόγραμμα είναι πλήρες."):I18n.text("Παρατηρήσεις προγράμματος — μπορείτε να συνεχίσετε τις αλλαγές.\n")+String.join("\n",issues));report.setCaretPosition(0);
    }
    private void skills(){
        Team t=team();if(t==null)return;
        JPanel panel=new JPanel(new GridLayout(0,t.posts.size()+1,8,8));
        panel.add(new JLabel(I18n.text("Εργαζόμενος")));for(Post p:t.posts)panel.add(new JLabel(p.name));
        Map<Employee,Map<String,JCheckBox>> boxes=new LinkedHashMap<>();
        for(Employee e:t.employees){panel.add(new JLabel(e.name));Map<String,JCheckBox> row=new LinkedHashMap<>();
            for(Post p:t.posts){JCheckBox b=new JCheckBox("",e.skills.contains(p.id));panel.add(b);row.put(p.id,b);}boxes.put(e,row);
        }
        JPanel content=new JPanel(new BorderLayout(0,12));content.add(new JLabel(I18n.text("Επιλέξτε τα πόστα που μπορεί να αναλάβει κάθε εργαζόμενος. Κενή γραμμή = καμία αυτόματη βάρδια.")),BorderLayout.NORTH);
        JScrollPane scroll=new JScrollPane(panel);scroll.setPreferredSize(new Dimension(800,Math.min(450,70+t.employees.size()*35)));content.add(scroll);
        if(JOptionPane.showConfirmDialog(this,content,I18n.text("Δεξιότητες ομάδας"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        boxes.forEach((e,row)->{e.skills.clear();row.forEach((id,b)->{if(b.isSelected())e.skills.add(id);});});save();refresh();
    }
    private void clearManual(){
        Team t=team();if(t==null)return;
        if(JOptionPane.showConfirmDialog(this,I18n.text("Να αφαιρεθούν όλες οι χειροκίνητες επιλογές της ομάδας «")+t.name+I18n.text("» για ")+monthTitle.getText()+I18n.text(";\nΟι αυτόματες αναθέσεις, οι αργίες και οι άλλοι μήνες διατηρούνται. Πατήστε μετά Υπολογισμός βαρδιών."),I18n.text("Καθαρισμός χειροκίνητων"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        Scheduler.clearManual(t,ym());save();refresh();
    }
    private void unlock(){int r=grid.getSelectedRow(),c=grid.getSelectedColumn();if(team()==null||r<0||c<1)return;if(team().month(ym()).lockedDays.contains(c))return;Cell cell=team().cell(team().employees.get(r).id,ym().atDay(c));if(cell!=null)cell.locked=false;save();refresh();}
    private void generate(){generate(false);}
    private void generate(boolean alternative){Team t=team();YearMonth date=ym();if(t==null||t.posts.isEmpty()||t.employees.isEmpty()){status.setText(I18n.text("Προσθέστε πόστα και εργαζομένους πριν τον υπολογισμό."));return;}String beforeFairness=Fairness.summary(t,date);History.capture(t,date,I18n.text("Πριν τον υπολογισμό"));if(!save()){t.history.removeLast();return;}busy=true;month.setEnabled(false);year.setEnabled(false);status.setText(I18n.text("Υπολογισμός…"));
        new SwingWorker<List<String>,Void>(){protected List<String> doInBackground(){return scheduler.generate(data,t,date,alternative?java.util.concurrent.ThreadLocalRandom.current().nextLong():42L);}protected void done(){busy=false;month.setEnabled(true);year.setEnabled(true);try{get();save();refresh();if(t.nightBalanceWeight>0||t.weekendBalanceWeight>0)report.append("\n"+I18n.text("Πριν:\n")+beforeFairness+"\n"+I18n.text("Μετά:\n")+Fairness.summary(t,date));status.setText(I18n.text("Ο υπολογισμός ολοκληρώθηκε. Ελέγξτε τις αποκλίσεις πριν χρησιμοποιήσετε το πρόγραμμα."));}catch(Exception e){error(e);}}}.execute();}
    private void fairness(){
        Team t=team();if(t==null)return;JPanel panel=new JPanel(new BorderLayout(10,14));JPanel settings=new JPanel(new GridLayout(0,2,8,8));
        JSpinner nights=new JSpinner(new SpinnerNumberModel(t.nightBalanceWeight,0,10,1)),weekends=new JSpinner(new SpinnerNumberModel(t.weekendBalanceWeight,0,10,1)),lookback=new JSpinner(new SpinnerNumberModel(t.fairnessLookback,0,12,1));
        settings.add(new JLabel(I18n.text("Βάρος νυχτερινών (0 = ανενεργό)")));settings.add(nights);settings.add(new JLabel(I18n.text("Βάρος Σαββατοκύριακων (0 = ανενεργό)")));settings.add(weekends);settings.add(new JLabel(I18n.text("Προηγούμενοι μήνες αναφοράς (0–12)")));settings.add(lookback);
        settings.add(new JLabel(I18n.text("Ορίστε τα νυχτερινά από τα Πόστα.")));settings.add(new JLabel(I18n.text("Μετρά η ημέρα έναρξης κάθε βάρδιας.")));panel.add(settings,BorderLayout.NORTH);
        JPanel people=new JPanel(new GridLayout(0,5,8,8));for(String header:new String[]{"Εργαζόμενος","Συμμετοχή νυχτών","Συμμετοχή Σ/Κ","Όχι νύχτες","Όχι Σ/Κ"})people.add(new JLabel(I18n.text(header)));
        Map<Employee,JCheckBox[]> boxes=new LinkedHashMap<>();for(Employee e:t.employees){people.add(new JLabel(e.name));JCheckBox[] row={new JCheckBox("",!e.excludeNightBalance),new JCheckBox("",!e.excludeWeekendBalance),new JCheckBox("",e.noNights),new JCheckBox("",e.noWeekends)};for(JCheckBox b:row)people.add(b);boxes.put(e,row);}
        JScrollPane employees=new JScrollPane(people);employees.setPreferredSize(new Dimension(920,200));panel.add(employees);
        JTextArea metrics=new JTextArea(Fairness.summary(t,ym()),9,75);metrics.setEditable(false);panel.add(new JScrollPane(metrics),BorderLayout.SOUTH);
        if(JOptionPane.showConfirmDialog(this,panel,I18n.text("Δίκαιη κατανομή"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        t.nightBalanceWeight=(Integer)nights.getValue();t.weekendBalanceWeight=(Integer)weekends.getValue();t.fairnessLookback=(Integer)lookback.getValue();boxes.forEach((e,b)->{e.excludeNightBalance=!b[0].isSelected();e.excludeWeekendBalance=!b[1].isSelected();e.noNights=b[2].isSelected();e.noWeekends=b[3].isSelected();});save();refresh();
    }
    private void availability(){
        Team t=team();if(t==null||t.employees.isEmpty())return;
        String[] names=t.employees.stream().map(e->e.name).toArray(String[]::new);
        JComboBox<String> select=new JComboBox<>(names);
        if(JOptionPane.showConfirmDialog(this,select,I18n.text("Διαθεσιμότητα"),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        Employee person=t.employees.get(select.getSelectedIndex());JPanel panel=new JPanel(new BorderLayout(8,12));JPanel week=new JPanel(new GridLayout(0,2,8,8));JCheckBox[] days=new JCheckBox[7];
        for(int i=0;i<7;i++){days[i]=new JCheckBox(DayOfWeek.of(i+1).getDisplayName(java.time.format.TextStyle.FULL,I18n.locale()),person.availableWeekdays==null||person.availableWeekdays[i]);week.add(days[i]);}
        panel.add(week,BorderLayout.NORTH);JTextArea exceptions=new JTextArea(10,40);
        person.availabilityOverrides.forEach((d,v)->exceptions.append(d+"="+(v?"1":"0")+"\n"));panel.add(new JScrollPane(exceptions));
        panel.add(new JLabel(I18n.text("Εξαιρέσεις YYYY-MM-DD=0 (όχι) ή =1 (ναι), μία ανά γραμμή")),BorderLayout.SOUTH);
        if(JOptionPane.showConfirmDialog(this,panel,person.name,JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        Map<String,Boolean> parsed=Availability.parse(exceptions.getText());person.availableWeekdays=new boolean[7];for(int i=0;i<7;i++)person.availableWeekdays[i]=days[i].isSelected();person.availabilityOverrides=parsed;save();refresh();
    }
    private void history(){
        Team t=team();if(t==null)return;
        JDialog dialog=new JDialog(this,I18n.text("Ιστορικό"),true);DefaultListModel<Revision> entries=new DefaultListModel<>();t.history.reversed().forEach(entries::addElement);JList<Revision> revisions=new JList<>(entries);revisions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel content=new JPanel(new BorderLayout(12,12));content.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));content.add(new JScrollPane(revisions));JPanel actions=new JPanel(new FlowLayout());
        JButton capture=new ActionButton(I18n.text("Στιγμιότυπο μήνα"),ActionButton.Style.PRIMARY);capture.addActionListener(e->{String note=name(I18n.text("Περιγραφή στιγμιοτύπου"));if(note==null)return;Revision r=History.capture(t,ym(),note);if(save())entries.add(0,r);else t.history.remove(r);});actions.add(capture);
        JButton view=new ActionButton(I18n.text("Προβολή"),ActionButton.Style.STANDARD);view.addActionListener(e->{Revision r=revisions.getSelectedValue();if(r==null)return;Team frozen=History.read(r);YearMonth date=YearMonth.parse(r.month);JTable table=new JTable(new ScheduleTableModel(data,frozen,date,()->{},()->false));table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);table.getColumnModel().getColumn(0).setPreferredWidth(300);for(int i=1;i<table.getColumnCount();i++)table.getColumnModel().getColumn(i).setPreferredWidth(120);JScrollPane scroll=new JScrollPane(table);scroll.setPreferredSize(new Dimension(1000,400));JOptionPane.showMessageDialog(dialog,scroll,r.toString(),JOptionPane.PLAIN_MESSAGE);});actions.add(view);
        JButton export=new ActionButton(I18n.text("Εξαγωγή PDF"),ActionButton.Style.EXPORT);export.addActionListener(e->{Revision r=revisions.getSelectedValue();if(r==null)return;JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("history-"+r.month+".pdf"));if(chooser.showSaveDialog(dialog)!=JFileChooser.APPROVE_OPTION)return;Path file=chooser.getSelectedFile().toPath();if(Files.exists(file)&&JOptionPane.showConfirmDialog(dialog,I18n.text("Αντικατάσταση υπάρχοντος PDF;"),"PDF",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{PdfExporter.export(data,History.read(r),YearMonth.parse(r.month),file);}catch(Exception ex){error(ex);}});actions.add(export);
        JButton restore=new ActionButton(I18n.text("Επαναφορά ως νέα ομάδα"),ActionButton.Style.STANDARD);restore.addActionListener(e->{Revision r=revisions.getSelectedValue();if(r==null)return;String n=name(I18n.text("Όνομα νέας ομάδας"));if(n==null)return;Team copy=History.restoreCopy(data,r,n);if(!save())data.teams.remove(copy);});actions.add(restore);
        content.add(actions,BorderLayout.SOUTH);dialog.setContentPane(content);dialog.setSize(1050,550);dialog.setLocationRelativeTo(this);dialog.setVisible(true);
    }
    private void pdf(){if(team()==null)return;JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("sidekick-"+ym()+".pdf"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;Path file=chooser.getSelectedFile().toPath();if(Files.exists(file)&&JOptionPane.showConfirmDialog(this,I18n.text("Αντικατάσταση υπάρχοντος PDF;"),"PDF",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{PdfExporter.export(data,team(),ym(),file);status.setText(I18n.text("Δημιουργήθηκε PDF: ")+file.toAbsolutePath());}catch(Exception e){error(e);}}
}
