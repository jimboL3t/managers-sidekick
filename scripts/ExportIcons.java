// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
import gr.sidekick.Logo;
import java.nio.file.*;
import javax.imageio.ImageIO;
/** Render the existing vector logo, without a separate artwork dependency. */
public class ExportIcons {
 public static void main(String[] args)throws Exception {
  Path out=Path.of(args[0]);Files.createDirectories(out);
  for(int size:new int[]{16,32,48,64,128,256,512,1024})
   ImageIO.write(Logo.image(size),"png",out.resolve(size+".png").toFile());
 }
}
