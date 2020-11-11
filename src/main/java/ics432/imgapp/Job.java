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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.lang.Thread;

import static javax.imageio.ImageIO.createImageOutputStream;

/**
 * A class that defines the "job" abstraction, that is, a set of input image
 * files to which an ImgTransform must be applied, thus generating a set of
 * output image files. Each output file name is the input file name prepended
 * with the ImgTransform name and an underscore.
 */
class Job {

    private Double imageSizeTotal = 0.0;
    private Double computeSpeed = 0.0;
    private ArrayBlockingQueue<WorkUnit> inputBuffer;
    private ArrayBlockingQueue<WorkUnit> outputBuffer;
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
    private boolean changeProcessorCount = false;
    private volatile int currentProcessorCount;
    private ExecutorService pool = Executors.newFixedThreadPool(1);

    /**
     * Constructor
     *
     * 
     * @param targetDir    The target directory in which to generate output images
     * @param inputFiles   The list of input file paths
     */
    Job(MainWindow mw) {
        this.outcome = new ArrayList<>();
        this.inputBuffer = new ArrayBlockingQueue<WorkUnit>(16, true);
        this.outputBuffer = new ArrayBlockingQueue<WorkUnit>(16, true);
        this.mw = mw;
    }

    /**
     * Method to execute the imgTransform job
     */
    void execute() {

            Runnable readRun = () -> {
                readFunction();
            };
            Thread readThread = new Thread(readRun);

            createProcessorThreads();

            Runnable writeRun = () -> {
                writeFunction();
            };
            Thread writeThread = new Thread(writeRun);

            readThread.setDaemon(true);
            readThread.start();
            // processThread.start();
            writeThread.setDaemon(true);
            writeThread.start();

    }

    private void readFunction() {
        // this.totalStartTime = System.nanoTime(); workunit must be added

        // set as infinite loop because this will be run as daemon thread in order to indefinitely process incoming work. 
        while(true) {
            System.out.println("Reading");
            WorkUnit work;
            // wait for notification if there are no jobs to be done to avoid wasting cpu cycles. 
            synchronized(this.mw.todo)
            {
                while (this.mw.todo.size() == 0 ) {
                    try {
                        this.mw.todo.wait();
                    } catch (InterruptedException e) {
                    }
                }
                work = this.mw.todo.remove(0);
            }   

            if(!work.poisoned && !work.jw.isCancelled()){

                System.err.println("Applying " + work.imgTransform.getName() + " to "
                                + work.inputFile.toAbsolutePath().toString() + " ...");
                // Load the image from file
                Image image;
                try {
                    long readStartTime = System.nanoTime();
                    image = new Image(work.inputFile.toUri().toURL().toString());
                    long readEndTime = System.nanoTime();
                    work.readTime = readEndTime - readStartTime;
                    work.image = image; 
                    try {
                        this.inputBuffer.put(work);   
                    } catch (Exception e) {
                        
                    }
                    synchronized(this) {
                        notifyAll();
                    }
                    if (image.isError()) {
                        throw new IOException("Error while reading from " + work.inputFile.toAbsolutePath().toString() + " ("
                                + image.getException().toString() + ")");
                    }
                } catch (IOException e) {
                    try {
                        throw new IOException("Error while reading from " + work.inputFile.toAbsolutePath().toString());
                    } catch (IOException e1) {
                    }
                }
                image = null;
            }else{
                // Do nothing when a work is in the queue whos job has been cancelled. 
                try {
                    this.inputBuffer.put(work);   
                } catch (Exception e) {
                    
                }
                synchronized(this) {
                    notifyAll();
                }
            }

        }
    }

    private void processFunction() {
        while (true) {
            System.out.println("Processing");
            // if (changeProcessorCount){
            //     if (currentProcessorCount == 1){
            //         this.changeProcessorCount = false;
            //         this.createProcessorThreads();

            //         break;
            //     }else{
            //         currentProcessorCount--;
            //         break;
            //     }
            // }

            WorkUnit work; 
            synchronized(this) {
                while (this.inputBuffer.peek() == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
                work =  this.inputBuffer.poll();
            }
            
           
            if(!work.poisoned && !work.jw.isCancelled()){
                // Process the image
                long processStartTime = System.nanoTime();
                BufferedImage img = work.imgTransform.getBufferedImageOp().filter(SwingFXUtils.fromFXImage(work.image, null), null);
                long processEndTime = System.nanoTime();
                work.processTime = processEndTime - processStartTime;
                work.bufferedImage = img;
                
                try {
                    this.outputBuffer.put(work);
                } catch (Exception e) {  
                }

                synchronized(this) {
                    notifyAll();
                }

                work = null;
            }else{
                try {
                    this.outputBuffer.put(work);
                } catch (Exception e) {  
                }

                synchronized(this) {
                    notifyAll();
                }

            }
        }
    }

    private void writeFunction() {
        while (true) {
            WorkUnit work;
            synchronized(this) {
                while (this.outputBuffer.peek() == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }

                work =  this.outputBuffer.poll();
                notifyAll();
            }

            if (!work.poisoned && !work.jw.isCancelled()){
                // Write the image back to a file
                System.out.println("Writing!!");
                String outputPath = work.targetDir + System.getProperty("file.separator") + work.imgTransform.getName() + "_" + work.inputFile.getFileName();
                try {
                    long writeStartTime = System.nanoTime();
                    OutputStream os = new FileOutputStream(new File(outputPath));
                    ImageOutputStream outputStream = createImageOutputStream(os);
                    ImageIO.write(work.bufferedImage, "jpg", outputStream);
                    long writeEndTime = System.nanoTime();
                    work.writeTime = writeEndTime - writeStartTime;
                } catch (IOException | NullPointerException e) {
                    try {
                        throw new IOException("Error while writing to " + outputPath);
                    } catch (IOException e1) {
                    }
                }
                Path outputFile = Paths.get(outputPath);
                work.jw.displayJob(new ImgTransformOutcome(true, work.inputFile, outputFile, null));
                            this.mw.increaseImagesProcessed();
                            if (this.mw.sw == null) {
                            } else {
                                this.mw.sw.windowUpdateImagesProcessed();
                            }
                            try {
                                this.imageSizeTotal += Files.size(work.inputFile);
                            } catch (IOException e) {
                                work.jw.displayJob(new ImgTransformOutcome(false, work.inputFile, null, e));
                            }
                work.jw.updateTasksDone();
                this.imagesDone++;
                updateFilter(work);
                Platform.runLater(() -> work.jw.updateTimes(work));
            }else if (work.poisoned){
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                }
                Platform.runLater(() -> work.jw.jobCompleted());
            }else{
                Platform.runLater(() -> work.jw.updateTimes(work));
            }

        }
        // this.totalEndtime = System.nanoTime();
        // this.totalTime = (double)(this.totalEndtime - this.totalStartTime);

        // Platform.runLater(() -> work.jw.jobCompleted()); need to add to read so process and write dont see poisoned
    }

