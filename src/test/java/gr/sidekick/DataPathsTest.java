package gr.sidekick;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
class DataPathsTest {
 @Test void nativeAndPortableLocationsDoNotDependOnInstallationFolder(){
  String installed=System.getProperty("sidekick.installed"),override=System.getProperty("sidekick.dataDir");
  try{System.clearProperty("sidekick.dataDir");System.clearProperty("sidekick.installed");assertEquals(Path.of("data","sidekick.json"),DataPaths.file());System.setProperty("sidekick.installed","true");assertEquals(Path.of(System.getProperty("user.home"),"ManagersSidekick","data","sidekick.json"),DataPaths.file());System.setProperty("sidekick.dataDir","custom-data");assertEquals(Path.of("custom-data","sidekick.json"),DataPaths.file());}
  finally{if(installed==null)System.clearProperty("sidekick.installed");else System.setProperty("sidekick.installed",installed);if(override==null)System.clearProperty("sidekick.dataDir");else System.setProperty("sidekick.dataDir",override);}
 }
}
