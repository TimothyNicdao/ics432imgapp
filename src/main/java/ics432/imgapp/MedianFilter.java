package ics432.imgapp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Collections;

public class MedianFilter implements BufferedImageOp {
  @Override
  public BufferedImage filter(BufferedImage src, BufferedImage dst) {
    int max_X = src.getWidth();
    int max_Y = src.getHeight();

    for (int x = 0; x < max_X; x++) {

      for (int y = 0; y < max_Y; y++) {
        ArrayList<Byte> redValues = new ArrayList<>();
        ArrayList<Byte> greenValues = new ArrayList<>();
        ArrayList<Byte> blueValues = new ArrayList<>();

        for (int i = x - 1; i < x + 2; i++) {

          for (int j = y - 1; j < y + 2; j++) {
            if((i != -1) && (j != -1) && (i != max_X ) && (j != max_Y)) {
                redValues.add(RGB.intToBytes(src.getRGB(i, j))[0]);
                greenValues.add(RGB.intToBytes(src.getRGB(i, j))[1]);
                blueValues.add(RGB.intToBytes(src.getRGB(i, j))[2]);
            }
          }
        }
        Collections.sort(redValues);
        Collections.sort(greenValues);
        Collections.sort(blueValues);

        byte[] bytes = {redValues.get(redValues.size()/2),greenValues.get(greenValues.size()/2), blueValues.get(blueValues.size()/2)};
        src.setRGB(x, y, RGB.bytesToInt(bytes));
      }
    }

    dst = src;

    return dst;
  }

  @Override
  public Rectangle2D getBounds2D(BufferedImage src) {
    return null;
  }

  @Override
  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
    return null;
  }

  @Override
  public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
    return null;
  }

  @Override
  public RenderingHints getRenderingHints() {
    return null;
  }
}
