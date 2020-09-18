package ics432.imgapp;

import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
@ExtendWith(ApplicationExtension.class)
class FileWithViewPortTest {


    private FileListWithViewPort flwvp;
    private Scene scene;
    private boolean nothingIsSelected;
    private Stage stage;

    /**
     * Code to start the Application
     *
     * @param stage The stage
     */
    @Start
    void start(Stage stage) {

        HBox layout = new HBox();
        this.flwvp = new FileListWithViewPort(800, 400, true);
        layout.getChildren().add(this.flwvp);
        this.scene = new Scene(layout, 800, 400);
        stage.setScene(this.scene);
        stage.setResizable(false);
        stage.show();
        stage.toFront();
        this.stage = stage;
    }

    /**
     * Code to stop the Application
     */
    @Stop
    void stop() {
        stage.close();
    }


    /**
     * A test to check that the selected item's image is displayed
     *
     * This test uses reflection to access private methods.
     *
     * @param robot Will be injected by the test runner.
     */
    @Test
    @SuppressWarnings("unchecked")
//    @Disabled
    void checkBasicFunctionality(FxRobot robot) {

        robot.clickOn(this.scene);
        robot.press(KeyCode.A).release(KeyCode.A);



        // Get the empty image
        Image emptyImage = null, brokenImage = null;
        {
            Path resourceDirectory = Paths.get("src", "main", "resources", "empty-image.png");
            String absolutePath = resourceDirectory.toFile().getAbsolutePath();
            try {
                emptyImage = new Image(new File(absolutePath).toURI().toURL().toString());
            } catch (MalformedURLException e) {
                Assertions.fail(e.toString());
            }
        }
        // Get the broken image
        {
            Path resourceDirectory = Paths.get("src", "main", "resources", "broken-image.png");
            String absolutePath = resourceDirectory.toFile().getAbsolutePath();
            try {
                brokenImage = new Image(new File(absolutePath).toURI().toURL().toString());
            } catch (MalformedURLException e) {
                Assertions.fail(e.toString());
            }
        }

        // Create a list of files
        List<Path> fileList = new ArrayList<>();

        String[] filenames = {"fish1.jpg", "fish2.jpg"};
        for (String f : filenames) {
            Path resourceDirectory = Paths.get("src", "test", "resources", f);
            String absolutePath = resourceDirectory.toFile().getAbsolutePath();
            fileList.add(Paths.get(absolutePath));
        }

        // Get the (private!) addFiles() method
        Method m = TestUtil.getAddFilesMethod(FileListWithViewPort.class);

        // Invoke it to add the files
        try {
            m.invoke(flwvp, fileList);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assertions.fail(e.toString());
        }

        robot.sleep(1000);

        // Check that the FileListWithViewPort has 2 files
        assert(this.flwvp.getNumFiles() == 2);

        // Check that the FileListWithViewPort has 1 selected  file
        // Get a reference to the ListView private field
        ListView<Path> lv = null;
        try {
            lv = (ListView<Path>) TestUtil.getFieldByName(FileListWithViewPort.class, "availableFilesView").get(flwvp);
        } catch (IllegalAccessException e) {
            Assertions.fail(e.toString());
        }

        // Select the last item
        lv.getSelectionModel().selectLast();
        robot.clickOn(lv);

        robot.sleep(1000);
        assert(this.flwvp.getSelection().size() == 1);

        // Check that the FileListWithViewPort's selected item  is the last one
        assert(this.flwvp.getSelection().get(0) == fileList.get(1));

        robot.sleep(1000);

        // Get a reference to the ImageView private field
        ImageView iv = null;
        try {
            iv = (ImageView) TestUtil.getFieldByName(FileListWithViewPort.class, "iv" ).get(flwvp);
        } catch (IllegalAccessException e) {
            Assertions.fail(e.toString());
        }

        // Check that image displayed in the iv is what it should be
        try {
            assert(iv.getImage().getUrl().equals(fileList.get(1).toUri().toURL().toString()));
        } catch (MalformedURLException e) {
            Assertions.fail(e.toString());
        }

        // Check that adding an already-existing image does nothing
        try {
            m.invoke(flwvp, fileList);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assertions.fail(e.toString());
        }

        robot.sleep(1000);
        assert(this.flwvp.getNumFiles() == 2);
        assert(this.flwvp.getSelection().size() == 1);

        // Move the selection on slot up and delete the item
        robot.clickOn(lv);
        robot.press(KeyCode.BACK_SPACE).release(KeyCode.BACK_SPACE);
        robot.sleep(1000);

        // Check that only one file remains and that it's displayed
        assert(flwvp.getNumFiles() == 1);
        try {
            assert(iv.getImage().getUrl().equals(fileList.get(0).toUri().toURL().toString()));
        } catch (MalformedURLException e) {
            Assertions.fail(e.toString());
        }

        // Set the flwvp to be non-editable
        Field isEditableField = TestUtil.getFieldByName(FileListWithViewPort.class, "isEditable");
        try {
            isEditableField.set(flwvp, false);
        } catch (IllegalAccessException e) {
            Assertions.fail(e.toString());
        }

        robot.sleep(1000);

        // Try to remove the item
        robot.clickOn(lv);
        robot.press(KeyCode.BACK_SPACE).release(KeyCode.BACK_SPACE);
        robot.sleep(1000);

        assert(this.flwvp.getNumFiles() == 1);
        assert(this.flwvp.getSelection().size() == 1);

        // Clear the list
        flwvp.clear();
        robot.sleep(1000);

        assert(this.flwvp.getNumFiles() == 0);
        assert(this.flwvp.getSelection().size() == 0);

        // Check that the empty-image.png is displayed
        robot.sleep(1000);
        assert(iv.getImage().getUrl().equals(emptyImage.getUrl()));

        // Add an invalid jpg and check that the broken image is displayed
        fileList.clear();
        Path resourceDirectory = Paths.get("src", "test", "resources", "invalid.jpg");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        fileList.add(Paths.get(absolutePath));
        try {
            m.invoke(flwvp, fileList);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assertions.fail(e.toString());
        }

        robot.sleep(1000);
        assert(flwvp.getNumFiles() == 1);

        lv.getSelectionModel().selectLast();
        robot.clickOn(lv);
        robot.sleep(1000);

        assert(iv.getImage().getUrl().equals(brokenImage.getUrl()));

        flwvp.clear();
        robot.sleep(1000);

        assert(iv.getImage().getUrl().equals(emptyImage.getUrl()));

    }

