package ics432.imgapp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MultiThreadedJob extends Job {

    private int bufferSize;
    private ReaderThread readerThread;
    private ProcessorThread processorThread;
    private WriterThread writerThread;


    /**
     * Constructor
     * @param jobWindow: the JobWindow that started this job
     */
    public MultiThreadedJob(Path targetDir,
                            List<Path> inputFiles,
                            ImgTransform imgTransform,
                            int bufferSize,
                            JobWindow jobWindow) {
        super(targetDir, inputFiles, imgTransform, bufferSize, jobWindow);
        this.bufferSize =  bufferSize;
        this.readerThread = null;
        this.processorThread = null;
        this.writerThread = null;
    }


    /**
     * Method to execute the job, synchronously (i.e., the method is blocking)
     */
    public void execute() {

        long start = System.currentTimeMillis();

        if (this.jobWindow != null) {
            this.jobWindow.updateProgress(0.0);
        }

        // Create and start all threads
        this.readerThread = new ReaderThread(this);
        this.processorThread = new ProcessorThread(this);
        this.writerThread = new WriterThread(this);

        this.readerThread.start();
        this.processorThread.start();
        this.writerThread.start();

        try {
            this.writerThread.join();
        } catch (InterruptedException e) {
            if (this.isCanceled) {
                this.readerThread.interrupt();
                this.processorThread.interrupt();
                this.writerThread.interrupt();
                return;
            }
        }
        this.profiling.totalExecutionTime = (System.currentTimeMillis() - start) / 1000F;

        if (this.jobWindow != null) {
            this.jobWindow.updateDisplayAfterJobCompletion(
                    this.isCanceled,
                    this.outcomes,
                    this.profiling);
        }
    }

    /**
     * Nested ReaderThread class
     */
    private class ReaderThread extends Thread {

        private MultiThreadedJob job;

        public ReaderThread(MultiThreadedJob job) {
            this.job = job;
        }

        @Override
        public void run() {

            while (true) {

                if (this.job.isCanceled) {
                    break;
                }

                WorkUnit wu;
                try {
                    wu = this.job.toRead.take();
                } catch (InterruptedException e) {
                    //  We're canceled
                    break;
                }

                if (wu != ProdConsBuffer.theEnd) {

                    try {
                        long t1 = System.currentTimeMillis();
                        wu.readInputFile();
                        long t2 = System.currentTimeMillis();
                        this.job.profiling.readTime += (t2 - t1) / 1000F;
                    } catch (IOException e) {
                        this.job.outcomes.add(new ImgTransformOutcome(false, wu.inputFile, null, e));
                        continue;
                    }
                }

                try {
                    this.job.toProcess.put(wu);
                } catch (InterruptedException e) {
                    // We're canceled
                    break;
                }

                if (wu == ProdConsBuffer.theEnd) {
                    break;
                }
            }
        }
    }



    /**
     * Nested ProcessorThread class
     */
    private class ProcessorThread extends Thread {

        private MultiThreadedJob job;

        public ProcessorThread(MultiThreadedJob job) {
            this.job = job;
        }

        @Override
        public void run() {

            while (true) {

                if (this.job.isCanceled) {
                    break;
                }

                WorkUnit wu;
                try {
                    wu = this.job.toProcess.take();
                } catch (InterruptedException e) {
                    //  We're canceled
                    break;
                }

                if (wu != ProdConsBuffer.theEnd) {

                    long t1 = System.currentTimeMillis();
                    wu.processImage();
                    long t2 = System.currentTimeMillis();
                    this.job.profiling.processingTime += (t2 - t1) / 1000F;
                }

                try {
                    this.job.toWrite.put(wu);
                } catch (InterruptedException e) {
                    // We're canceled
                    break;
                }

                if (wu == ProdConsBuffer.theEnd) {
                    break;
                }
            }
        }
    }


    /**
     * Nested WriterThread class
     */
    private class WriterThread extends Thread {

        private MultiThreadedJob job;

        public WriterThread(MultiThreadedJob job) {
            this.job = job;
        }

        @Override
        public void run() {

            int count = 0;
            while (true) {

                if (this.job.isCanceled) {
                    break;
                }

                WorkUnit wu;
                try {
                    wu = this.job.toWrite.take();
                } catch (InterruptedException e) {
                    //  We're canceled
                    break;
                }

                ImgTransformOutcome outcome = null;

                if (wu == ProdConsBuffer.theEnd) {
                    break;
                }

                try {
                    long t1 = System.currentTimeMillis();
                    wu.writeImage();
                    long t2 = System.currentTimeMillis();
                    this.job.profiling.writeTime += (t2 - t1) / 1000F;
                    outcome = new ImgTransformOutcome(true, wu.inputFile, wu.outputFile, null);
                } catch (IOException e) {
                    outcome = new ImgTransformOutcome(false, wu.inputFile, null, e);
                }

                this.job.outcomes.add(outcome);

                if (this.job.jobWindow != null) {
                    double progress = 1.0 * (++count) / this.job.inputFiles.size();
                    this.job.jobWindow.updateProgress(progress);
                }

                if (outcome.success) {
                    ICS432ImgApp.statistics.newlyProcessedImage(this.job.imgTransform);
                }

                if (this.job.jobWindow != null) {
                    if (outcome.success) {
                        this.job.jobWindow.updateDisplayAfterImgProcessed(outcome.outputFile);
                    }
                }
            }
        }
    }

}
