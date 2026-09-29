// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
import gr.sidekick.*;
import java.nio.file.*;
import java.time.*;
import static gr.sidekick.Model.*;
/** Runs against the distributed JAR and Java runtime, never against live user data. */
public class ReleaseSmoke {
 public static void main(String[] args)throws Exception{
  Path output=Path.of(args[0]);Files.createDirectories(output);Theme.install();I18n.setLanguage("en");
  Data d=new Data();Team t=TeamService.create(d,"Release smoke");Post p=new Post("Duty","08:00","16:00");t.posts.add(p);Employee e=new Employee("Test employee");e.skills.add(p.id);t.employees.add(e);YearMonth m=YearMonth.of(2026,9);
  new Scheduler().generate(d,t,m);History.capture(t,m,"Smoke");Storage storage=new Storage(output.resolve("smoke.json"));storage.save(d);
  if(storage.load().teams.getFirst().history.size()!=1)throw new AssertionError("Storage/history failure");
  PdfExporter.export(d,t,m,output.resolve("smoke.pdf"));
  if(Files.size(output.resolve("smoke.pdf"))<1000)throw new AssertionError("PDF failure");
  try(var in=App.class.getResourceAsStream("/META-INF/sidekick/LICENSE")){if(in==null)throw new AssertionError("License missing");}
  System.out.println("PASS: bundled Java "+System.getProperty("java.version")+"; Swing headless, scheduling, history, persistence, English and PDF.");
 }
}
