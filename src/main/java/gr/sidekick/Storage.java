// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
// See LICENSE and COPYING.md for permissions and warranty disclaimer.
package gr.sidekick;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

public final class Storage {
    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public Storage(Path file) { this.file=file; }
    public Model.Data load() throws IOException {
        if (!Files.exists(file)) return new Model.Data();
        try (Reader r=Files.newBufferedReader(file)) {
            JsonElement tree=JsonParser.parseReader(r);
            if(!tree.isJsonObject())throw new IOException(I18n.text("Μη έγκυρο αρχείο δεδομένων ή έκδοση."));
            JsonObject source=tree.getAsJsonObject();
            Model.Data data=gson.fromJson(source,Model.Data.class);
            if(data==null || data.version!=1 || data.teams==null || data.leaves==null) throw new IOException(I18n.text("Μη έγκυρο αρχείο δεδομένων ή έκδοση."));
            if(data.language==null)data.language="el";
            // Legacy files have no built-in type IDs. Recognize the original default
            // names once, then persist the mapping; new custom types stay untouched.
            if(!source.has("builtInLeaves"))data.builtInLeaves=legacyBuiltins(data.leaves);
            for(int i=0;i<data.teams.size();i++){
                Model.Team team=data.teams.get(i);
                if(team.employees==null)team.employees=new java.util.ArrayList<>();
                if(team.history==null)team.history=new java.util.ArrayList<>();
                for(Model.Employee employee:team.employees)if(employee.availabilityOverrides==null)employee.availabilityOverrides=new java.util.TreeMap<>();
                if(team.leaveTypes==null||team.leaveTypes.isEmpty())team.leaveTypes=new java.util.LinkedHashMap<>(data.leaves);
                if(!source.getAsJsonArray("teams").get(i).getAsJsonObject().has("builtInLeaves"))team.builtInLeaves=legacyBuiltins(team.leaveTypes);
            }
            return data;
        } catch(JsonParseException e) { throw new IOException(I18n.text("Δεν διαβάζονται τα δεδομένα. Διατηρήστε το αρχείο και το .bak."),e); }
    }
    private java.util.Map<String,String> legacyBuiltins(java.util.Map<String,String> leaves){
        java.util.Map<String,String> result=new java.util.LinkedHashMap<>();
        for(String name:new Model.Data().leaves.values())leaves.entrySet().stream().filter(e->name.equals(e.getValue())).findFirst().ifPresent(e->result.put(e.getKey(),e.getValue()));
        return result;
    }
    public void save(Model.Data data) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path tmp=file.resolveSibling(file.getFileName()+".tmp");
        try(Writer w=Files.newBufferedWriter(tmp)) { gson.toJson(data,w); }
        if(Files.exists(file)) Files.copy(file,file.resolveSibling(file.getFileName()+".bak"),StandardCopyOption.REPLACE_EXISTING);
        try { Files.move(tmp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
        catch(AtomicMoveNotSupportedException e) { Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING); }
    }
}
