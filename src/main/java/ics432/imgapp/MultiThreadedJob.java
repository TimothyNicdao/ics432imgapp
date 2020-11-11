package ics432.imgapp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MultiThreadedJob extends Job {

    private int bufferSize;


    /**
     * Constructor
     * @param jobWindow: the JobWindow that started this job
     */
    public MultiThreadedJob(Path targetDir,
                            List<Path> inputFiles,
                            ImgTransform imgTransform,
                            int bufferSize,
                            JobWindow jobWindow, MainWindow mw) {
        super(targetDir, inputFiles, imgTransform, bufferSize, jobWindow, mw);
        this.bufferSize =  bufferSize;
    }
 

    /**
     * Method to execute the job, synchronously (i.e., the method is blocking)
     */
    public void execute() {

        long start = System.currentTimeMillis();

        if (this.jobWindow != null) {
            this.jobWindow.updateProgress(0.0);
        }

        while(this.jobWindow.jobDone != true) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        this.profiling.totalExecutionTime = (System.currentTimeMillis() - start) / 1000F;

        if (this.jobWindow != null) {
            this.jobWindow.updateDisplayAfterJobCompletion(
                    this.isCanceled,
                    this.outcomes,
                    this.profiling);
        }


    }

}
