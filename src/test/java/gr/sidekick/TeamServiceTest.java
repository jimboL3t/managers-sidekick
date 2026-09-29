// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import static gr.sidekick.Model.*;
import static org.junit.jupiter.api.Assertions.*;

class TeamServiceTest {
    @TempDir Path temp;

    @Test void copyIsIndependentAcrossAllMutableStateAndSurvivesReload() throws Exception {
        Data data=new Data();Team original=TeamService.create(data,"Κύρια");
        Post p=new Post("Πρωί","08:00","16:00");original.posts.add(p);
        Employee e=new Employee("Μαρία");e.skills.add(p.id);original.employees.add(e);
        YearMonth ym=YearMonth.of(2026,9);original.month(ym).holidays.add(8);
        original.month(ym).cells.put(key(e.id,1),new Cell(p.id,true));
        String before=new Gson().toJson(original);
        Team copy=TeamService.duplicate(data,original,"Δοκιμή");
        assertTrue(copy.sandbox);assertNotEquals(original.id,copy.id);
        Post cp=copy.posts.getFirst();Employee ce=copy.employees.getFirst();
        assertNotEquals(p.id,cp.id);assertNotEquals(e.id,ce.id);
        assertTrue(ce.skills.contains(cp.id));assertFalse(ce.skills.contains(p.id));
        assertEquals(cp.id,copy.cell(ce.id,ym.atDay(1)).value);assertTrue(copy.cell(ce.id,ym.atDay(1)).locked);
        cp.demand[0]=2;cp.start="07:00";ce.name="Άλλο όνομα";ce.skills.clear();
        copy.month(ym).holidays.add(15);copy.cell(ce.id,ym.atDay(1)).locked=false;
        copy.leaveTypes.put(OFF,"Δοκιμαστικό ρεπό");copy.leaveTypes.put("test","Δοκιμαστική άδεια");copy.allowedLeaves.clear();copy.maxConsecutive=3;
        new Scheduler().generate(data,copy,ym);
        assertEquals(before,new Gson().toJson(original));
        Storage storage=new Storage(temp.resolve("teams.json"));storage.save(data);Data loaded=storage.load();
        assertEquals(2,loaded.teams.size());assertEquals(before,new Gson().toJson(loaded.teams.getFirst()));
        assertEquals("Δοκιμαστικό ρεπό",loaded.teams.getLast().leaveTypes.get(OFF));
        assertEquals("Ρεπό",loaded.teams.getFirst().leaveTypes.get(OFF));
    }

    @Test void deletingEitherTeamDoesNotDeleteTheOther() {
        Data data=new Data();Team a=TeamService.create(data,"Α");Team b=TeamService.duplicate(data,a,"Β");
        assertTrue(TeamService.delete(data,a));assertEquals(java.util.List.of(b),data.teams);
        assertFalse(TeamService.delete(data,a));assertTrue(TeamService.delete(data,b));assertTrue(data.teams.isEmpty());
    }

    @Test void legacyLeaveCatalogMigratesIntoSeparateTeamMaps() throws Exception {
        Path file=temp.resolve("legacy.json");
        Files.writeString(file,"{\"version\":1,\"leaves\":{\"off\":\"Ρεπό\"},\"teams\":[{\"name\":\"Α\"},{\"name\":\"Β\"}]}");
        Data data=new Storage(file).load();
        data.teams.getFirst().leaveTypes.put("custom","Άδεια Α");
        assertFalse(data.teams.getLast().leaveTypes.containsKey("custom"));assertFalse(data.leaves.containsKey("custom"));
    }
}
