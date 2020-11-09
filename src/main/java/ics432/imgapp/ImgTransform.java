package ics432.imgapp;

import java.awt.image.BufferedImageOp;

/**
 * A simple class to define an image imgTransform
 */

class ImgTransform {
    private final String name;
    private final BufferedImageOp op;

    /**
     * Constructor
     */
    ImgTransform(String name, BufferedImageOp filter) {
        this.name = name;
        this.op = filter;
    }

    /**
     * Getter for the imgTransform's name
     *
     * @return The imgTransform's name
     */
    String getName() {
        return this.name;
    }

    /**
     * Override toString method that just returns the imgTransform's name
     *
     * @return The imgTransform's name
     */
    @Override
    public String toString() { return this.name; }

    /**
     * Getter for the buffered image operation
     *
     * @return The buffered image operation
     */
    BufferedImageOp getBufferedImageOp() {
        return op;
    }
}
