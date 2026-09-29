// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class SidekickTest {
    @TempDir Path temp;
    final YearMonth ym=YearMonth.of(2026,9);
    Team team(int people){Team t=new Team("Ομάδα");Post p=new Post("Πρωί","08:00","16:00");t.posts.add(p);for(int i=0;i<people;i++){Employee e=new Employee("Άτομο "+i);e.skills.add(p.id);t.employees.add(e);}return t;}
    @Test void coversPostsAndPreservesLocksWithoutInventingExtraRest(){Data data=new Data();Team t=team(3);Employee e=t.employees.getFirst();t.month(ym).cells.put(key(e.id,4),new Cell(OFF,true));List<String> issues=new Scheduler().generate(data,t,ym);assertTrue(CalendarRules.uncoveredDays(t,ym).isEmpty(),issues.toString());assertFalse(issues.stream().anyMatch(s->s.contains("δεξιότητα")),issues.toString());for(Employee person:t.employees)assertEquals(8,CalendarRules.count(t,person,ym,OFF));assertEquals(OFF,t.cell(e.id,ym.atDay(4)).value);assertTrue(t.cell(e.id,ym.atDay(4)).locked);assertEquals(90,t.month(ym).cells.size());for(Employee person:t.employees)assertEquals(22,Scheduler.workCount(t,person,ym));}
    @Test void insufficientSkillsReportsCoverageWithoutIllegalAssignments(){Data data=new Data();Team t=team(1);t.employees.getFirst().skills.clear();List<String> issues=new Scheduler().generate(data,t,ym);assertTrue(issues.stream().anyMatch(s->s.contains("κάλυψη 0 / 1")));assertTrue(t.month(ym).cells.values().stream().allMatch(c->c.value.equals(OFF)));}
    @Test void overnightRestAcrossMonths(){Data data=new Data();Team t=team(1);Employee e=t.employees.getFirst();Post night=new Post("Νύχτα","22:00","06:00");t.posts.add(night);e.skills.add(night.id);t.month(ym.minusMonths(1)).cells.put(key(e.id,31),new Cell(night.id,true));t.month(ym).cells.put(key(e.id,1),new Cell(t.posts.getFirst().id,true));assertTrue(new Scheduler().validate(data,t,ym).stream().anyMatch(s->s.contains("2026-09-01 · Άτομο 0: δεξιότητα")));new Scheduler().generate(data,t,ym);assertEquals(t.posts.getFirst().id,t.cell(e.id,ym.atDay(1)).value);}
    @Test void sixConsecutiveDaysCrossingMonthAreRejected(){Team t=team(1);Employee e=t.employees.getFirst();for(int d=27;d<=31;d++)t.month(ym.minusMonths(1)).cells.put(key(e.id,d),new Cell(t.posts.getFirst().id,true));t.month(ym).cells.put(key(e.id,1),new Cell(t.posts.getFirst().id,true));assertTrue(new Scheduler().validate(new Data(),t,ym).stream().anyMatch(s->s.contains("2026-09-01 · Άτομο 0: δεξιότητα")));}
    @Test void exactlyElevenHoursAllowed(){Team t=team(1);Employee e=t.employees.getFirst();Post late=new Post("Απόγευμα","13:00","21:00");t.posts.add(late);e.skills.add(late.id);t.month(ym).cells.put(key(e.id,1),new Cell(late.id,true));t.month(ym).cells.put(key(e.id,2),new Cell(t.posts.getFirst().id,true));assertFalse(new Scheduler().validate(new Data(),t,ym).stream().anyMatch(s->s.contains("δεξιότητα")));}
    @Test void overnightRestWithInterveningDay(){Team t=team(1);Employee e=t.employees.getFirst();Post longShift=new Post("Μεγάλη","23:00","22:00"),early=new Post("Νωρίς","00:00","08:00");t.posts.addAll(List.of(longShift,early));e.skills.addAll(List.of(longShift.id,early.id));t.month(ym).cells.put(key(e.id,1),new Cell(longShift.id,true));t.month(ym).cells.put(key(e.id,3),new Cell(early.id,true));assertTrue(new Scheduler().validate(new Data(),t,ym).stream().anyMatch(s->s.contains("2026-09-03 · Άτομο 0: δεξιότητα")));}
    @Test void futureMonthLocksConstrainGeneratedEnd(){Team t=team(2);Employee e=t.employees.getFirst();Post night=new Post("Νύχτα","22:00","06:00");t.posts.add(night);Arrays.fill(t.posts.getFirst().demand,0);e.skills.add(night.id);t.employees.get(1).skills.add(night.id);t.month(ym.plusMonths(1)).cells.put(key(e.id,1),new Cell(t.posts.getFirst().id,true));new Scheduler().generate(new Data(),t,ym);Cell last=t.cell(e.id,ym.atEndOfMonth());assertTrue(last==null||!night.id.equals(last.value));}
    @Test void storageRoundTripAndBackup()throws Exception{Storage s=new Storage(temp.resolve("data.json"));Data d=new Data();d.teams.add(team(2));s.save(d);s.save(d);assertEquals("Ομάδα",s.load().teams.getFirst().name);assertTrue(Files.exists(temp.resolve("data.json.bak")));Files.writeString(temp.resolve("data.json"),"{");assertThrows(java.io.IOException.class,s::load);}
    @Test void pdfGreekLandscapeAndTotals()throws Exception{Data d=new Data();Team t=team(12);new Scheduler().generate(d,t,ym);Path file=temp.resolve("month.pdf");PdfExporter.export(d,t,ym,file);try(var doc=Loader.loadPDF(file.toFile())){assertEquals(3,doc.getNumberOfPages());assertTrue(doc.getPage(0).getMediaBox().getWidth()>doc.getPage(0).getMediaBox().getHeight());String text=new PDFTextStripper().getText(doc);assertTrue(text.contains("Σύνολα"));assertTrue(text.contains("Πρωί"));assertTrue(text.contains("Άτομο 11"));}}
}
