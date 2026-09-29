package gr.sidekick;
import java.nio.file.*;
/** Portable launches retain their old location; installed launches use user-owned storage. */
public final class DataPaths {
 private DataPaths(){}
 public static Path file(){
  String override=System.getProperty("sidekick.dataDir");
  if(override!=null&&!override.isBlank())return Path.of(override).resolve("sidekick.json");
  if(Boolean.getBoolean("sidekick.installed"))return Path.of(System.getProperty("user.home"),"ManagersSidekick","data","sidekick.json");
  return Path.of("data","sidekick.json");
 }
}
