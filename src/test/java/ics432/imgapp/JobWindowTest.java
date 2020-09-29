package ics432.imgapp;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@ExtendWith(ApplicationExtension.class)
class JobWindowTest {

    private List<Path> fileList;
    private JobWindow jw;
    private boolean wasClosed;

    /**
     * Code to start the Application
     *
     * @param stage The stage
     */
    @Start
    void start(Stage stage) {

        // Create a list of files
        fileList = new ArrayList<>();

        String[] filenames = {"fish1.jpg", "fish2.jpg"};
        for (String f : filenames) {
            Path resourceDirectory = Paths.get("src", "test", "resources", f);
            String absolutePath = resourceDirectory.toFile().getAbsolutePath();
            fileList.add(Paths.get(absolutePath));
        }

        this.jw = new JobWindow(800, 400, 0,0,0, fileList);
    }

    /**
     * Code to stop the Application
     */
    @Stop
    void stop() {
        this.jw.close();
    }

    /**
     * A test to check that buttons are set up correctly
     *
     * @param robot Will be injected by the test runner.
     */
    @Test
//    @Disabled
    void checkButtonsOnStartup(FxRobot robot) {

        // Change Dir Button
        Button changeDirButton = robot.lookup("#changeDirButton").queryButton();
        Assertions.assertThat(changeDirButton).hasText("");
        Assertions.assertThat(changeDirButton).isEnabled();

        // Run Job Button
        Button runJobButton = robot.lookup("#runJobButton").queryButton();
        Assertions.assertThat(runJobButton).hasText("Run job (on " + fileList.size() + " images)");
        Assertions.assertThat(runJobButton).isEnabled();

        // Close Button
        Button closeButton = robot.lookup("#closeButton").queryButton();
        Assertions.assertThat(closeButton).hasText("Close");
        Assertions.assertThat(closeButton).isEnabled();
    }

    /**
     * A test to check that change Target Dir works
     * <p>
     * This test uses reflection to access private methods.
     *
     * @param robot Will be injected by the test runner.
     */
    @Test
//    @Disabled
    void changeTargetDir(FxRobot robot) {

        // Get a reference to the private targetDirTextField field
        TextField td = null;
        try {
            td = (TextField) TestUtil.getFieldByName(JobWindow.class, "targetDirTextField").get(this.jw);
        } catch (IllegalAccessException e) {
            Assertions.fail(e.toString());
        }

        // Get a reference to the private method setTargetDir()
        Method setTargetDirMethod = TestUtil.getSetTargetDirMethodOfJobWindowClass();

        // Invoke it on some dir
        Path path = Paths.get("src", "test", "resources");
        String absolutePath = path.toFile().getAbsolutePath();
        try {
            setTargetDirMethod.invoke(jw, path);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assertions.fail(e.toString());
        }

        // Check the textfield
        assert(td.getText().equals(absolutePath));
    }

    /**
     * A test to simple bring up the FileChooser when changing the target dir
     * <p>
     * This test uses reflection to access private methods.
     *
     * @param robot Will be injected by the test runner.
     */
    @Test
    @Disabled
    void clickChangeDir(FxRobot robot) {

        Button changeDirButton = robot.lookup("#changeDirButton").queryButton();
        robot.clickOn(changeDirButton);

        // TODO: Would be nice to interact with DirChooser dialog, but
        // TODO: it's not clear how to get a handle to it as it is not a
        // TODO: JavaFX object but a native widget...
    }


    /**
     * Check that the "Close" button works
     *
     * @param robot Will be injected by the test runner.
     */
    @Test
//    @Disabled
    void checkCloseButton(FxRobot robot) {

        // Close Button
        Button closeButton = robot.lookup("#closeButton").queryButton();

        robot.clickOn(closeButton);
        robot.sleep(1000);

        assert(! this.jw.isShowing());
    }

    /**
     * Check that one can add a listener to the "window closed" event
     *
     * @param robot Will be injected by the test runner.
     */
    @Test
//    @Disabled
    void checkCloseListener(FxRobot robot) {

        this.wasClosed = false;
        this.jw.addCloseListener(() -> {this.wasClosed = true;});

        Button closeButton = robot.lookup("#closeButton").queryButton();
        robot.clickOn(closeButton);
        robot.sleep(1000);

        assert(this.wasClosed);

    }


    /**
     * Check that a job can run
     */
    @Test
//    @Disabled
    void checkJobExecution(FxRobot robot) {

        Button runJobButton = robot.lookup("#runJobButton").queryButton();
        ComboBox<ImgTransform> imgTransformList = robot.lookup("#imgTransformList").queryComboBox();

        // Select the Invert imgTransform
        Platform.runLater(()-> {
            imgTransformList.getSelectionModel().select(0);
        });

        robot.clickOn(runJobButton);

        robot.sleep(1000);

        // Make sure that processing has happened
        String inputFileName = "fish1.jpg";

        // Test coverage
        {
            Image bogus = Util.loadImageFromDir("whatever", "bogus");
            assert(bogus == null);
        }

        Image displayedImage = Util.loadImageFromDir(System.getProperty("user.dir"), "Invert_" + inputFileName);
        Image expectedImage = Util.loadImageFromResourceFile("test", "correct_invert_" + inputFileName);


        assert(TestUtil.checkPixels(displayedImage, expectedImage));
    }

    /**
     * Check that an invalid job execution causes an Alert to popup
     */
    @Test
//    @Disabled
    void checkInvalidJobExecution(FxRobot robot) {

        Button runJobButton = robot.lookup("#runJobButton").queryButton();
        ComboBox<ImgTransform> imgTransformList = robot.lookup("#imgTransformList").queryComboBox();

        // Select the Invert imgTransform
        Platform.runLater(()-> {
            imgTransformList.getSelectionModel().select(0);
        });


        // Set the target directory to a bogus path
        Method setTargetDirMethod = TestUtil.getSetTargetDirMethodOfJobWindowClass();
        String bogusPath = "/invalid/bogus/weird/";
        try {
            setTargetDirMethod.invoke(jw, Paths.get(bogusPath));
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assertions.fail(e.toString());
        }

        // Attempt to run the job
        robot.clickOn(runJobButton);

        robot.sleep(1000);

        boolean alert_found = false;
        for (int i=0; i < Stage.getWindows().size(); i++) {
            Window w = Stage.getWindows().get(i);

            // Really terrible way of determining  whether the window is indeed a Dialog
            if (w.getClass().toString().contains("Dialog")) {
                alert_found = true;

                // Dismiss it!
                robot.clickOn(w);
                robot.press(KeyCode.ENTER).release(KeyCode.ENTER);

                break;
            }
        }
        assert(alert_found);
    }

}
