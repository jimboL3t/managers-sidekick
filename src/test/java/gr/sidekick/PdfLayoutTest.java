package gr.sidekick;

import org.junit.jupiter.api.Test;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class PdfLayoutTest {
 @Test void fullMonthHasTwoLineDaysNamedCategoriesAndTotals()throws Exception{
  Data data=new Data();Team team=TeamService.create(data,"Ομάδα υποστήριξης");YearMonth month=YearMonth.of(2026,10);
  team.posts.add(new Post("Πρωί","08:00","16:00"));team.posts.add(new Post("Απόγευμα","16:00","00:00"));team.posts.add(new Post("Νύχτα","00:00","08:00"));team.month(month).holidays.add(28);
  for(int e=0;e<10;e++){
   Employee person=new Employee(e==0?"Μαρία Παπαδοπούλου":"Εργαζόμενος "+e);team.employees.add(person);
   for(int d=1;d<=31;d++){if(e==1&&d==31)continue;team.month(month).cells.put(key(person.id,d),new Cell(d%7<2?OFF:team.posts.get(e%3).id,d==1));}
  }
  Path path=Path.of("target","pdf-preview.pdf");PdfExporter.export(data,team,month,path);
  assertEquals(31,PdfExporter.assignedTotal(team,team.employees.getFirst(),month));assertEquals(30,PdfExporter.assignedTotal(team,team.employees.get(1),month));
  try(var doc=Loader.loadPDF(path.toFile())){
   assertEquals(2,doc.getNumberOfPages());
   PDFTextStripper stripper=new PDFTextStripper();stripper.setEndPage(1);String text=stripper.getText(doc);
   assertTrue(text.contains("Υ1 Πρωί"),text);assertTrue(text.contains("Σύνολο"));assertTrue(text.contains("Πέμ"));
   List<String> totalValues=new ArrayList<>();
   PDFTextStripper positions=new PDFTextStripper(){@Override protected void writeString(String value,List<TextPosition> chars)throws java.io.IOException{
    if(!chars.isEmpty()&&chars.getFirst().getXDirAdj()>770&&chars.getFirst().getYDirAdj()>400&&chars.getFirst().getYDirAdj()<570)totalValues.add(value.strip());super.writeString(value,chars);
   }};
   positions.setEndPage(1);positions.getText(doc);assertEquals(10,totalValues.size(),totalValues.toString());assertEquals("31",totalValues.getFirst());assertEquals("30",totalValues.get(1));
   ImageIO.write(new PDFRenderer(doc).renderImageWithDPI(0,125),"png",Path.of("target","pdf-preview.png").toFile());
  }
  ImageIO.write(Logo.image(384),"png",Path.of("target","logo-preview.png").toFile());
 }
 @Test void continuationTotalsIncludeCategoriesOnOtherPages()throws Exception{
  Data data=new Data();Team t=TeamService.create(data,"Συνέχεια");YearMonth month=YearMonth.of(2024,2);Employee e=new Employee("Άτομο");t.employees.add(e);
  for(int i=0;i<14;i++){Post p=new Post("Πόστο "+i,"08:00","16:00");t.posts.add(p);t.month(month).cells.put(key(e.id,i+1),new Cell(p.id,false));}
  t.month(month).cells.put(key(e.id,29),new Cell(OFF,true));assertEquals(15,PdfExporter.assignedTotal(t,e,month));
  Path file=Path.of("target","pdf-continuation.pdf");PdfExporter.export(data,t,month,file);
  try(var doc=Loader.loadPDF(file.toFile())){assertEquals(4,doc.getNumberOfPages());for(int page=1;page<=3;page++){PDFTextStripper text=new PDFTextStripper();text.setStartPage(page);text.setEndPage(page);String content=text.getText(doc);assertTrue(content.contains("Σύνολο"));assertTrue(content.contains("15"));}}
 }
}
