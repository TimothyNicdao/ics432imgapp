package ics432.imgapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements a "Job Window" on which a user
 * can launch a "job", i.e., apply a imgTransform to a batch of input image files
 */

class JobWindow extends Stage {

    private Path targetDir;
    private boolean multithreading;
    private int maxNumImagedInRAM;
    private final List<Path> inputFiles;
    private final FileListWithViewPort flwvp;
    private final Button changeDirButton;
    private final TextField targetDirTextField;
    private final Button runButton;
    private final Button cancelButton;
    private final Button closeButton;
    private final ComboBox<ImgTransform> imgTransformList;
    private JobThread runningJob = null;
    private MainWindow mw;
    protected boolean jobDone = false;

    private ProgressArea progressArea;

    /**
     * Constructor
     *
     * @param windowWidth      The window's width
     * @param windowHeight     The window's height
     * @param X          The horizontal position of the job window
     * @param Y          The vertical position of the job window
     * @param inputFiles The batch of input image files
     * @param maxNumImagedInRAM Bound on number of images in RAM
     */
    JobWindow(int windowWidth, int windowHeight, double X, double Y, int id, List<Path> inputFiles, boolean multithreading, int maxNumImagedInRAM, MainWindow mw) {

        double buttonPreferredHeight = 27.0;

        // Set up instance variables
        targetDir = Paths.get(System.getProperty("user.dir"));
        this.inputFiles = inputFiles;
        this.multithreading = multithreading;
        this.maxNumImagedInRAM = maxNumImagedInRAM;
        this.mw = mw;

        // Set up the window
        this.setX(X);
        this.setY(Y);
        this.setTitle("Image Transformation Job #" + id);
        this.setResizable(false);

        // Make this window non closable
        this.setOnCloseRequest(Event::consume);

        // Create all widgets in the window
        Label targetDirLabel = new Label("Target Directory:");
        targetDirLabel.setPrefWidth(115);

        changeDirButton = new Button("");
        changeDirButton.setId("changeDirButton");
        changeDirButton.setPrefHeight(buttonPreferredHeight);
        Image image = Util.loadImageFromResourceFile("main", "folder-icon.png");

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        changeDirButton.setGraphic(imageView);

        this.targetDirTextField = new TextField(this.targetDir.toString());
        targetDirTextField.setDisable(true);
        HBox.setHgrow(targetDirTextField, Priority.ALWAYS);

        Label transformLabel = new Label("Transformation: ");
        transformLabel.setPrefWidth(115);

        imgTransformList = new ComboBox<>();
        imgTransformList.setId("imgTransformList");  // For TestFX

        ICS432ImgApp.updateDPThreads(this.mw.dpThreadAmount);
        WorkUnit.updateDPThreads(this.mw.dpThreadAmount);
        imgTransformList.setItems(FXCollections.observableArrayList(
                ICS432ImgApp.filters
        ));

        imgTransformList.getSelectionModel().selectFirst();  //Chooses first imgTransform as default

        this.runButton = new Button("Run job (on " + inputFiles.size() + " image" + (inputFiles.size() == 1 ? "" : "s") + ")");
        this.runButton.setId("runJobButton");
        this.runButton.setPrefHeight(buttonPreferredHeight);

        this.flwvp = new FileListWithViewPort(windowWidth *  0.98, windowHeight - 4 * buttonPreferredHeight - 3 *  5, false);
        this.flwvp.addFiles(inputFiles);

        this.cancelButton = new Button("Cancel");
        this.cancelButton.setId("cancelJobButton");
        this.cancelButton.setPrefHeight(buttonPreferredHeight);
        this.cancelButton.setDisable(true);

        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);


