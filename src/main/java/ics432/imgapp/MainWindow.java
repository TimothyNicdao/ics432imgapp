package ics432.imgapp;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements the "Main Window" for the app, which
 * allows the user to add input files for potential filtering and
 * to create an image filtering job
 */
class MainWindow {

    private final Stage primaryStage;
    private final Button quitButton;
    private final Button showStatsButton;
    private int pendingJobCount = 0;
    private volatile int jobsExecuted = 0;
    private volatile int imagesProcessed = 0;
    private volatile ArrayList<Double> computeSpeedInvertArr = new ArrayList<Double>();
    private volatile Double computeSpeedInvert = 0.0;
    private volatile ArrayList<Double> computeSpeedOilArr = new ArrayList<Double>();
    private volatile Double computeSpeedOil = 0.0;
    private volatile ArrayList<Double> computeSpeedSolarizeArr = new ArrayList<Double>();
    private volatile Double computeSpeedSolarize = 0.0;
    private final FileListWithViewPort fileListWithViewPort;
    private int jobID = 0;
    private Double updatedValue;
    public StatisticsWindow sw;
    public CheckBox multithreadCheckBox;

    /**
     * Constructor
     *
     * @param primaryStage The primary stage
     */
    MainWindow(Stage primaryStage, int windowWidth, int windowHeight) {

        double buttonPreferredHeight = 27.0;

        // Set up the primaryStage
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ICS 432 Image Editing App");

        // Make this primaryStage non closable
        this.primaryStage.setOnCloseRequest(Event::consume);

        // Create all widgets
        Button addFilesButton = new Button("Add Image Files");
        addFilesButton.setPrefHeight(buttonPreferredHeight);
        addFilesButton.setId("addFilesButton"); // for TestFX

        showStatsButton = new Button("Show Statistics");
        addFilesButton.setPrefHeight(buttonPreferredHeight);
        addFilesButton.setId("showStatsButton"); 

        Button createJobButton = new Button("Create Job");
        createJobButton.setPrefHeight(buttonPreferredHeight);
        createJobButton.setDisable(true);
        createJobButton.setId("createJobButton"); // for TestFX

        quitButton = new Button("Quit");
        quitButton.setId("quitButton"); // for TestFX
        quitButton.setPrefHeight(buttonPreferredHeight);

        this.multithreadCheckBox = new CheckBox("Multithreading");
        this.multithreadCheckBox.setPrefHeight(buttonPreferredHeight);

        this.fileListWithViewPort = new FileListWithViewPort(
                windowWidth  * 0.98,
                windowHeight - 3 * buttonPreferredHeight - 3 * 5,
                true);

        // Listen for the "nothing is selected" property of the widget
        // to disable the createJobButton dynamically
        this.fileListWithViewPort.addNoSelectionListener(createJobButton::setDisable);

        // Set actions for all widgets
        addFilesButton.setOnAction(e -> addFiles(selectFilesWithChooser()));

        showStatsButton.setOnAction(e -> {
            this.showStatsButton.setDisable(true);
            StatisticsWindow sw = new StatisticsWindow(
                (int) (windowWidth * 0.8), (int) (windowHeight * 0.8),
                this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                this.primaryStage.getY() + 50 + this.pendingJobCount * +10, this);

            this.sw = sw;
                sw.addCloseListener(() -> {
                    this.showStatsButton.setDisable(false);
                });
        });

        quitButton.setOnAction(e -> {
            // If the button is enabled, it's fine to quit
            this.primaryStage.close();

        });

        createJobButton.setOnAction(e -> {
            this.quitButton.setDisable(true);
            this.pendingJobCount += 1;
            this.jobID += 1;
            JobWindow jw = new JobWindow(
                (int) (windowWidth * 0.8), (int) (windowHeight * 0.8),
                this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                this.primaryStage.getY() + 50 + this.pendingJobCount * +10,
                this.jobID, new  ArrayList<>(this.fileListWithViewPort.getSelection()), this);
                
                jw.addCloseListener(() -> {
                    
                    this.pendingJobCount -= 1;
                    if (this.pendingJobCount == 0) {
                        this.quitButton.setDisable(false);
                    }
                });
                
        });

        //Construct the layout
        VBox layout = new VBox(5);

        layout.getChildren().add(addFilesButton);
        layout.getChildren().add(multithreadCheckBox);
        layout.getChildren().add(this.fileListWithViewPort);

        HBox row = new HBox(5);
        row.getChildren().add(createJobButton);
        row.getChildren().add(showStatsButton);
        row.getChildren().add(quitButton);
        layout.getChildren().add(row);

        Scene scene = new Scene(layout, windowWidth, windowHeight);
        this.primaryStage.setScene(scene);
        this.primaryStage.setResizable(false);

        // Make this primaryStage non closable
        this.primaryStage.setOnCloseRequest(Event::consume);

        //  Show it on  screen.
        this.primaryStage.show();
    }


