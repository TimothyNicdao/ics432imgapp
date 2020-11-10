package ics432.imgapp;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.IntToDoubleFunction;

import static javax.imageio.ImageIO.createImageOutputStream;

/**
 * A class that defines the "job" abstraction, that is, a set of input image
 * files to which an ImgTransform must be applied, thus generating a set of
 * output image files. Each output file name is the input file name prepended
 * with the ImgTransform name and an underscore.
 */
class Job {

    private final ImgTransform imgTransform;
    private final Path targetDir;
    private final List<Path> inputFiles;
    private Double imageSizeTotal = 0.0;
    private Double computeSpeed = 0.0;
    private final ArrayBlockingQueue<Image> inputBuffer;
    private final ArrayDeque<Path> inputFileBuffer;
    private final ArrayBlockingQueue<BufferedImage> outputBuffer;
    private int imagesDone = 0;
    private int imagesProcessed = 0;

    // The list of outcomes for each input file
    private final List<ImgTransformOutcome> outcome;

    // Times for reading, writing, and processing the image(s)
    private Double readTime = 0.0;
    private Double writeTime = 0.0;
    private Double processTime = 0.0;
    private long totalStartTime = 0;
    private long totalEndtime = 0;
    private Double totalTime = 0.0;
    private MainWindow mw;

    /**
     * Constructor
     *
     * @param imgTransform The imgTransform to apply to input images
     * @param targetDir    The target directory in which to generate output images
     * @param inputFiles   The list of input file paths
     */
    Job(ImgTransform imgTransform, Path targetDir, List<Path> inputFiles) {

        this.imgTransform = imgTransform;
        this.targetDir = targetDir;
        this.inputFiles = inputFiles;
        this.outcome = new ArrayList<>();
        this.inputBuffer = new ArrayBlockingQueue<>(16);
        this.inputFileBuffer = new ArrayDeque<Path>(inputFiles.size());
        this.outputBuffer = new ArrayBlockingQueue<>(16);
    }

    /**
     * Method to execute the imgTransform job
     */
    void execute(JobWindow window, MainWindow mw) {

        this.mw = mw;

            //readThread.start();
            //processThread.start();
            //writeThread.start();

    }

