package ics432.imgapp;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static javax.imageio.ImageIO.createImageOutputStream;

/**
 * A class that defines the "job" abstraction, that is, a  set of input image files
 * to which an ImgTransform must be applied, thus generating a set of output
 * image files. Each output file name is the input file name prepended with
 * the ImgTransform name and an underscore.
 */
class Job {

    private final ImgTransform imgTransform;
    private final Path targetDir;
    private final List<Path> inputFiles;

    // The list of outcomes for each input file
    private final List<ImgTransformOutcome> outcome;

    /**
     * Constructor
     *
     * @param imgTransform The imgTransform to apply to input images
     * @param targetDir  The target directory in which to generate output images
     * @param inputFiles The list of input file paths
     */
    Job(ImgTransform imgTransform,
        Path targetDir,
        List<Path> inputFiles) {

        this.imgTransform = imgTransform;
        this.targetDir = targetDir;
        this.inputFiles = inputFiles;

        this.outcome = new ArrayList<>();
    }

    /**
     * Method to execute the imgTransform job
     */
    void execute() {

        // Go through each input file and process it
        for (Path inputFile : inputFiles) {

            System.err.println("Applying " + this.imgTransform.getName() + " to " + inputFile.toAbsolutePath().toString() + " ...");

            Path outputFile;
            try {
                outputFile = processInputFile(inputFile);
                // Generate a "success" outcome
                this.outcome.add(new ImgTransformOutcome(true, inputFile, outputFile, null));
            } catch (IOException e) {
                // Generate a "failure" outcome
                this.outcome.add(new ImgTransformOutcome(false, inputFile, null, e));
            }
        }
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
     * Helper method to apply a imgTransform to an input image file
     *
     * @param inputFile The input file path
     * @return the output file path
     */
    private Path processInputFile(Path inputFile) throws IOException {

        // Load the image from file
        Image image;

        long startTime = System.currentTimeMillis();
        try {
            image = new Image(inputFile.toUri().toURL().toString());
            if (image.isError()) {
                throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString() +
                        " (" + image.getException().toString() + ")");
            }
        } catch (IOException e) {
            throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString());
        }
        long endTime = System.currentTimeMillis();
        System.err.println("Reading time: " + (endTime - startTime));

        // Process the image
        long startTime2 = System.currentTimeMillis();
        BufferedImage img = imgTransform.getBufferedImageOp().filter(SwingFXUtils.fromFXImage(image, null), null);
        long endTime2 = System.currentTimeMillis();
        System.err.println("Processing time: " + (endTime2 - startTime2));

        // Write the image back to a file
        long startTime3 = System.currentTimeMillis();
        String outputPath = this.targetDir + System.getProperty("file.separator") + this.imgTransform.getName() + "_" + inputFile.getFileName();
        try {
            OutputStream os = new FileOutputStream(new File(outputPath));
            ImageOutputStream outputStream = createImageOutputStream(os);
            ImageIO.write(img, "jpg", outputStream);
        } catch (IOException | NullPointerException e) {
            throw new IOException("Error while writing to " + outputPath);
        }
        long endTime3 = System.currentTimeMillis();
        System.err.println("Writing time: " + (endTime3 - startTime3));

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
