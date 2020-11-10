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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;
import java.util.List;

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
    private ArrayBlockingQueue<Image> inputBuffer;
    private ArrayBlockingQueue<Path> inputFileBuffer;
    private ArrayBlockingQueue<BufferedImage> outputBuffer;
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
    Job(MainWindow mw) {

        this.imgTransform = imgTransform;
        this.targetDir = targetDir;
        this.outcome = new ArrayList<>();
        this.inputBuffer = new ArrayBlockingQueue<Image>(16);
        this.inputFileBuffer = new ArrayBlockingQueue<Path>(16);
        this.outputBuffer = new ArrayBlockingQueue<BufferedImage>(16);
        this.mw = mw;
    }

    /**
     * Method to execute the imgTransform job
     */
    void execute(JobWindow window, MainWindow mw) {

        if (mw.mtcbSelected == true) {
            Runnable readRun = () -> {
                readFunction(this.inputFiles);
            };
            Thread readThread = new Thread(readRun);

            Runnable processRun = () -> {
                processFunction();
            };
            Thread processThread = new Thread(processRun);

            Runnable writeRun = () -> {
                writeFunction(window);
            };
            Thread writeThread = new Thread(writeRun);

            readThread.start();
            processThread.start();
            writeThread.start();

        } 
    }

    private void readFunction(List<Path> inputFiles) {
        this.totalStartTime = System.nanoTime();
        for (Path inputFile : inputFiles) {

            synchronized(this)
            {
                while (this.inputBuffer.size() == this.mw.sliderValue) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }   

            System.err.println("Applying " + this.imgTransform.getName() + " to "
                            + inputFile.toAbsolutePath().toString() + " ...");
            // Load the image from file
            Image image;
            try {
                long readStartTime = System.nanoTime();
                image = new Image(inputFile.toUri().toURL().toString());
                long readEndTime = System.nanoTime();
                this.readTime += readEndTime - readStartTime;
                synchronized(this) {
                    this.inputBuffer.addLast(image);
                    this.inputFileBuffer.addLast(inputFile);
                    notifyAll();
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
            image = null;
        }
    }

    private void processFunction() {
        while (this.imagesProcessed != this.inputFiles.size()) {
            synchronized(this) {
                while (this.inputBuffer.peek() == null) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
                }
            }

            synchronized(this)
            {
                while (this.outputBuffer.size() == this.mw.sliderValue) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }  
            
            Image image;
            synchronized(this) {
                image = this.inputBuffer.removeFirst();
            }
            // Process the image
            long processStartTime = System.nanoTime();
            BufferedImage img = imgTransform.getBufferedImageOp().filter(SwingFXUtils.fromFXImage(image, null), null);
            image = null;
            long processEndTime = System.nanoTime();
            this.processTime += processEndTime - processStartTime;
            synchronized(this) {
                this.outputBuffer.addLast(img);
                notifyAll();
            }
            img = null;
        }
    }

    private synchronized void writeFunction(JobWindow window) {
        while (this.imagesDone != this.inputFiles.size()) {
            synchronized(this) {
                while (this.outputBuffer.peek() == null) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }
            }
            // Write the image back to a file
            BufferedImage img;
            Path inputFile;
            synchronized(this) {
                img = this.outputBuffer.removeFirst();
                inputFile = this.inputFileBuffer.removeFirst();
                notifyAll();
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