        // Set actions for all widgets
        changeDirButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose target directory");
            File dir = dirChooser.showDialog(this);
            this.setTargetDir(Paths.get(dir.getAbsolutePath()));
        });

        runButton.setOnAction(e -> {
            this.closeButton.setDisable(true);
            this.changeDirButton.setDisable(true);
            this.runButton.setDisable(true);
            this.cancelButton.setDisable(false);
            this.imgTransformList.setDisable(true);

            startJob();


        });

        closeButton.setOnAction(f -> this.close());

        cancelButton.setOnAction(f -> {
            if (this.runningJob != null) {
                runningJob.cancel();
            }
            this.cancelButton.setDisable(true);
        });

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
        row3.getChildren().add(cancelButton);
        row3.getChildren().add(closeButton);

        this.progressArea = new ProgressArea(5);
        row3.getChildren().add(this.progressArea);

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
            targetDir = dir;
            this.targetDirTextField.setText(targetDir.toAbsolutePath().toString());
        }
    }


    private void startJob() {

        // Clear the display
        this.flwvp.clear();

        // Create the Job
        Job job;
        job  = new MultiThreadedJob(this.targetDir,
                this.inputFiles,
                this.imgTransformList.getSelectionModel().getSelectedItem(),
                this.maxNumImagedInRAM,
                this, this.mw);

        // TODO: Launch it in a Cancellable Thread
        this.runningJob = new JobThread(job);
        this.runningJob.start();
        
    }

    /**
     * Method to update the display upon image processed successfully
     * @param outputFile The path to the output file
     */
    public void updateDisplayAfterImgProcessed(Path outputFile) {
        this.flwvp.addFile(outputFile);
    }

    /**
     * Method to update the display upon job completion
     * @param wasCancelled: true if the job was canceled
     * @param outcomes: Job outcomes
     */
    public void updateDisplayAfterJobCompletion(boolean wasCancelled,
                                                List<Job.ImgTransformOutcome> outcomes,
                                                Job.Profiling profiling) {

        String textToShow;

        if (! wasCancelled) {
            textToShow =
                    "Total Time: " + String.format("%.2f sec", profiling.totalExecutionTime) + " | " +
                            "Read Time: " + String.format("%.2f sec", profiling.readTime) + " | " +
                            "Write Time: " + String.format("%.2f sec", profiling.writeTime) + " | " +
                            "Processing Time: " + String.format("%.2f sec", profiling.processingTime);
        } else {
            textToShow = "CANCELED";
        }

        this.progressArea.showText(textToShow);

        // Process the outcome
        List<Path> toAddToDisplay = new ArrayList<>();

        StringBuilder errorMessage = new StringBuilder();
        for (Job.ImgTransformOutcome o : outcomes) {
            if (o.success) {
                toAddToDisplay.add(o.outputFile);
            } else {
                errorMessage.append(o.inputFile.toAbsolutePath().toString()).append(": ").append(o.error.getMessage()).append("\n");
            }
        }

        // Pop up error dialog if needed
        if (!errorMessage.toString().equals("")) {
            Platform.runLater(()-> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ImgTransform Job Error");
                alert.setHeaderText(null);
                alert.setContentText(errorMessage.toString());
                alert.showAndWait();
            });
        }

        // Update the viewport
        this.flwvp.addFiles(toAddToDisplay);
        this.cancelButton.setDisable(true);
        this.closeButton.setDisable(false);
    }

    /**
     * Method to update job progress
     */
    public void updateProgress(double progress)  {
        this.progressArea.updateProgressBar(progress);
    }

    /**
     * Nested helper class
     */
    private class ProgressArea extends HBox {

        ProgressBar progressBar;
        Label textLabel;

        public ProgressArea(int spacing) {
            super(spacing);
            this.setAlignment(Pos.CENTER_LEFT);

            this.progressBar = new ProgressBar(0.0);
            this.textLabel = new Label("");
            this.textLabel.setFont(new Font(15));
            this.textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        }

        public void updateProgressBar(double progress){
            Platform.runLater(() ->  {
                this.getChildren().remove(this.textLabel);
                if (this.getChildren().indexOf(this.progressBar)  == -1) {
                    this.getChildren().add(progressBar);
                }
                this.progressBar.progressProperty().setValue(progress);
            });
        }

        public void showText(String text) {
            Platform.runLater(() -> {
                this.getChildren().remove(this.progressBar);
                this.textLabel.setText("  " + text);
                this.getChildren().add(this.textLabel);
            });
        }
    }

    /**
     * Nested class
     */
    private class JobThread extends Thread {

        private Job job;

        public JobThread(Job job)  {
            this.job = job;
        }

        public void cancel() {
            this.job.isCanceled = true;
        }

        @Override
        public void run() {
            this.job.execute();
        }

    }



}
