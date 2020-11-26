package ics432.imgapp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class Job {

    Path targetDir;
    List<Path> inputFiles;
    ImgTransform imgTransform;
    protected JobWindow jobWindow;
    protected MainWindow mw;

    protected List<ImgTransformOutcome> outcomes;
    protected Profiling profiling;

    protected volatile boolean isCanceled;

    /**
     * Constructor
     * @param targetDir: the target directory
     * @param inputFiles: the input files
     * @param imgTransform: the image transform to apply
     * @param jobWindow: the JobWindow that started this job, in case it needs to be updated (null otherwise)
     */
    Job(Path targetDir,
        List<Path>  inputFiles,
        ImgTransform imgTransform,
        int bufferSize,
        JobWindow jobWindow, MainWindow mw) {

        this.targetDir = targetDir;
        this.inputFiles  = inputFiles;
        this.imgTransform  = imgTransform;
        this.jobWindow = jobWindow;
        this.mw = mw;

        this.outcomes = new ArrayList<>();
        this.profiling = new Profiling();
        this.isCanceled = false;

        // Populate the toRead buffer
        try {
            for (Path inputFile : this.inputFiles) {
                WorkUnit wu = new WorkUnit(this.imgTransform, inputFile, this.targetDir, this, false, this.mw);
                this.mw.toRead.put(wu);
            }
            WorkUnit jobEnd = new WorkUnit(this.imgTransform, this.inputFiles.get(0), this.targetDir, this, true,this.mw);
            this.mw.toRead.put(jobEnd);
            // Put a the "the end" work unit to signal the end of computing
        } catch (InterruptedException ignore) {
        }

    }

    /**
     * Getter for outcomes
     */
    List<ImgTransformOutcome> getOutcomes()  {
        return this.outcomes;
    }

    /**
     * Method to execute the job, synchronously (i.e., the method is blocking)
     */
    public abstract void execute();

    /**
     * A helper nested class to define a imgTransform' outcome for a given input file and ImgTransform
     */
    class ImgTransformOutcome {

        final boolean success;
        final Path inputFile;
        final Path outputFile;
        final Exception error;

        /**
         * Constructor
         *
         * @param success     Whether the imgTransform operation worked
         * @param input_file  The input file
         * @param output_file The output file (null if success is false)
         * @param error       The exception raised (null if success is true)
         */
        ImgTransformOutcome(boolean success, Path input_file, Path output_file, Exception error) {
            this.success = success;
            this.inputFile = input_file;
            this.outputFile = output_file;
            this.error = error;
        }
    }

    /**
     * A helper nested class to keep track of profiling information
     */
    class Profiling {
        public float readTime = 0.0F;
        public float writeTime = 0.0F;
        public float processingTime = 0.0F;
        public float totalExecutionTime = 0.0F;
        public float totalMBProcessed = 0.0F;
    }
}
