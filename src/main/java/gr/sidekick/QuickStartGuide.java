// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;

import java.nio.file.*;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;

/** Two-page bilingual guide, generated with the same embedded font as schedule PDFs. */
public final class QuickStartGuide {
 private QuickStartGuide(){}
 public static void main(String[] args)throws IOException { export(Path.of(args[0])); }
 public static void export(Path path)throws IOException {
  try(PDDocument doc=new PDDocument()){
   PDFont font=PDType0Font.load(doc,PdfExporter.fontPath().toFile());
   page(doc,font,"Γρήγορη εκκίνηση",new String[]{
    "Εκκίνηση ανά λειτουργικό",
    "Windows: αποσυμπιέστε ολόκληρο το ZIP σε εγγράψιμο φάκελο. Ανοίξτε το managersSidekick.exe, εφόσον περιλαμβάνεται, αλλιώς το start-windows.cmd. Κρατήστε όλα τα αρχεία μαζί.",
    "macOS: ανοίξτε το DMG και σύρετε την εφαρμογή στο Applications. Στο παλιό portable πακέτο αποσυμπιέστε και ανοίξτε το start-macos.command.",
    "Linux: αποσυμπιέστε ολόκληρο το πακέτο και εκτελέστε sh start-linux.sh μέσα στον φάκελό του, σε γραφικό περιβάλλον.",
    "Τα πακέτα με runtime περιλαμβάνουν Java. Το γενικό test ZIP απαιτεί εγκατεστημένη Java 21+ (στο Mac χρησιμοποιήστε sh start-linux.sh).",
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
   page(doc,font,"Quick start",new String[]{
    "Launch on your operating system",
    "Windows: extract the entire ZIP to a writable folder. Open managersSidekick.exe when included, otherwise start-windows.cmd. Keep the complete application folder together.",
    "macOS: open the DMG and drag the app to Applications. For older portable packages, extract and open start-macos.command.",
    "Linux: extract the complete archive and run sh start-linux.sh from its directory in a graphical desktop session.",
    "Runtime bundles include Java. The generic test ZIP requires Java 21+; on macOS launch it with sh start-linux.sh.",
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
