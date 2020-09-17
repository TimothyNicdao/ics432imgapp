package ics432.imgapp;

import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.geometry.Insets;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements a "Job Window" on which a user
 * can launch a Job
 */

class JobWindow extends Stage {

    private Path targetDir;
    private final List<Path> inputFiles;
    private final FileListWithViewPort flwvp;
    private final Button changeDirButton;
    private final TextField targetDirTextField;
    private final Button runButton;
    private final Button closeButton;
    private final ComboBox<ImgTransform> imgTransformList;
    private final ObservableList<String> examples;
    private final ListView<String> examplesView;
    private long totalTime;
    private Job a;

    /**
     * Constructor
     *
     * @param windowWidth      The window's width
     * @param windowHeight     The window's height
     * @param X          The horizontal position of the job window
     * @param Y          The vertical position of the job window
     * @param id         The id of the job
     * @param inputFiles The batch of input image files
     */
    JobWindow(int windowWidth, int windowHeight, double X, double Y, int id, List<Path> inputFiles) {

        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // Set up instance variables
        targetDir = Paths.get(System.getProperty("user.dir"));
        this.inputFiles = inputFiles;

        // Set up the window
        this.setX(X);
        this.setY(Y);
        this.setTitle("Image Transformation Job #" + id);
        this.setResizable(false);

        // Make this window non closable
        this.setOnCloseRequest(Event::consume);

        // Create all sub-widgets in the window
        Label targetDirLabel = new Label("Target Directory:");
        targetDirLabel.setPrefWidth(115);

        // Create a "change target directory"  button
        this.changeDirButton = new Button("");
        this.changeDirButton.setId("changeDirButton");
        this.changeDirButton.setPrefHeight(buttonPreferredHeight);
        Image image = Util.loadImageFromResourceFile("main", "folder-icon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        this.changeDirButton.setGraphic(imageView);

        // Create a "target directory"  textfield
        this.targetDirTextField = new TextField(this.targetDir.toString());
        this.targetDirTextField.setDisable(true);
        HBox.setHgrow(targetDirTextField, Priority.ALWAYS);

        // Create an informative label
        Label transformLabel = new Label("Transformation: ");
        transformLabel.setPrefWidth(115);

        //  Create the pulldown list of image transforms
        this.imgTransformList = new ComboBox<>();
        this.imgTransformList.setId("imgTransformList");  // For TestFX
        OilFilter oil4Filter = new OilFilter();
        oil4Filter.setRange(4);

        this.imgTransformList.setItems(FXCollections.observableArrayList(
                new ImgTransform("Invert", new InvertFilter()),
                new ImgTransform("Solarize", new SolarizeFilter()),
                new ImgTransform("Oil4", oil4Filter)
        ));

        this.imgTransformList.getSelectionModel().selectFirst();  //Chooses first imgTransform as default

        // Create a "Run" button
        this.runButton = new Button("Run job (on " + inputFiles.size() + " image" + (inputFiles.size() == 1 ? "" : "s") + ")");
        this.runButton.setId("runJobButton");
        this.runButton.setPrefHeight(buttonPreferredHeight);

        // Create the FileListWithViewPort display
        this.flwvp = new FileListWithViewPort(windowWidth *  0.98, windowHeight - 4 * buttonPreferredHeight - 3 *  5, false);
        this.flwvp.addFiles(inputFiles);

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);

        // Set actions for all widgets
        this.changeDirButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose target directory");
            File dir = dirChooser.showDialog(this);
            this.setTargetDir(Paths.get(dir.getAbsolutePath()));
        });

        this.runButton.setOnAction(e -> {
            this.closeButton.setDisable(true);
            this.changeDirButton.setDisable(true);
            this.runButton.setDisable(true);
            this.imgTransformList.setDisable(true);

            executeJob(imgTransformList.getSelectionModel().getSelectedItem());

            this.closeButton.setDisable(false);
        });

        this.closeButton.setOnAction(f -> this.close());

        this.examples = FXCollections.observableArrayList();
        this.examplesView = new ListView<>(examples);
        this.examplesView.setPrefWidth(windowWidth * 0.61);
        this.examplesView.setMaxWidth(windowWidth * 0.61);
        // Build the scene
        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().add(targetDirLabel);
        row1.getChildren().add(changeDirButton);
        row1.getChildren().add(targetDirTextField);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().add(transformLabel);
        row2.getChildren().add(imgTransformList);
        layout.getChildren().add(row2);

        layout.getChildren().add(flwvp);

        HBox row3 = new HBox(5);
        row3.getChildren().add(runButton);
        row3.getChildren().add(closeButton);
        row3.getChildren().add(examplesView);
        row3.setMargin(examplesView, new Insets(0, 0, 0, 150));

        layout.getChildren().add(row3);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();
    }

    /**
     * Method to add a listener for the "window was closed" event
     *
     * @param listener The listener method
     */
    public void addCloseListener(Runnable listener) {
        this.addEventHandler(WindowEvent.WINDOW_HIDDEN, (event) -> listener.run());
    }

    /**
     * Method to set the target directory
     *
     * @param dir A directory
     */
    private void setTargetDir(Path dir) {
        if (dir != null) {
            this.targetDir = dir;
            this.targetDirTextField.setText(targetDir.toAbsolutePath().toString());
        }
    }

    public class MyThread extends Thread {

      @Override
      public void run() {
        a.execute();
      }
    }

    /**
     * A method to execute the job
     *
     * @param imgTransform The imgTransform to apply to input images
     */
    private void executeJob(ImgTransform imgTransform) {

        // Clear the display
        this.flwvp.clear();

        // Create a job
        Job job = new Job(imgTransform, this.targetDir, this.inputFiles);
        a = job;
        MyThread t = new MyThread();
        t.start();

        // Execute it

        // Process the outcome
        List<Path> toAddToDisplay = new ArrayList<>();

        StringBuilder errorMessage = new StringBuilder();

        try{
          t.join();
        }catch (InterruptedException e){}

        for (Job.ImgTransformOutcome o : job.getOutcome()) {
          System.err.println(job.getOutcome().size());
            if (o.success) {
                toAddToDisplay.add(o.outputFile);
                System.err.println(o.outputFile + "Time...." + " Read time: " + o.readTime + " | " + " Write time: " + o.writeTime + " | " + " Process time: " + o.processTime);
                totalTime = o.readTime + o.writeTime + o.processTime;

                this.examples.add("Path: " + o.outputFile + " Read: " + (o.readTime) + " Write: " + (o.writeTime) + " Process: " + (o.processTime) + " Total: " + totalTime);
                examplesView.setItems(examples);


            } else {
                errorMessage.append(o.inputFile.toAbsolutePath().toString()).append(": ").append(o.error.getMessage()).append("\n");
            }
        }

        // Pop up error dialog if needed
        if (!errorMessage.toString().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ImgTransform Job Error");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
        }

        // Update the viewport
        this.flwvp.addFiles(toAddToDisplay);
    }
}
