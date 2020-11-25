package ics432.imgapp;

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.awt.RenderingHints;
import java.util.*;
import java.util.Collections;
import ics432.imgapp.RGB;

public class MedianFilter implements BufferedImageOp {
    
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        BufferedImage outputImage = new BufferedImage(
            src.getWidth(), src.getHeight(),
            src.getType());
        for (int i = 0; i < src.getWidth(); i++) {
			for (int j = 0; j < src.getHeight(); j++) {
                outputImage.setRGB(i, j, findMedian(src, i, j));
            }
        }
        System.out.println("exited for loops");
        return outputImage;
    }

    public int findMedian(BufferedImage src, int i, int j) {
        List<Byte> redValues = new ArrayList<Byte>();
        List<Byte> blueValues = new ArrayList<Byte>();
        List<Byte> greenValues = new ArrayList<Byte>();
        int rgb;
        byte[] bytes;
        rgb = src.getRGB(i,j);
        bytes = RGB.intToBytes(rgb);
        redValues.add(bytes[0]);
        blueValues.add(bytes[1]);
        greenValues.add(bytes[2]);

        // If not at any border
        if (i != 0 & i != src.getWidth() -1 & j != 0 & j != src.getHeight() -1) {
            // top left
            rgb = src.getRGB(i-1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower left
            rgb = src.getRGB(i-1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == 0 && j == 0) {
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == src.getWidth()-1 & j == src.getHeight()-1) {
            // top left
            rgb = src.getRGB(i-1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (j == 0 & i == src.getWidth()-1) {
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower left
            rgb = src.getRGB(i-1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == 0 & j == src.getHeight()-1) {
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (j == src.getHeight()-1) {
            // top left
            rgb = src.getRGB(i-1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (j == 0) {
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower left
            rgb = src.getRGB(i-1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == 0) {
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);        
        } else {
            // top left
            rgb = src.getRGB(i-1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower left
            rgb = src.getRGB(i-1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValues.add(bytes[1]);
            greenValues.add(bytes[2]);
        }
        Collections.sort(redValues);
        Collections.sort(blueValues);
        Collections.sort(greenValues);
        int halfway = redValues.size()/2;
        bytes[0] = redValues.get(halfway);
        bytes[1] = blueValues.get(halfway);
        bytes[2] = greenValues.get(halfway);
        return RGB.bytesToInt(bytes);
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
        return null;
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src,
    ColorModel destCM) {
        return src;
    }

    public Point2D getPoint2D(Point2D srcPt,
    Point2D dstPt) {
        return srcPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }
}
