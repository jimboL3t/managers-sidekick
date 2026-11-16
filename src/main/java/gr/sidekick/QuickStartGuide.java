// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import java.nio.file.*;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;

/** English-first bilingual guide, generated with the same embedded font as schedule PDFs. */
public final class QuickStartGuide {
 private QuickStartGuide(){}
 public static void main(String[] args)throws IOException { export(Path.of(args[0])); }
 public static void export(Path path)throws IOException {
  try(PDDocument doc=new PDDocument()){
   PDFont font=PDType0Font.load(doc,PdfExporter.fontPath().toFile());
   page(doc,font,"Quick start",new String[]{
    "Launch on your operating system",
    "Windows: extract the entire ZIP to a writable folder. Open managersSidekick.exe when included, otherwise start-windows.cmd. Keep the complete application folder together.",
    "macOS: open the DMG and drag the app to Applications. For older portable packages, extract and open start-macos.command.",
    "Linux: extract the complete archive and run sh start-linux.sh from its directory in a graphical desktop session.",
    "Runtime bundles include Java. The generic test ZIP requires Java 21+; on macOS launch it with sh start-linux.sh.",
    "Zoom: team-selection display settings offer 50–200% overall scale. Below the calendar, Days (X) and Employees (Y) independently resize cells without changing assignments or PDF exports.",
    "Your first schedule",
    "1. Create and open a work team. Select the month and year.",
    "2. Create Duties: name, start/end times, operating days and staffing levels. Mark a duty Optional if it should only be covered by available staff after mandatory duties.",
    "3. Add employees. All existing duties are selected for a new employee; uncheck any they cannot perform. Adding a new duty later does not automatically change existing employees' skills.",
    "4. Set leave, availability and holidays. Team settings default to 5 consecutive working days and allow 1–10. Minimum rest remains a separate rule.",
    "5. Enter requested assignments, then click Calculate shifts. Manual assignments and locked days are preserved.",
    "6. Review notes and red dates, then Save and Export PDF. Unfilled optional duties do not count as mandatory coverage gaps.",
    "Data and backups",
    "Portable: data folder beside the application. Installed Mac app: ~/ManagersSidekick/data. Close the app and back up the entire data folder before upgrading. An older portable Mac database needs an initial manual transfer. Do not run two versions against the same database.",
    "Some actions save automatically. Use a team copy for experiments. See START-HERE and docs/ADMIN_GUIDE.en.md for full instructions."
   });
   page(doc,font,"Planning settings & history patterns",new String[]{
    "Find the right setting",
    "Team & duties: duties, leave types and change history. Employees: people, monthly targets, eligible duties and availability. Schedule planning: work limits, rest preferences, fair distribution and history patterns.",
    "Two consecutive rest days means any adjacent days. One Saturday and Sunday off together requests a full weekend once a month. Both are preferences, subject to coverage and locks.",
    "Fair shift mix: Schedule planning > Shift mix & weekend balance. Set Morning / Afternoon / Night to 5 (0 = off), reference months to 0 for this month only. Parallel duties count together. Set reserves in each employee monthly workload; they cover mandatory gaps only and are excluded from balance. See docs/FAIRNESS.en.md.",
    "Use approved history (optional; disabled by default)",
    "1. Enter and review previous months in the calendar. Select the future month to calculate, then open Schedule planning > Patterns from previous months.",
    "2. Select finalized Training months. Set a reference window of 1–12 months (default 6). Only approved months before the target and inside that window are used.",
    "3. Select participating employees and Low, Medium or High influence. Click What does the app recognize? to preview sample counts, time-band percentages and recurring triples.",
    "4. Enable patterns, accept and click Calculate shifts. Settings alone do not rearrange the schedule. An employee needs at least 5 eligible shifts; matching transitions need 3 observations.",
    "Morning: 06:00–13:59. Afternoon: 14:00–21:59. Night: 22:00–05:59 or explicitly marked night duty. Patterns learn time-band frequencies and adjacent pairs/triples, not exact weekly templates or duty names.",
    "Leave, rest days, empty cells and holidays do not train patterns and break sequences. Training does not join different months. Target holidays also receive no pattern score.",
    "Approvals store independent snapshots. Later schedule edits do not retrain them automatically. Use Refresh selected snapshots to explicitly replace them; uncheck a month to revoke approval. Cancel discards dialog changes.",
    "Patterns are soft preferences. Manual locks, eligibility, availability, rest and coverage take priority; configured fairness takes priority over pattern similarity in final ranking. Always review the result.",
    "All processing is local. Backups include approved snapshots. Use a copied team for experiments. Full instructions: docs/PATTERNS.en.md."
   });
   page(doc,font,"Γρήγορη εκκίνηση",new String[]{
    "Εκκίνηση ανά λειτουργικό",
    "Windows: αποσυμπιέστε ολόκληρο το ZIP σε εγγράψιμο φάκελο. Ανοίξτε το managersSidekick.exe, εφόσον περιλαμβάνεται, αλλιώς το start-windows.cmd. Κρατήστε όλα τα αρχεία μαζί.",
    "macOS: ανοίξτε το DMG και σύρετε την εφαρμογή στο Applications. Στο παλιό portable πακέτο αποσυμπιέστε και ανοίξτε το start-macos.command.",
    "Linux: αποσυμπιέστε ολόκληρο το πακέτο και εκτελέστε sh start-linux.sh μέσα στον φάκελό του, σε γραφικό περιβάλλον.",
    "Τα πακέτα με runtime περιλαμβάνουν Java. Το γενικό test ZIP απαιτεί εγκατεστημένη Java 21+ (στο Mac χρησιμοποιήστε sh start-linux.sh).",
    "Zoom: στην οθόνη ομάδων η συνολική κλίμακα είναι 50–200%. Κάτω από το ημερολόγιο, Ημέρες (Χ) και Εργαζόμενοι (Υ) αλλάζουν ανεξάρτητα το μέγεθος κελιών. Δεν αλλάζουν τις βάρδιες ή το PDF.",
    "Τα πρώτα βήματα",
    "1. Δημιουργήστε ομάδα και ανοίξτε την. Επιλέξτε μήνα και έτος.",
    "2. Δημιουργήστε τις Υπηρεσίες: όνομα, ώρες, ημέρες λειτουργίας και απαιτούμενα άτομα. Επιλέξτε Προαιρετική υπηρεσία αν καλύπτεται μόνο όταν περισσεύει διαθέσιμο προσωπικό.",
    "3. Προσθέστε εργαζομένους. Όλες οι υπάρχουσες υπηρεσίες είναι προεπιλεγμένες: αφαιρέστε όσες δεν μπορεί να κάνει ο εργαζόμενος. Οι νέες υπηρεσίες δεν προστίθενται αυτόματα στους ήδη υπάρχοντες εργαζομένους.",
    "4. Ρυθμίστε άδειες, διαθεσιμότητα και αργίες. Στις ρυθμίσεις ομάδας το όριο συνεχόμενης εργασίας είναι 5 ημέρες, με δυνατότητα επιλογής 1–10. Η ανάπαυση παραμένει ξεχωριστός κανόνας.",
    "5. Βάλτε τις επιθυμητές χειροκίνητες αναθέσεις και πατήστε Υπολογισμός βαρδιών. Οι χειροκίνητες επιλογές και οι κλειδωμένες ημέρες διατηρούνται.",
    "6. Ελέγξτε τις παρατηρήσεις και τις κόκκινες ημερομηνίες. Πατήστε Αποθήκευση και Εξαγωγή PDF. Οι ακάλυπτες προαιρετικές υπηρεσίες δεν αποτελούν έλλειμμα υποχρεωτικής κάλυψης.",
    "Δεδομένα και ασφάλεια",
    "Portable: φάκελος data δίπλα στην εφαρμογή. Mac app: ~/ManagersSidekick/data. Πριν από αναβάθμιση, κλείστε την εφαρμογή και αντιγράψτε όλο τον φάκελο data ως backup. Από παλιό portable Mac απαιτείται αρχική μεταφορά. Μην ανοίγετε δύο εκδόσεις στην ίδια βάση.",
    "Υπάρχουν ενέργειες που αποθηκεύουν αυτόματα. Για πειραματισμό χρησιμοποιήστε αντίγραφο ομάδας. Αναλυτικές οδηγίες: START-HERE και docs/ADMIN_GUIDE.md."
   });
   page(doc,font,"Ρυθμίσεις σχεδιασμού & πρότυπα ιστορικού",new String[]{
    "Πού βρίσκεται κάθε επιλογή",
    "Ομάδα & υπηρεσίες: υπηρεσίες, άδειες, ιστορικό αλλαγών. Εργαζόμενοι: στοιχεία, στόχοι, επιτρεπόμενες υπηρεσίες και διαθεσιμότητα. Σχεδιασμός προγράμματος: όρια εργασίας, ρεπό, δίκαιη κατανομή και πρότυπα.",
    "Δύο συνεχόμενα ρεπό μπορεί να είναι οποιεσδήποτε ημέρες. Ένα Σάββατο και Κυριακή μαζί ζητά πλήρες ΣΚ μία φορά τον μήνα. Είναι προτιμήσεις, εφόσον επιτρέπουν κάλυψη και κλειδώματα.",
    "Δίκαιη κατανομή: Σχεδιασμός προγράμματος > Κατανομή ωραρίων & ΣΚ. Ορίστε Πρωί / Απόγευμα / Νύχτα = 5 (0 = ανενεργό), μήνες αναφοράς = 0 για σύγκριση μόνο αυτού του μήνα. Οι παράλληλες υπηρεσίες μετρούν μαζί. Ορίστε εφεδρικούς στον μηνιαίο φόρτο: καλύπτουν μόνο υποχρεωτικά κενά και εξαιρούνται από τη σύγκριση. Βλ. docs/FAIRNESS.md.",
    "Χρήση εγκεκριμένου ιστορικού (αρχικά απενεργοποιημένη)",
    "1. Συμπληρώστε και ελέγξτε προηγούμενους μήνες στο ημερολόγιο. Επιλέξτε τον μήνα υπολογισμού και ανοίξτε Σχεδιασμός προγράμματος > Πρότυπα προηγούμενων μηνών.",
    "2. Επιλέξτε οριστικοποιημένους Μήνες για μάθηση. Ορίστε παράθυρο 1–12 προηγούμενων μηνών (προεπιλογή 6). Χρησιμοποιούνται μόνο εγκεκριμένοι μήνες μέσα στο παράθυρο και πριν τον μήνα υπολογισμού.",
    "3. Επιλέξτε συμμετέχοντες εργαζομένους και μικρή, μεσαία ή μεγάλη επιρροή. Πατήστε Τι αναγνωρίζει το πρόγραμμα; για δείγματα, ποσοστά και επαναλαμβανόμενες τριάδες.",
    "4. Ενεργοποιήστε τα πρότυπα, πατήστε ΟΚ και Υπολογισμός βαρδιών. Απαιτούνται τουλάχιστον 5 επιλέξιμες βάρδιες ανά εργαζόμενο και 3 παραδείγματα για αντίστοιχη μετάβαση.",
    "Πρωί: 06:00–13:59. Απόγευμα: 14:00–21:59. Νύχτα: 22:00–05:59 ή χαρακτηρισμένη νυχτερινή υπηρεσία. Αναγνωρίζονται συχνότητες και διαδοχικά ζεύγη/τριάδες, όχι ακριβή εβδομαδιαία πρότυπα ή ονόματα υπηρεσιών.",
    "Άδειες, ρεπό, κενά και αργίες εξαιρούνται και διακόπτουν τις ακολουθίες. Δεν ενώνονται διαφορετικοί μήνες εκπαίδευσης. Οι αργίες του νέου μήνα δεν βαθμολογούνται βάσει προτύπων.",
    "Η έγκριση κρατά ανεξάρτητο στιγμιότυπο. Μεταγενέστερες αλλαγές δεν το ενημερώνουν αυτόματα. Επιλέξτε Ανανέωση επιλεγμένων στιγμιοτύπων για ρητή αντικατάσταση ή αποεπιλέξτε μήνα για ανάκληση. Ακύρωση δεν αποθηκεύει τις επιλογές.",
    "Τα πρότυπα είναι προτιμήσεις. Κλειδώματα, δεξιότητες, διαθεσιμότητα, ανάπαυση και κάλυψη προηγούνται. Η δίκαιη κατανομή προηγείται στην τελική αξιολόγηση. Ελέγχετε πάντα το αποτέλεσμα.",
    "Η επεξεργασία είναι τοπική. Τα backup περιλαμβάνουν τα στιγμιότυπα. Δοκιμάστε σε αντίγραφο ομάδας. Αναλυτικά: docs/PATTERNS.md."
   });
   doc.save(path.toFile());
  }
 }
 private static void page(PDDocument doc,PDFont font,String title,String[] paragraphs)throws IOException{
  PDPage page=new PDPage(PDRectangle.A4);doc.addPage(page);
  try(PDPageContentStream out=new PDPageContentStream(doc,page)){
   Logo.pdf(out,42,770,32);line(out,font,84,790,17,"Manager’s Sidekick");line(out,font,84,773,11,title);
   float y=745;
   for(String paragraph:paragraphs){
    String current="";
    for(String word:paragraph.split(" ")){
     String next=current.isEmpty()?word:current+" "+word;
     if(font.getStringWidth(next)/1000*10>510){line(out,font,42,y,10,current);y-=14;current=word;}else current=next;
    }
    line(out,font,42,y,10,current);y-=22;
   }
   if(y<30)throw new IOException("Quick start guide exceeds page bounds");
  }
 }
 private static void line(PDPageContentStream out,PDFont font,float x,float y,float size,String text)throws IOException{
  out.beginText();out.setFont(font,size);out.newLineAtOffset(x,y);out.showText(text);out.endText();
 }
}
