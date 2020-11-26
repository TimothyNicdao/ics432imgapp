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

public class DPMedianFilter implements BufferedImageOp {

    public int currentThreads = 0;
    public int dpThreads = 1;
    public BufferedImage outputImage;
    public ArrayList<FilterThread> filterThreadList = new ArrayList();;

    public DPMedianFilter(int amount) {
        this.dpThreads = amount;
    }
    
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {

        this.outputImage = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        if (this.currentThreads != this.dpThreads) {
            while (this.currentThreads != this.dpThreads) {
                this.currentThreads++;
                FilterThread newThread = new FilterThread(src, this.currentThreads, this.dpThreads, this);
                this.filterThreadList.add(newThread);
                newThread.start();
            }
            while (!filterThreadList.isEmpty()) {
                try { 
                    filterThreadList.get(0).join(); 
                    filterThreadList.remove(0);
                } 
                catch (Exception e) { 
                } 
            }
        } else {
            for (int i = 0; i < src.getWidth(); i++) {
			    for (int j = 0; j < src.getHeight(); j++) {
                    this.outputImage.setRGB(i, j, findMedian(src, i, j));
                }
            }   
        }
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

    /**
     * Nested ReaderThread class
     */
    private class FilterThread extends Thread {

        public BufferedImage src;
        public DPMedianFilter dpmf;
        public int threadNumber;
        public int totalThreads;
        public FilterThread(BufferedImage src, int threadNumber, int totalThreads, DPMedianFilter dpmf) {
            this.src = src;
            this.threadNumber = threadNumber;
            this.totalThreads = totalThreads;
            this.dpmf = dpmf;
        }

        @Override
        public void run() {

            int threadBlock = (src.getHeight()-1)/totalThreads;
            int start;
            int end;
            if (threadNumber == totalThreads) {
                start = threadBlock*(threadNumber-1);
                end = this.src.getHeight()-1;
            } else {
                start = threadBlock*(threadNumber-1);
                end = threadBlock*(threadNumber)-1;
            }

            for (int i = 0; i < this.src.getWidth(); i++) {
                for (int j = start; j <= end; j++) {
                        this.dpmf.outputImage.setRGB(i, j, findMedian(src, i, j));
                }
            }
            
        }
    }
}

