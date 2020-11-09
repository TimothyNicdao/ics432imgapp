package ics432.imgapp;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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
    private int pendingJobCount = 0;
    private final FileListWithViewPort fileListWithViewPort;
    private StatisticsWindow statisticsWindow = null;
    private int jobID = 0;
    private CheckBox multithreadingCheckBox = null;
    private Slider memorySlider;




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

        Button viewStatsButton = new Button("View Stats");
        viewStatsButton.setPrefHeight(buttonPreferredHeight);
        viewStatsButton.setId("viewStatsButton"); // for TestFX

        multithreadingCheckBox = new CheckBox("Multithreading");
        multithreadingCheckBox.setSelected(true);

        this.memorySlider = new Slider(2, 40, 2);
        this.memorySlider.setPrefWidth(350);
        this.memorySlider.setBlockIncrement(2);
        this.memorySlider.setMajorTickUnit(2);
        this.memorySlider.setMinorTickCount(0);
        this.memorySlider.setShowTickLabels(true);
        this.memorySlider.setSnapToTicks(true);

        Button createJobButton = new Button("Create Job");
        createJobButton.setPrefHeight(buttonPreferredHeight);
        createJobButton.setDisable(true);
        createJobButton.setId("createJobButton"); // for TestFX

        quitButton = new Button("Quit");
        quitButton.setId("quitButton"); // for TestFX
        quitButton.setPrefHeight(buttonPreferredHeight);

        this.fileListWithViewPort = new FileListWithViewPort(
                windowWidth  * 0.98,
                windowHeight - 3 * buttonPreferredHeight,
                true);

        // Listen for the "is empty" property of the widget
        // to disable the createJobButton dynamically
        this.fileListWithViewPort.addEmptinessListener(createJobButton::setDisable);

        // Set actions for all widgets
        addFilesButton.setOnAction(e -> addFiles(selectFilesWithChooser()));

        viewStatsButton.setOnAction(e -> {

            viewStatsButton.setDisable(true);
            this.statisticsWindow = new StatisticsWindow(
                    350, 125,
                    this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                    this.primaryStage.getY() + 30 + this.pendingJobCount * 10);

            this.statisticsWindow.addCloseListener(() -> {
                viewStatsButton.setDisable(false);
            });
        });

        quitButton.setOnAction(e -> {
            // If the button is enabled, it's fine to quit

            if (this.statisticsWindow != null)  {
                this.statisticsWindow.close();
            }
            this.primaryStage.close();
        });

        createJobButton.setOnAction(e -> {
            this.quitButton.setDisable(true);
            this.pendingJobCount += 1;
            this.jobID += 1;
            JobWindow jw = new JobWindow(
                    (int) (windowWidth * 0.8), (int) (windowHeight * 0.8),
                    this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                    this.primaryStage.getY() + 50 + this.pendingJobCount * 10,
                    this.jobID,
                    new  ArrayList<>(this.fileListWithViewPort.getSelection()),
                    this.multithreadingCheckBox.isSelected(),
                    16);

            jw.addCloseListener(() -> {
                this.pendingJobCount -= 1;
                if (pendingJobCount == 0) {
                    this.quitButton.setDisable(false);
                }
            });
        });

        //Construct the layout
        VBox layout = new VBox(5);

        HBox toprow = new HBox(10 );
        toprow.setFillHeight(true);          // Added this
        toprow.setAlignment(Pos.CENTER_LEFT);

        toprow.getChildren().add(addFilesButton);
        toprow.getChildren().add(viewStatsButton);
        layout.getChildren().add(toprow);

        layout.getChildren().add(this.fileListWithViewPort);

        HBox row = new HBox(10);
        row.getChildren().add(createJobButton);
        row.getChildren().add(quitButton);
        layout.getChildren().add(row);

        Scene scene = new Scene(layout, windowWidth, windowHeight);
        this.primaryStage.setScene(scene);
        this.primaryStage.setResizable(false);

        // Make this primaryStage non closable
        this.primaryStage.setOnCloseRequest(Event::consume);

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
        List<File>  selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);

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

}
