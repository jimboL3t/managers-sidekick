package gr.sidekick;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/** Original vector monogram: the same paths in Swing, SVG and exported PDF. */
public final class Logo {
    private Logo() {}
    public static BufferedImage image(int size){
        BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.scale(size/96d,size/96d);
        g.setColor(new Color(0x282140));g.fill(new RoundRectangle2D.Double(0,0,96,96,46,46));
        g.setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        Path2D m=new Path2D.Double();m.moveTo(16,66);m.lineTo(16,31);m.lineTo(31,49);m.lineTo(46,31);m.lineTo(46,66);g.setColor(new Color(0xBEA6F6));g.draw(m);
        Path2D s=new Path2D.Double();s.moveTo(79,34);s.curveTo(61,24,52,41,67,48);s.curveTo(87,55,78,74,58,64);g.setColor(new Color(0x91D5C2));g.draw(s);g.dispose();return image;
    }
    public static Icon icon(int size){return new ImageIcon(image(size));}
    public static void pdf(PDPageContentStream out,float x,float y,float size)throws IOException{
        out.saveGraphicsState();out.transform(new org.apache.pdfbox.util.Matrix(size/96,0,0,-size/96,x,y+size));
        out.setNonStrokingColor(new Color(0x282140));
        out.moveTo(23,0);out.lineTo(73,0);out.curveTo(86,0,96,10,96,23);out.lineTo(96,73);out.curveTo(96,86,86,96,73,96);out.lineTo(23,96);out.curveTo(10,96,0,86,0,73);out.lineTo(0,23);out.curveTo(0,10,10,0,23,0);out.closePath();out.fill();
        out.setLineWidth(6);out.setLineCapStyle(1);out.setLineJoinStyle(1);out.setStrokingColor(new Color(0xBEA6F6));out.moveTo(16,66);out.lineTo(16,31);out.lineTo(31,49);out.lineTo(46,31);out.lineTo(46,66);out.stroke();
        out.setStrokingColor(new Color(0x91D5C2));out.moveTo(79,34);out.curveTo(61,24,52,41,67,48);out.curveTo(87,55,78,74,58,64);out.stroke();out.restoreGraphicsState();
    }
}
