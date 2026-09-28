package gr.sidekick;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import java.nio.file.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import static gr.sidekick.Model.*;

public final class PdfExporter {
    public static Path fontPath() throws IOException {
        String custom=System.getProperty("sidekick.font");
        if(custom!=null&&Files.isRegularFile(Path.of(custom)))return Path.of(custom);
        for(String p:List.of("/System/Library/Fonts/Supplemental/Arial.ttf","/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf","C:/Windows/Fonts/arial.ttf"))if(Files.isRegularFile(Path.of(p)))return Path.of(p);
        throw new IOException("Δεν βρέθηκε γραμματοσειρά για ελληνικά. Εκκινήστε με -Dsidekick.font=/διαδρομή/γραμματοσειρά.ttf");
    }
    public static void export(Data data,Team t,YearMonth ym,Path file) throws IOException {
        try(PDDocument doc=new PDDocument()) {
            PDFont font=PDType0Font.load(doc,fontPath().toFile());
            List<String> codes=new ArrayList<>();t.posts.forEach(p->codes.add(p.id));codes.addAll(data.leaves.keySet());
            int perPage=10;
            for(int first=0;first<Math.max(1,t.employees.size());first+=perPage) {
                PDPage page=new PDPage(new PDRectangle(PDRectangle.A4.getHeight(),PDRectangle.A4.getWidth()));doc.addPage(page);
                try(PDPageContentStream out=new PDPageContentStream(doc,page)) {
                    float width=page.getMediaBox().getWidth()-40;
                    text(out,font,20,565,14,"Manager’s Sidekick · "+t.name+" · "+ym.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.forLanguageTag("el"))));
                    text(out,font,20,547,8,"* Κλειδωμένο · Μωβ: Σαββατοκύριακο · Πράσινο: Αργία · Κόκκινη ημερομηνία: ελλιπής κάλυψη");
                    List<Employee> people=t.employees.subList(first,Math.min(first+perPage,t.employees.size()));
                    List<List<String>> schedule=new ArrayList<>();List<String> header=new ArrayList<>();header.add("Εργαζόμενος");for(int d=1;d<=ym.lengthOfMonth();d++)header.add(ym.atDay(d).format(DateTimeFormatter.ofPattern("d E",Locale.forLanguageTag("el"))));schedule.add(header);
                    for(Employee e:people){List<String> row=new ArrayList<>();row.add(e.name);for(int d=1;d<=ym.lengthOfMonth();d++){Cell c=t.cell(e.id,ym.atDay(d));row.add(c==null?"—":code(codes,t,c.value)+(c.locked?"*":""));}schedule.add(row);}
                    table(out,font,schedule,20,530,width,23,6,t,ym);
                    text(out,font,20,253,10,"Σύνολα ανά εργαζόμενο (ολόκληρος μήνας)");
                    // Split wide summaries across additional pages to keep columns readable.
                    List<String> part=codes.subList(0,Math.min(20,codes.size()));
                    summary(out,font,t,ym,people,codes,part,238,width);
                    text(out,font,20,24,7,"Σελίδα "+doc.getNumberOfPages()+" · Οι κωδικοί αναλύονται στο υπόμνημα. Μη εργάσιμες ημέρες περιλαμβάνουν άδειες και ρεπό.");
                }
                for(int offset=20;offset<codes.size();offset+=20){PDPage extra=new PDPage(page.getMediaBox());doc.addPage(extra);try(PDPageContentStream out=new PDPageContentStream(doc,extra)){text(out,font,20,560,12,"Συνέχεια συνόλων · "+t.name+" · "+ym);summary(out,font,t,ym,t.employees.subList(first,Math.min(first+perPage,t.employees.size())),codes,codes.subList(offset,Math.min(offset+20,codes.size())),530,page.getMediaBox().getWidth()-40);}}
            }
            for(int offset=0;offset<Math.max(1,codes.size());offset+=27){PDPage page=new PDPage(new PDRectangle(PDRectangle.A4.getHeight(),PDRectangle.A4.getWidth()));doc.addPage(page);try(PDPageContentStream out=new PDPageContentStream(doc,page)){text(out,font,20,560,14,"Υπόμνημα · "+ym);for(int i=offset;i<Math.min(offset+27,codes.size());i++){String id=codes.get(i);Post p=t.post(id);text(out,font,20,535-(i-offset)*18,10,code(codes,t,id)+" = "+label(data,t,id)+(p==null?"":" · "+p.start+"–"+p.end));}}}
            doc.save(file.toFile());
        }
    }
    private static String code(List<String> codes,Team t,String id){int i=codes.indexOf(id);return i<0?"?":(t.post(id)==null?"Α":"Π")+(i+1);}
    private static void summary(PDPageContentStream out,PDFont font,Team t,YearMonth ym,List<Employee> people,List<String> codes,List<String> part,float top,float width)throws IOException{
        List<List<String>> rows=new ArrayList<>();List<String> header=new ArrayList<>();header.add("Εργαζόμενος");for(String id:part)header.add(code(codes,t,id));rows.add(header);
        for(Employee e:people){List<String> row=new ArrayList<>();row.add(e.name);for(String id:part){int n=0;for(int d=1;d<=ym.lengthOfMonth();d++){Cell c=t.cell(e.id,ym.atDay(d));if(c!=null&&id.equals(c.value))n++;}row.add(""+n);}rows.add(row);}table(out,font,rows,20,top,width,18,7);
    }
    private static void table(PDPageContentStream out,PDFont font,List<List<String>> rows,float left,float top,float width,float height,float size)throws IOException{
        table(out,font,rows,left,top,width,height,size,null,null);
    }
    private static void table(PDPageContentStream out,PDFont font,List<List<String>> rows,float left,float top,float width,float height,float size,Team team,YearMonth month)throws IOException{
        Set<Integer> uncovered=team==null?Set.of():CalendarRules.uncoveredDays(team,month);
        int columns=rows.getFirst().size();float nameWidth=120,cellWidth=columns==1?width:(width-nameWidth)/(columns-1);
        for(int r=0;r<rows.size();r++)for(int c=0;c<columns;c++){float x=c==0?left:left+nameWidth+(c-1)*cellWidth,w=c==0?(columns==1?width:nameWidth):cellWidth,y=top-r*height;
            java.awt.Color fill=java.awt.Color.WHITE;
            if(team!=null&&c>0){
                if(CalendarRules.weekend(month.atDay(c)))fill=new java.awt.Color(0xEDEAF3);
                if(CalendarRules.holiday(team,month.atDay(c)))fill=new java.awt.Color(0xE1EEE8);
                if(r==0&&uncovered.contains(c))fill=new java.awt.Color(0xF5D9D9);
            }
            out.setNonStrokingColor(fill);out.addRect(x,y-height,w,height);out.fill();out.setNonStrokingColor(java.awt.Color.BLACK);
            out.setStrokingColor(java.awt.Color.LIGHT_GRAY);out.addRect(x,y-height,w,height);out.stroke();String s=rows.get(r).get(c);while(s.length()>1&&font.getStringWidth(s)/1000*size>w-4)s=s.substring(0,s.length()-1);text(out,font,x+2,y-height+6,size,s);}
    }
    private static void text(PDPageContentStream out,PDFont font,float x,float y,float size,String s)throws IOException{out.beginText();out.setFont(font,size);out.newLineAtOffset(x,y);out.showText(s);out.endText();}
}
