package gr.sidekick;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import com.google.gson.Gson;
import java.nio.file.*;
import java.time.YearMonth;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class LocalizationTest {
 @TempDir Path dir;
 @AfterEach void reset(){I18n.setLanguage("el");}
 @Test void preferencePersistsAndLegacyDefaultsToGreek()throws Exception{
  Storage storage=new Storage(dir.resolve("data.json"));assertEquals("el",storage.load().language);
  Data data=new Data();data.language="en";Team t=TeamService.create(data,"Ομάδα");storage.save(data);
  Data loaded=storage.load();assertEquals("en",loaded.language);assertEquals(t.builtInLeaves,loaded.teams.getFirst().builtInLeaves);
  Files.writeString(dir.resolve("data.json"),"{\"version\":1,\"leaves\":{\"off\":\"Ρεπό\"},\"teams\":[]}");
  assertEquals("el",storage.load().language);
 }
 @Test void translationsDoNotRenameUserContent(){
  Data data=new Data();Team t=TeamService.create(data,"Αποθήκευση");Post post=new Post("Ρεπό","08:00","16:00");t.posts.add(post);
  t.leaveTypes.put("custom","Κανονική άδεια");String before=new Gson().toJson(data);
  I18n.setLanguage("en");assertEquals("Save",I18n.text("Αποθήκευση"));assertEquals("Ρεπό",label(data,t,post.id));
  assertEquals("Κανονική άδεια",label(data,t,"custom"));assertNotEquals("Ρεπό",label(data,t,OFF));
  assertEquals(before,new Gson().toJson(data));I18n.setLanguage("el");assertEquals("Ρεπό",label(data,t,OFF));
 }
 @Test void codesHaveIndependentCounters(){
  Data data=new Data();Team t=TeamService.create(data,"Team");Post p=new Post("Duty","08:00","16:00");t.posts.add(p);
  List<String> ids=List.of(OFF,"leave1",p.id,"leave2");
  assertEquals("Ρ",PdfExporter.code(ids,t,OFF));assertEquals("Υ1",PdfExporter.code(ids,t,p.id));assertEquals("Α2",PdfExporter.code(ids,t,"leave2"));
  I18n.setLanguage("en");assertEquals("R",PdfExporter.code(ids,t,OFF));assertEquals("D1",PdfExporter.code(ids,t,p.id));assertEquals("L1",PdfExporter.code(ids,t,"leave1"));
 }
 @Test void englishPdfKeepsGreekNamesAndAssignments()throws Exception{
  Data data=new Data();Team t=TeamService.create(data,"Ομάδα");Employee e=new Employee("Μαρία");t.employees.add(e);Post p=new Post("Πρωί","08:00","16:00");t.posts.add(p);
  YearMonth month=YearMonth.of(2026,9);t.month(month).cells.put(key(e.id,1),new Cell(p.id,true));t.month(month).cells.put(key(e.id,2),new Cell(OFF,true));
  String before=new Gson().toJson(data);I18n.setLanguage("en");Path file=Path.of("target/pdf-preview-en.pdf");PdfExporter.export(data,t,month,file);
  try(var doc=Loader.loadPDF(file.toFile())){String text=new PDFTextStripper().getText(doc);assertTrue(text.contains("Employee"),text);assertTrue(text.contains("September"),text);assertTrue(text.contains("Total"),text);assertTrue(text.contains("D1 Πρωί"),text);assertTrue(text.contains("Μαρία"),text);assertFalse(text.contains("Σύνολο"));}
  assertEquals(before,new Gson().toJson(data));
 }
}
