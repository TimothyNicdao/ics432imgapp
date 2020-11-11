package ics432.imgapp;

import javafx.application.Platform;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;
import javafx.collections.FXCollections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
    private final Button cancelButton;
    private Label readTimeLabel = new Label("Total Read Time: ");
    private Label processTimeLabel = new Label("Total Process Time: ");
    private Label writeTimeLabel = new Label("Total Write Time: ");
    private Label totalTimeLabel = new Label("Total Execution Time: ");
    private Label jobReadValue = new Label("");
    private Label jobProcessValue = new Label("");
    private Label jobWriteValue = new Label("");
    private Label jobTotalValue = new Label("");
    private Task workerTask;
    final Label progressLabel = new Label("Image progress:");
    final ProgressBar progressBar = new ProgressBar(0);
    private final ComboBox<ImgTransform> imgTransformList;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean shouldCancel;
    private boolean jobDone = false;
    private volatile int tasksDone = 0;
    private MainWindow mw;
    private double readTime = 0; 
    private double processTime = 0; 
    private double writeTime = 0; 
    private double totalTime = 0; 

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
    JobWindow(int windowWidth, int windowHeight, double X, double Y, int id, List<Path> inputFiles, MainWindow mainWindow) {
        this.mw = mainWindow;
        
        // Keep track of wether the jobs were cancelled via the cancel button 
        lock.lock();
        try {
            this.shouldCancel = false;
        }
        catch(Exception batcherror) {
            System.out.println(batcherror);
        }
        finally {
            lock.unlock(); 
        }

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

        this.readTimeLabel.setPrefWidth(95);
        this.readTimeLabel.setVisible(false);

        this.processTimeLabel.setPrefWidth(105);
        this.processTimeLabel.setVisible(false);

        this.writeTimeLabel.setPrefWidth(105);
        this.writeTimeLabel.setVisible(false);

        this.totalTimeLabel.setPrefWidth(125);
        this.totalTimeLabel.setVisible(false);

        this.jobReadValue.setPrefWidth(100);
        this.jobReadValue.setVisible(false);

        this.jobProcessValue.setPrefWidth(100);
        this.jobProcessValue.setVisible(false);

        this.jobWriteValue.setPrefWidth(100);
        this.jobWriteValue.setVisible(false);

        this.jobTotalValue.setPrefWidth(100);
        this.jobTotalValue.setVisible(false);

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

        // Progress Label Initialize
        this.progressLabel.setPrefWidth(100);
        this.progressLabel.setVisible(false);

        // Progress Bar Initialize
        this.progressBar.setVisible(false);

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

        // Create a "cancel" button
        this.cancelButton = new Button("Cancel");
        this.cancelButton.setId("cancelButton");
        this.cancelButton.setPrefHeight(buttonPreferredHeight);
        this.cancelButton.setDisable(true);

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
            this.cancelButton.setDisable(false);
            this.progressLabel.setVisible(true);
            this.progressBar.setVisible(true);
            this.progressBar.setProgress(0);
          
            Runnable myJob = () -> {
                executeJob(imgTransformList.getSelectionModel().getSelectedItem());
            };
            Thread thread1 = new Thread(myJob);
            thread1.start();

            Runnable progressTracker = () -> {
                progress();
            };
            Thread thread2 = new Thread(progressTracker);
            thread2.start();
            // this.closeButton.setDisable(false); will be implemented via a listener 
        });

        this.closeButton.setOnAction(f -> this.close());
        
        this.cancelButton.setOnAction(f -> this.cancel());

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
        row2.getChildren().add(progressLabel);
        row2.getChildren().add(progressBar);
        layout.getChildren().add(row2);

        layout.getChildren().add(flwvp);

        HBox row3 = new HBox(5);
        row3.getChildren().add(runButton);
        row3.getChildren().add(closeButton);
        row3.getChildren().add(cancelButton);
        layout.getChildren().add(row3);

        HBox row4 = new HBox(5);
        row4.getChildren().add(readTimeLabel);
        row4.getChildren().add(jobReadValue);
        row4.getChildren().add(processTimeLabel);
        row4.getChildren().add(jobProcessValue);
        row4.getChildren().add(writeTimeLabel);
        row4.getChildren().add(jobWriteValue);
        row4.getChildren().add(totalTimeLabel);
        row4.getChildren().add(jobTotalValue);

        layout.getChildren().add(row4);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();

    }

    private void progress() {
        double filesDone = 0;
        double totalFiles = inputFiles.size();
        double percentage = 0;
        while(this.jobDone == false) {
            if (getTasksDone() != filesDone) {
                percentage = (filesDone + 1)/totalFiles;
                this.progressBar.setProgress(percentage);
                filesDone++;
            }
        }
    };

    public synchronized void updateTasksDone() {
        this.tasksDone = this.tasksDone + 1;
    } 

    private double getTasksDone() {
        return this.tasksDone;
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
     * Method to add a listener for the cancel button
     *
     * @param listener The listener method
     */
    public void cancel() {

        lock.lock();
        try {
            this.shouldCancel = true; 
            this.runButton.setDisable(true);
            this.workerTask.cancel(true);
            this.progressBar.progressProperty().unbind();
            this.progressBar.setProgress(0);
        }
        catch(Exception batcherror) {
            System.out.println(batcherror);
        }
        finally {
            lock.unlock(); 
        }
    }


    /**
     * Method to add a listener for the cancel button
     *
     * @param listener The listener method
     */
    public boolean isCancelled() {
        return this.shouldCancel;
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

    /**
     * A method to add times
     *
     * @param givenJob The job whose time is being calculated for
     */
    public void updateTimes(WorkUnit work) {
        if (this.shouldCancel == false) {
          this.readTime += work.readTime;
          String readText = Double.toString(this.readTime/1000000000);
          this.jobReadValue.setText(readText + "s");

          this.processTime += this.processTime;
          String processText = Double.toString(this.processTime/1000000000);
          this.jobProcessValue.setText(processText + "s");

          this.writeTime += work.writeTime; 
          String writeText = Double.toString(this.writeTime/1000000000);
          this.jobWriteValue.setText(writeText + "s");

          this.totalTime += work.totalTime;
          String totalText = Double.toString(this.totalTime/1000000000);
          this.jobTotalValue.setText(totalText + "s");

          this.mw.increaseExecutedJobs();
          if(this.mw.sw == null){}
          else { this.mw.sw.windowUpdateJobsExecuted();}
        } else {
          this.jobReadValue.setText("CANCELED");
          this.jobProcessValue.setText("CANCELED");
          this.jobWriteValue.setText("CANCELED");
          this.jobTotalValue.setText("CANCELED");
        }

    }

    /**
     * A method used after the job is finished to enable the close button and
     * show the time values
     *
     */
    public void jobCompleted() {
        this.closeButton.setDisable(false);
        this.readTimeLabel.setVisible(true);
        this.jobReadValue.setVisible(true);
        this.processTimeLabel.setVisible(true);
        this.jobProcessValue.setVisible(true);
        this.writeTimeLabel.setVisible(true);
        this.jobWriteValue.setVisible(true);
        this.totalTimeLabel.setVisible(true);
        this.jobTotalValue.setVisible(true);
        this.cancelButton.setVisible(false);
        this.cancelButton.setDisable(true);
        this.progressLabel.setVisible(false);
        this.progressBar.setVisible(false);
        jobDone = true;
    }

    /**
     * A method to execute the job
     *
     * @param imgTransform The imgTransform to apply to input images
     */
    private void executeJob(ImgTransform imgTransform) {

        // Clear the display
        this.flwvp.clear();

        for (Path inputFile : this.inputFiles){
            WorkUnit work = new WorkUnit(this, imgTransform, this.targetDir, inputFile);
            mw.addWork(work);
        }

        // Add a poisoned work to signal that this job window should be completed. 
        WorkUnit work = new WorkUnit(this, null, null, null);
        work.poisoned = true;
        mw.addWork(work);
        

        // // Create a job
        // Job job = new Job(imgTransform, this.targetDir, this.inputFiles);

        // // Execute it
        // job.execute(this, this.mw);

        // close the window 
        // this.closeButton.setDisable(false);

        // this.cancelButton.setDisable(true);
    }

    /**
     * A listener that updates job window when a job finishes
     *
     * @param jobOutcome The outcome of a job 
     */
    public void displayJob(Job.ImgTransformOutcome imageOutcome){

        // Process the outcome
        List<Path> toAddToDisplay = new ArrayList<>();

        StringBuilder errorMessage = new StringBuilder();

        if (imageOutcome.success) {
            toAddToDisplay.add(imageOutcome.outputFile);
        }else if(!imageOutcome.success && imageOutcome.error == null){
            errorMessage.append(imageOutcome.inputFile.toAbsolutePath().toString()).append(": ").append("Cancelled").append("\n");
        }else {
            errorMessage.append(imageOutcome.inputFile.toAbsolutePath().toString()).append(": ").append(imageOutcome.error.getMessage()).append("\n");
        }

        // Update the viewport
        this.flwvp.addFiles(toAddToDisplay);

    }

}
