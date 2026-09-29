// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import java.awt.Color;
import java.nio.file.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import static gr.sidekick.Model.*;

public final class PdfExporter {
    private static final Color INK=new Color(0x302943), MUTED=new Color(0x686176), LINE=new Color(0xDCD8E5);
    private static final int PEOPLE=10, CATEGORIES=10;
    private static final float LEFT=24, WIDTH=PDRectangle.A4.getHeight()-48;
    public static Path fontPath() throws IOException {
        String custom=System.getProperty("sidekick.font");
        if(custom!=null&&Files.isRegularFile(Path.of(custom)))return Path.of(custom);
        for(String p:List.of("/System/Library/Fonts/Supplemental/Arial.ttf","/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf","C:/Windows/Fonts/arial.ttf"))if(Files.isRegularFile(Path.of(p)))return Path.of(p);
        throw new IOException(I18n.text("Δεν βρέθηκε γραμματοσειρά για ελληνικά. Εκκινήστε με -Dsidekick.font=/διαδρομή/γραμματοσειρά.ttf"));
    }
    public static void export(Data data,Team team,YearMonth month,Path file)throws IOException{
        try(PDDocument doc=new PDDocument()){
            PDFont font=PDType0Font.load(doc,fontPath().toFile());
            doc.getDocumentInformation().setTitle("Manager’s Sidekick · "+team.name+" · "+month);
            List<String> categories=new ArrayList<>();team.posts.forEach(p->categories.add(p.id));
            for(String id:leaves(data,team).keySet())if(!categories.contains(id))categories.add(id);
            // Keep even orphaned categories visible and included in totals.
            for(Employee e:team.employees)for(int d=1;d<=month.lengthOfMonth();d++){
                Cell c=team.cell(e.id,month.atDay(d));if(c!=null&&!categories.contains(c.value))categories.add(c.value);
            }
            for(int first=0;first<Math.max(1,team.employees.size());first+=PEOPLE){
                List<Employee> people=team.employees.subList(first,Math.min(first+PEOPLE,team.employees.size()));
                PDPage page=page(doc);
                try(PDPageContentStream out=new PDPageContentStream(doc,page)){
                    heading(out,font,team,month,I18n.text("Μηνιαίο πρόγραμμα"));
                    text(out,font,LEFT,507,7,I18n.text("* Χειροκίνητη επιλογή  ·  Μωβ: Σαββατοκύριακο  ·  Πράσινο: αργία  ·  Κόκκινο: ελλιπής κάλυψη"),MUTED);
                    List<List<String>> rows=new ArrayList<>();List<String> header=new ArrayList<>();header.add(I18n.text("Εργαζόμενος"));
                    for(int d=1;d<=month.lengthOfMonth();d++)header.add(String.format("%02d",d)+"\n"+month.atDay(d).format(DateTimeFormatter.ofPattern("EEE",I18n.locale())));rows.add(header);
                    for(Employee e:people){List<String> row=new ArrayList<>();row.add(e.name);for(int d=1;d<=month.lengthOfMonth();d++){Cell c=team.cell(e.id,month.atDay(d));row.add(c==null?"—":code(categories,team,c.value)+(c.locked?"*":""));}rows.add(row);}
                    table(out,font,rows,497,32,21,6.5f,false,team,month);
                    text(out,font,LEFT,240,11,I18n.text("Σύνολα ανά εργαζόμενο"),INK);
                    summary(out,font,data,team,month,people,categories,categories.subList(0,Math.min(CATEGORIES,categories.size())),228);
                    footer(out,font,doc.getNumberOfPages());
                }
                for(int offset=CATEGORIES;offset<categories.size();offset+=CATEGORIES){
                    PDPage extra=page(doc);
                    try(PDPageContentStream out=new PDPageContentStream(doc,extra)){
                        heading(out,font,team,month,I18n.text("Σύνολα · συνέχεια κατηγοριών"));
                        summary(out,font,data,team,month,people,categories,categories.subList(offset,Math.min(offset+CATEGORIES,categories.size())),497);footer(out,font,doc.getNumberOfPages());
                    }
                }
            }
            for(int offset=0;offset<Math.max(1,categories.size());offset+=24){
                PDPage page=page(doc);try(PDPageContentStream out=new PDPageContentStream(doc,page)){
                    heading(out,font,team,month,I18n.text("Υπόμνημα υπηρεσιών και αδειών"));
                    for(int i=offset;i<Math.min(offset+24,categories.size());i++){
                        String id=categories.get(i);Post post=team.post(id);float y=492-(i-offset)*18;
                        text(out,font,LEFT,y,9,code(categories,team,id),INK);
                        String description=label(data,team,id)+(post==null?"":"  ·  "+post.start+"–"+post.end);
                        text(out,font,LEFT+44,y,9,fit(font,description,9,WIDTH-44),INK);
                    }footer(out,font,doc.getNumberOfPages());
                }
            }
            doc.save(file.toFile());
        }
    }
    private static PDPage page(PDDocument doc){PDPage page=new PDPage(new PDRectangle(PDRectangle.A4.getHeight(),PDRectangle.A4.getWidth()));doc.addPage(page);return page;}
    private static void heading(PDPageContentStream out,PDFont font,Team team,YearMonth month,String title)throws IOException{
        Logo.pdf(out,LEFT,537,36);text(out,font,LEFT+48,559,15,"Manager’s Sidekick",INK);
        text(out,font,LEFT+48,540,9,fit(font,team.name+(team.sandbox?I18n.text(" · Δοκιμαστικό αντίγραφο"):""),9,400),MUTED);
        String date=month.format(DateTimeFormatter.ofPattern("LLLL yyyy",I18n.locale()));
        text(out,font,LEFT+WIDTH-font.getStringWidth(date)/1000*14,557,14,date,INK);
        text(out,font,LEFT,522,10,title,INK);
    }
    private static void footer(PDPageContentStream out,PDFont font,int page)throws IOException{
        text(out,font,LEFT,18,7,I18n.text("Σύνολο = όλες οι ανατεθειμένες ημέρες του μήνα (υπηρεσίες + ρεπό + άδειες). Τα κενά δεν μετρούν."),MUTED);
        text(out,font,LEFT+WIDTH-50,18,7,I18n.text("Σελίδα ")+page,MUTED);
    }
    static int assignedTotal(Team team,Employee employee,YearMonth month){int count=0;for(int d=1;d<=month.lengthOfMonth();d++)if(team.cell(employee.id,month.atDay(d))!=null)count++;return count;}
    static String code(List<String> categories,Team team,String id){
        if(OFF.equals(id))return I18n.english()?"R":"Ρ";
        if(!categories.contains(id))return "?";
        boolean duty=team.post(id)!=null;int number=0;
        for(String category:categories){
            if(!OFF.equals(category)&&(team.post(category)!=null)==duty)number++;
            if(category.equals(id))break;
        }
        return (duty?(I18n.english()?"D":"Υ"):(I18n.english()?"L":"Α"))+number;
    }

