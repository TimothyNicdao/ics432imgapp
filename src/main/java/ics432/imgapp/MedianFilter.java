package main.java.ics432.imgapp;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Collections;

public class MedianFilter extends BufferedImageOp {
    
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        for (int i = 0; i < src.getWidth(); i++) {
			for (int j = 0; j < src.getHeight(); j++) {
                    dst.setRGB(i, j, findMedian(src, i, j));
			}
        }
        return dst;
    }

    public int findMedian(BufferedImage src, int i, int j) {
        List<Byte> redValues = new ArrayList<Byte>();
        List<Byte> blueValues = new ArrayList<Byte>();
        List<Byte> greenValues = new ArrayList<Byte>();
        int rgb;
        byte[] bytes;

        // If not at any border
        if (i != 0 & i != src.getWidth() -1 & j != 0 & j != src.getHeight() -1) {
            // top left
            rgb = src.getRGB(i-1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower left
            rgb = src.getRGB(i-1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == 0 && j == 0) {
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == src.getWidth()-1 & j == src.getHeight()-1) {
            // top left
            rgb = src.getRGB(i-1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == 0) {
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (i == src.getWidth()-1) {
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (j == src.getHeight()-1) {
            // top left
            rgb = src.getRGB(i-1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top middle
            rgb = src.getRGB(i,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //top right
            rgb = src.getRGB(i+1,j-1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
        } else if (j == 0) {
            //middle left
            rgb = src.getRGB(i-1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //middle right
            rgb = src.getRGB(i+1,j);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower left
            rgb = src.getRGB(i-1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower middle
            rgb = src.getRGB(i,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
            //lower right
            rgb = src.getRGB(i+1,j+1);
            bytes = RGB.intToBytes(rgb);
            redValues.add(bytes[0]);
            blueValeus.add(bytes[1]);
            greenValues.add(bytes[2]);
        }
        Collections.sort(redValues);
        Collections.sort(blueValeus);
        Collections.sort(greenValues);
        int halfway = (int) redValues.size()/2;
        bytes[0] = redValues.get(halfway);
        bytes[1] = blueValues.get(halfway);
        bytes[3] = greenValues.get(halfway);
        return RGB.bytesToInt(bytes);

    }

}