    void readFunction(List<Path> inputFiles) {
        this.totalStartTime = System.nanoTime();
        for (Path inputFile : inputFiles) {

            System.err.println("Applying " + this.imgTransform.getName() + " to "
                            + inputFile.toAbsolutePath().toString() + " ...");
            // Load the image from file
            Image image;
            try {
                long readStartTime = System.nanoTime();
                image = new Image(inputFile.toUri().toURL().toString());
                long readEndTime = System.nanoTime();
                this.readTime += readEndTime - readStartTime;
                  try{
                    this.inputBuffer.put(image);

                    synchronized (this) {
                      this.inputFileBuffer.addLast(inputFile);
                    }

                  } catch(InterruptedException e) {

                  }
                if (image.isError()) {
                    throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString() + " ("
                            + image.getException().toString() + ")");
                }
            } catch (IOException e) {
                try {
                    throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString());
                } catch (IOException e1) {
                }
            }
        }

      System.err.println(Thread.currentThread().getName());           //hw8
    }

    void processFunction() {                                          //hw8
        while (this.imagesProcessed != this.inputFiles.size()) {

            Image image = null;
              try{
                image = this.inputBuffer.take();
              } catch(InterruptedException e) {

              }

            // Process the image
            long processStartTime = System.nanoTime();
            BufferedImage img = imgTransform.getBufferedImageOp().filter(SwingFXUtils.fromFXImage(image, null), null);
            long processEndTime = System.nanoTime();
            this.processTime += processEndTime - processStartTime;

            try{

              this.outputBuffer.put(img);

            } catch(InterruptedException e) {

            }
            this.imagesProcessed ++;
            img = null;
        }

      System.err.println(Thread.currentThread().getName());  //hw8
    }

    void writeFunction(JobWindow window, MainWindow mw) {     //hw8
      this.mw = mw;
        while (this.imagesDone != this.inputFiles.size()) {

            // Write the image back to a file
            BufferedImage img = null;
            Path inputFile = null;
              try{
                img = this.outputBuffer.take();

                synchronized (this) {
                  inputFile = this.inputFileBuffer.removeFirst();
                }

              } catch(InterruptedException e) {

              }

            String outputPath = this.targetDir + System.getProperty("file.separator") + this.imgTransform.getName() + "_" + inputFile.getFileName();
            try {
                long writeStartTime = System.nanoTime();
                OutputStream os = new FileOutputStream(new File(outputPath));
                ImageOutputStream outputStream = createImageOutputStream(os);
                ImageIO.write(img, "jpg", outputStream);
                img = null;
                long writeEndTime = System.nanoTime();
                this.writeTime += writeEndTime - writeStartTime;
            } catch (IOException | NullPointerException e) {
                try {
                    throw new IOException("Error while writing to " + outputPath);
                } catch (IOException e1) {
                }
            }
            Path outputFile = Paths.get(outputPath);
            window.displayJob(new ImgTransformOutcome(true, inputFile, outputFile, null));
                        this.mw.increaseImagesProcessed();
                        if (this.mw.sw == null) {
                        } else {
                            this.mw.sw.windowUpdateImagesProcessed();
                        }
                        try {
                            this.imageSizeTotal += Files.size(inputFile);
                        } catch (IOException e) {
                            window.displayJob(new ImgTransformOutcome(false, inputFile, null, e));
                        }
            window.updateTasksDone();
            this.imagesDone++;
        }
        this.totalEndtime = System.nanoTime();
        this.totalTime = (double)(this.totalEndtime - this.totalStartTime);

        updateFilter();
        Platform.runLater(() -> window.updateTimes(this));
        Platform.runLater(() -> window.jobCompleted());

      System.err.println(Thread.currentThread().getName()); //hw8
    }

    /**
     * Getter for job outcomes
     *
     * @return The job outcomes, i.e., a list of ImgTransformOutcome objects
     * (in flux if the job isn't done executing)
     */
    List<ImgTransformOutcome> getOutcome() {
        return this.outcome;
    }

    /**
     * Update proper filter
     *
     * @return The read time of the job
     */
    public void updateFilter() {
        this.imageSizeTotal = this.imageSizeTotal/100000;
        this.totalTime = this.totalTime/1000000000;
        this.computeSpeed = this.imageSizeTotal/this.totalTime;
        if (this.imgTransform.getName() == "Invert") {
            this.mw.updateInvert(this.computeSpeed);
            if(this.mw.sw == null){}
            else { this.mw.sw.windowUpdateInvert();}
        } else if (this.imgTransform.getName() == "Oil4") {
            this.mw.updateOil(this.computeSpeed);
            if(this.mw.sw == null){}
            else { this.mw.sw.windowUpdateOil();}
        } else if (this.imgTransform.getName() == "Solarize") {
            this.mw.updateSolarize(this.computeSpeed);
            if(this.mw.sw == null){}
            else { this.mw.sw.windowUpdateSolarize();}
        }
    }

    /**
     * Getter for readTime
     *
     * @return The read time of the job
     */
    public Double readValue() {
        return readTime;
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public Double processValue() {
        return processTime;
    }

    /**
     * Getter for writeTime
     *
     * @return The write time of the job
     */
    public Double writeValue() {
        return writeTime;
    }

    public Double totalTime() {
        return totalTime;
    }

    /**
     * Helper method to apply a imgTransform to an input image file
     *
     * @param inputFile The input file path
     * @return the output file path
     */
    private Path processInputFile(Path inputFile) throws IOException {
        // Load the image from file
        Image image;
        long readStartTime = System.nanoTime();
        try {
            image = new Image(inputFile.toUri().toURL().toString());
            if (image.isError()) {
                throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString() +
                        " (" + image.getException().toString() + ")");
            }
        } catch (IOException e) {
            throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString());
        }
        long readEndTime = System.nanoTime();
        readTime += readEndTime - readStartTime;

        // Process the image
        long processStartTime = System.nanoTime();
        BufferedImage img = imgTransform.getBufferedImageOp().filter(SwingFXUtils.fromFXImage(image, null), null);
        long processEndTime = System.nanoTime();
        processTime += processEndTime - processStartTime;

        // Write the image back to a file
        long writeStartTime = System.nanoTime();
        String outputPath = this.targetDir + System.getProperty("file.separator") + this.imgTransform.getName() + "_" + inputFile.getFileName();
        try {
            OutputStream os = new FileOutputStream(new File(outputPath));
            ImageOutputStream outputStream = createImageOutputStream(os);
            ImageIO.write(img, "jpg", outputStream);
        } catch (IOException | NullPointerException e) {
            throw new IOException("Error while writing to " + outputPath);
        }
        long writeEndTime = System.nanoTime();
        writeTime += writeEndTime - writeStartTime;

        // Success!
        return Paths.get(outputPath);
    }

    /**
     * A helper nested class to define a imgTransform' outcome for a given input file and ImgTransform
     */
    class ImgTransformOutcome {

        // Whether the image transform is successful or not
        final boolean success;
        // The Input File path
        final Path inputFile;
        // The output file path (or null if failure)
        final Path outputFile;
        // The exception that was raised (or null if success)
        final Exception error;

        /**
         * Constructor
         *
         * @param success     Whether the imgTransform operation worked
         * @param input_file  The input file path
         * @param output_file The output file path  (null if success is false)
         * @param error       The exception raised (null if success is true)
         */
        ImgTransformOutcome(boolean success, Path input_file, Path output_file, Exception error) {
            this.success = success;
            this.inputFile = input_file;
            this.outputFile = output_file;
            this.error = error;
        }
    }
}