    /**
     * Responsible for managing the correct number of processor threads. 
     *
     * @return The job outcomes, i.e., a list of ImgTransformOutcome objects
     * (in flux if the job isn't done executing)
     */

    public void changeProcessorThreads() {
        System.out.println("Change Called");
        this.changeProcessorCount = true;
        createProcessorThreads();
    }

    // /**
    //  * Responsible for managing the correct number of processor threads. 
    //  *
    //  * @return The job outcomes, i.e., a list of ImgTransformOutcome objects
    //  * (in flux if the job isn't done executing)
    //  */

    // public void poolShutDown() {
    //     this.pool.shutdown();
    // }

    /**
     * Responsible for managing the correct number of processor threads. 
     *
     * @return The job outcomes, i.e., a list of ImgTransformOutcome objects
     * (in flux if the job isn't done executing)
     */

    public void createProcessorThreads() {
        this.pool.shutdown();
        System.out.println("Creating " + (int) mw.processorSlider.getValue() + " threads" );
        // this.pool = Executors.newFixedThreadPool( (int) mw.processorSlider.getValue());
        // for (int i = 0; i < (int) mw.processorSlider.getValue(); i++){
        //    Runnable processRun = () -> {
        //        processFunction();
        //    };
        //    this.pool.execute(processRun);

        Runnable processRun = () -> {
                   processFunction();
               };

        pool = Executors.newFixedThreadPool((int) mw.processorSlider.getValue(),
            new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });

        for (int i = 0; i < (int) mw.processorSlider.getValue(); i++){
            pool.execute(processRun);
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
     * Update proper filter 
     *
     * @return The read time of the job
     */
    public void updateFilter(WorkUnit work) { 
        this.imageSizeTotal = this.imageSizeTotal/100000;
        this.totalTime = this.totalTime/1000000000;
        this.computeSpeed = this.imageSizeTotal/this.totalTime;
        if (work.imgTransform.getName() == "Invert") {
            this.mw.updateInvert(this.computeSpeed);
            if(this.mw.sw == null){}
            else { this.mw.sw.windowUpdateInvert();}
        } else if (work.imgTransform.getName() == "Oil4") {
            this.mw.updateOil(this.computeSpeed);
            if(this.mw.sw == null){}
            else { this.mw.sw.windowUpdateOil();}
        } else if (work.imgTransform.getName() == "Solarize") {
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
    // private Path processInputFile(Path inputFile) throws IOException {
    //     // Load the image from file
    //     Image image;
    //     long readStartTime = System.nanoTime();
    //     try {
    //         image = new Image(inputFile.toUri().toURL().toString());
    //         if (image.isError()) {
    //             throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString() +
    //                     " (" + image.getException().toString() + ")");
    //         }
    //     } catch (IOException e) {
    //         throw new IOException("Error while reading from " + inputFile.toAbsolutePath().toString());
    //     }
    //     long readEndTime = System.nanoTime();
    //     readTime += readEndTime - readStartTime;

    //     // Process the image
    //     long processStartTime = System.nanoTime();
    //     BufferedImage img = imgTransform.getBufferedImageOp().filter(SwingFXUtils.fromFXImage(image, null), null);
    //     long processEndTime = System.nanoTime();
    //     processTime += processEndTime - processStartTime;

    //     // Write the image back to a file
    //     long writeStartTime = System.nanoTime();
    //     String outputPath = this.targetDir + System.getProperty("file.separator") + this.imgTransform.getName() + "_" + inputFile.getFileName();
    //     try {
    //         OutputStream os = new FileOutputStream(new File(outputPath));
    //         ImageOutputStream outputStream = createImageOutputStream(os);
    //         ImageIO.write(img, "jpg", outputStream);
    //     } catch (IOException | NullPointerException e) {
    //         throw new IOException("Error while writing to " + outputPath);
    //     }
    //     long writeEndTime = System.nanoTime();
    //     writeTime += writeEndTime - writeStartTime;

    //     // Success!
    //     return Paths.get(outputPath);
    // }

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