    /**
     * Method that pops up a file chooser and returns chosen image files
     *
     * @return The list of files
     */
    private List<Path> selectFilesWithChooser() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image Files");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Jpeg Image Files", "*.jpg", "*.jpeg", "*.JPG", "*.JPEG"));
        List<File>  selectedFiles = fileChooser.showOpenMultipleDialog(this.primaryStage);

        if (selectedFiles == null) {
            return new ArrayList<>();
        } else {
            return selectedFiles.stream().collect(ArrayList::new,
                    (c, e) -> c.add(Paths.get(e.getAbsolutePath())),
                    ArrayList::addAll);
        }
    }

    /**
     * Method that adds files to the list of known files
     *
     * @param files The list of files
     */
    private void addFiles(List<Path> files) {

        if (files != null) {
            this.fileListWithViewPort.addFiles(files);
        }
    }

    /**
     * Method to update executed jobs number
     *
     * @param files The list of files
     */
    public synchronized void increaseExecutedJobs() {
        this.jobsExecuted++;
    }

    /**
     * Method to update executed jobs number
     *
     * @param files The list of files
     */
    public synchronized void increaseImagesProcessed() {
        imagesProcessed++;
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public long getJobsExecuted() { 
        return this.jobsExecuted; 
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public long getImagesProcessed() { 
        return this.imagesProcessed; 
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public Double getComputeSpeedInvert() { 
        return this.computeSpeedInvert; 
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public void updateInvert(Double value) {
       this.computeSpeedInvertArr.add(value);
       this.updatedValue = 0.0;
       for (int i = 0; i < this.computeSpeedInvertArr.size(); i++) {
           this.updatedValue = this.updatedValue + this.computeSpeedInvertArr.get(i);
       }
       this.computeSpeedInvert = this.updatedValue/this.computeSpeedInvertArr.size();
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public Double getComputeSpeedOil() { 
        return this.computeSpeedOil; 
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public void updateOil(Double value) {
        this.computeSpeedOilArr.add(value);
        this.updatedValue = 0.0;
        this.computeSpeedOilArr.forEach((item) -> this.updatedValue += item);
        this.computeSpeedOil = this.updatedValue/this.computeSpeedOilArr.size();
     }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public Double getComputeSpeedSolarize() { 
        return this.computeSpeedSolarize; 
    }

    /**
     * Getter for processTime
     *
     * @return The process time of the job
     */
    public void updateSolarize(Double value) {
        this.computeSpeedSolarizeArr.add(value);
        this.updatedValue = 0.0;
        this.computeSpeedSolarizeArr.forEach((item) -> this.updatedValue += item);
        this.computeSpeedSolarize = this.updatedValue/this.computeSpeedSolarizeArr.size();
     }

    /**
     * Check if multithreaded
     *
     * @return The process time of the job
     */
    public boolean checkMultithread() {
        return this.multithreadCheckBox.isSelected();
     }

}
