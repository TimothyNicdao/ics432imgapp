package ics432.imgapp;

import javafx.application.Platform;
import com.jhlabs.image.InvertFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


@ExtendWith(ApplicationExtension.class)
class JobTest {

    @Test
//    @Disabled
    void checkValidJobExecution() {

        // Create a list of files
        List<Path> fileList = new ArrayList<>();

        String[] filenames = {"fish1.jpg", "fish2.jpg"};
        for (String f : filenames) {
            Path resourceDirectory = Paths.get("src", "test", "resources", f);
            String absolutePath = resourceDirectory.toFile().getAbsolutePath();
            fileList.add(Paths.get(absolutePath));
        }

        // Create a job
        Path tmpPath = null;
        try {
            tmpPath = Files.createTempDirectory("ics432_img_test");
        } catch (IOException e) {
            Assertions.fail(e.toString());
        }
        String targetDirPath = tmpPath.toAbsolutePath().toString();

        ImgTransform imgTransform = new ImgTransform("Invert", new InvertFilter());
        Job job = new Job(imgTransform, Paths.get(targetDirPath), fileList);

        Platform.runLater(()-> {
            volatile JobWindow window = new JobWindow(100, 100, 100 ,100, 1 , null);
        });

        waitForRunLater();

        // Execute it
        job.execute(window);

        int count = 0;
        for (Job.ImgTransformOutcome outcome : job.getOutcome()) {
            assert(outcome.success);
            assert(outcome.error == null);
            assert(outcome.inputFile == fileList.get(count));
            assert(outcome.outputFile.toAbsolutePath().toString().equals(targetDirPath + System.getProperty("file.separator") + imgTransform.getName() + "_" + filenames[count]));
            assert(Files.exists(Paths.get(outcome.outputFile.toAbsolutePath().toString())));
            assert(TestUtil.checkPixels(outcome.outputFile,
                    Paths.get("src", "test", "resources", "correct_invert_" + filenames[count])));
            // Clean up
            outcome.outputFile.toFile().delete();
            count++;
        }

        // Clean up
        tmpPath.toFile().delete();


    }

    @Test
//    @Disabled
    void checkInvalidInputFile() {

        // Create a list of files
        List<Path> fileList = new ArrayList<>();
        fileList.add(Paths.get("/invalid/bogus/weird/none.jpg"));

        // Create a job
        Path tmpPath = null;
        try {
            tmpPath = Files.createTempDirectory("ics432_img_test");
        } catch (IOException e) {
            Assertions.fail(e.toString());
        }
        String targetDirPath = tmpPath.toAbsolutePath().toString();

        ImgTransform imgTransform = new ImgTransform("Invert", new InvertFilter());
        Job job = new Job(imgTransform, Paths.get(targetDirPath), fileList);

        Platform.runLater(()-> {
            volatile JobWindow window = new JobWindow(100, 100, 100 ,100, 1 , null);
        });
        
        waitForRunLater();

        // Execute it
        job.execute(window);

        assert(job.getOutcome().size() == 1);

        Job.ImgTransformOutcome outcome = job.getOutcome().get(0);

        assert(!outcome.success);
        assert(outcome.outputFile == null);
        assert(outcome.error != null);
        assert(outcome.error.getClass() == IOException.class);

        tmpPath.toFile().delete();
    }

    @Test
//    @Disabled
    void checkInvalidTargetDir() {

        // Create a list of files
        List<Path> fileList = new ArrayList<>();
        Path resourceDirectory = Paths.get("src", "test", "resources", "fish1.jpg");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        fileList.add(Paths.get(absolutePath));

        // Create a job
        String targetDirPath = "/invalid/bogus/weird/";
        ImgTransform imgTransform = new ImgTransform("Invert", new InvertFilter());
        Job job = new Job(imgTransform, Paths.get(targetDirPath), fileList);

        Platform.runLater(()-> {
            volatile JobWindow window = new JobWindow(100, 100, 100 ,100, 1 , null);
        });

        waitForRunLater(); 

        // Execute it
        job.execute(window);

        assert(job.getOutcome().size() == 1);

        Job.ImgTransformOutcome outcome = job.getOutcome().get(0);

        assert(!outcome.success);
        assert(outcome.outputFile == null);
        assert(outcome.error != null);
        assert(outcome.error.getClass() == IOException.class);
    }

    // @Test
    // // Method to wait for javafx threads
    // void waitForRunLater() throws InterruptedException {
    //     Semaphore semaphore = new Semaphore(0);
    //     Platform.runLater(() -> semaphore.release());
    //     semaphore.acquire();
    
    // }

}