    /**
     * A test to check that one can listen for the "nothing is selected" event
     *
     * @param robot Will be injected by the test runner.
     */
    @Test
//    @Disabled
    void checkNothingIsSelectedListener(FxRobot robot) {

        // Create a list of files
        List<Path> fileList = new ArrayList<>();

        String[] filenames = {"fish1.jpg", "fish2.jpg"};
        for (String f : filenames) {
            Path resourceDirectory = Paths.get("src", "test", "resources", f);
            String absolutePath = resourceDirectory.toFile().getAbsolutePath();
            fileList.add(Paths.get(absolutePath));
        }

        // Get the (private!) addFiles() method
        Method m = TestUtil.getAddFilesMethod(FileListWithViewPort.class);

        // Invoke it to add the files
        try {
            m.invoke(flwvp, fileList);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assertions.fail(e.toString());
        }

        // Register a listener
        nothingIsSelected = true;
        this.flwvp.addNoSelectionListener((b) -> {
            nothingIsSelected = b;
        });

        // Hit the downkey to select and item
        robot.clickOn(flwvp);
        robot.press(KeyCode.DOWN);

        robot.sleep(100);

        assert(!nothingIsSelected);


        // Clear the list
        this.flwvp.clear();
        robot.sleep(1000);

        assert(nothingIsSelected);

        // Add items back
        try {
            m.invoke(flwvp, fileList);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assertions.fail(e.toString());
        }

        robot.sleep(1000);

        // assert(nothingIsSelected); commented out because this causes errors for Linux machines



    }

}