    private static void summary(PDPageContentStream out,PDFont font,Data data,Team team,YearMonth month,List<Employee> people,List<String> categories,List<String> part,float top)throws IOException{
        List<List<String>> rows=new ArrayList<>();List<String> header=new ArrayList<>();header.add(I18n.text("Εργαζόμενος"));
        for(String id:part)header.add(code(categories,team,id)+" "+label(data,team,id));header.add(I18n.text("Σύνολο"));rows.add(header);
        for(Employee e:people){List<String> row=new ArrayList<>();row.add(e.name);for(String id:part)row.add(Integer.toString(CalendarRules.count(team,e,month,id)));row.add(Integer.toString(assignedTotal(team,e,month)));rows.add(row);}
        table(out,font,rows,top,32,16,7,true,null,null);
    }
    private static void table(PDPageContentStream out,PDFont font,List<List<String>> rows,float top,float headerHeight,float rowHeight,float size,boolean summary,Team team,YearMonth month)throws IOException{
        int columns=rows.getFirst().size();float nameWidth=128,totalWidth=summary?48:0;
        float cellWidth=(WIDTH-nameWidth-totalWidth)/Math.max(1,columns-1-(summary?1:0));
        Set<Integer> uncovered=team==null?Set.of():CalendarRules.uncoveredDays(team,month);
        for(int r=0;r<rows.size();r++)for(int c=0;c<columns;c++){
            boolean total=summary&&c==columns-1;
            float x=c==0?LEFT:total?LEFT+WIDTH-totalWidth:LEFT+nameWidth+(c-1)*cellWidth;
            float width=c==0?nameWidth:total?totalWidth:cellWidth;
            float height=r==0?headerHeight:rowHeight,y=r==0?top:top-headerHeight-(r-1)*rowHeight;
            Color fill=r==0?new Color(0xEEEAF6):r%2==0?new Color(0xFAF9FC):Color.WHITE;
            if(total)fill=new Color(0xE2EEE9);
            if(team!=null&&c>0){if(CalendarRules.weekend(month.atDay(c)))fill=new Color(0xEEEAF6);if(CalendarRules.holiday(team,month.atDay(c)))fill=new Color(0xE2EEE9);if(r==0&&uncovered.contains(c))fill=new Color(0xF8DFDF);}
            out.setNonStrokingColor(fill);out.addRect(x,y-height,width,height);out.fill();out.setStrokingColor(LINE);out.setLineWidth(.35f);out.addRect(x,y-height,width,height);out.stroke();
            String value=rows.get(r).get(c);
            List<String> lines=new ArrayList<>(Arrays.asList(value.split("\n")));
            if(summary&&r==0&&c>0&&!total){int space=value.indexOf(' ');String code=value.substring(0,space),name=value.substring(space+1);String first=fit(font,code+" "+name,size,width-6);
                if(first.equals(value))lines=List.of(first);else{int split=name.lastIndexOf(' ',Math.max(0,first.length()-code.length()-1));if(split>0)lines=List.of(code+" "+name.substring(0,split),name.substring(split+1));else lines=List.of(code, name);}}
            float baseline=y-height/2+(lines.size()-1)*(size+3)/2-size*.32f;
            for(int i=0;i<Math.min(2,lines.size());i++){
                String line=fit(font,lines.get(i),size,width-6);float dx=c==0?4:(width-font.getStringWidth(line)/1000*size)/2;
                text(out,font,x+dx,baseline-i*(size+3),size,line,INK);
            }
        }
    }
    private static String fit(PDFont font,String text,float size,float width)throws IOException{
        if(font.getStringWidth(text)/1000*size<=width)return text;
        String shortened=text;while(!shortened.isEmpty()&&font.getStringWidth(shortened+"…")/1000*size>width)shortened=shortened.substring(0,shortened.length()-1);
        return shortened+"…";
    }
    private static void text(PDPageContentStream out,PDFont font,float x,float y,float size,String text,Color color)throws IOException{
        out.setNonStrokingColor(color);out.beginText();out.setFont(font,size);out.newLineAtOffset(x,y);out.showText(text);out.endText();
    }
}
