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
            Model.Data data=gson.fromJson(r,Model.Data.class);
            if(data==null || data.version!=1 || data.teams==null || data.leaves==null) throw new IOException("Μη έγκυρο αρχείο δεδομένων ή έκδοση.");
            for(Model.Team team:data.teams) if(team.leaveTypes==null||team.leaveTypes.isEmpty()) team.leaveTypes=new java.util.LinkedHashMap<>(data.leaves);
            return data;
        } catch(JsonParseException e) { throw new IOException("Δεν διαβάζονται τα δεδομένα. Διατηρήστε το αρχείο και το .bak.",e); }
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
