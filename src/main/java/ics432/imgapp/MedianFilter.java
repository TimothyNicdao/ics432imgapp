package ics432.imgapp;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Collections;

public class MedianFilter implements BufferedImageOp {
  @Override
  public BufferedImage filter(BufferedImage src, BufferedImage dest) {

    int max_X = src.getWidth();
    int max_Y = src.getHeight();
    RGB helper = new RGB();

    for (int x = 0; x < max_X; x++) {

      for (int y = 0; y < max_Y; y++) {

        int temp;
        ArrayList<Byte> red = new ArrayList<>();
        ArrayList<Byte> green = new ArrayList<>();
        ArrayList<Byte> blue = new ArrayList<>();

        for (int i = x - 1; i < x + 2; i++) {

          for (int j = y - 1; j < y + 2; j++) {

            if((i != -1) && (j != -1) && (i != max_X ) && (j != max_Y)) {
                red.add(helper.intToBytes(src.getRGB(i, j))[0]);
                green.add(helper.intToBytes(src.getRGB(i, j))[1]);
                blue.add(helper.intToBytes(src.getRGB(i, j))[2]);
            }
          }
        }

        Collections.sort(red);
        Collections.sort(green);
        Collections.sort(blue);

        temp = helper.bytesToInt(new byte[] {red.get(red.size()/2), green.get(green.size()/2), blue.get(blue.size()/2)});

        src.setRGB(x, y, temp);

      }
    }
    dest = src;

    return dest;
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
