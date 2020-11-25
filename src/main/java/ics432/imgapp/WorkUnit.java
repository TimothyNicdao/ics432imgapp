package ics432.imgapp;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.imageio.ImageIO.createImageOutputStream;

/**
 * Class that implement a work unit abstraction
 */
public class WorkUnit {

    final public ImgTransform imgTransform;
    final public Path inputFile;
    final public Path targetDir;
    public Path outputFile;
    public Image inputImage;
    public BufferedImage processedImage;
    public Job givenJob;
    public boolean end;


    public WorkUnit(ImgTransform imgTransform, Path inputFile, Path targetDir, Job givenJob, boolean end) {
        this.imgTransform = imgTransform;
        this.inputFile = inputFile;
        this.targetDir = targetDir;
        this.outputFile = null;
        this.inputImage = null;
        this.processedImage = null;
        this.givenJob = givenJob;
        this.end = end;
    }

    public void readInputFile() throws IOException {
        // Load the image from file
        try {
            this.inputImage = new Image(inputFile.toUri().toURL().toString());
            if (this.inputImage.isError()) {
                throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString() +
                        " (" + this.inputImage.getException().toString() + ")");
            }
        } catch (IOException e) {
            this.inputImage = null;
            throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString());
        }
    }

    public void processImage() {
        if (this.inputImage != null) {
            int index = ICS432ImgApp.filters.indexOf(this.imgTransform);
            ImgTransform newImgTransform;
            if (index == 0 ) {
                newImgTransform = new ImgTransform("Invert", new InvertFilter());
            } else if (index == 1) {
                newImgTransform = new ImgTransform("Solarize", new SolarizeFilter());
            } else if (index == 2) {
                OilFilter of = new OilFilter();
                of.setRange(4);
                newImgTransform = new ImgTransform("Oil4", of);
            } else {
                newImgTransform = new ImgTransform("Median", new MedianFilter());
            }
                
            this.processedImage = newImgTransform.getBufferedImageOp().filter(SwingFXUtils.fromFXImage(this.inputImage, null), null);
            this.inputImage = null; // freeing  memory

            
        }
    }

    public  void writeImage() throws IOException {
        if (this.processedImage != null) {
            String outputPath = this.targetDir + "/" + this.imgTransform.getName() + "_" + this.inputFile.getFileName();
            try {
                this.outputFile = Paths.get(outputPath);
                OutputStream os = new FileOutputStream(new File(outputPath));
                ImageOutputStream outputStream = createImageOutputStream(os);
                ImageIO.write(this.processedImage, "jpg", outputStream);
            } catch (IOException | NullPointerException e) {
                throw new IOException("Error while writing to " + outputPath);
            }
            this.processedImage = null; // freeing memory
        }
    }

}
