package ics432.imgapp;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * A class that implements a "Job Window" on which a user
 * can launch a Job
 */

class StatisticsWindow extends Stage {

    private final Button closeButton;
    private final Button cancelButton;
    private Label jobsExecutedValue = new Label("");
    private Label imagesProcessedValue = new Label("");
    private Label computeSpeedInvertValue = new Label("");
    private Label computeSpeedOilValue = new Label("");
    private Label computeSpeedSolarizeValue = new Label("");
    private MainWindow mw;

    /**
     * Constructor
     *
     * @param windowWidth      The window's width
     * @param windowHeight     The window's height
     * @param X          The horizontal position of the statistics window
     * @param Y          The vertical position of the statistics window
     * @param mainWindow        The main window
     */
    StatisticsWindow(int windowWidth, int windowHeight, double X, double Y, MainWindow mainWindow) {

        this.mw = mainWindow;

        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // Set up the window
        this.setX(X);
        this.setY(Y);
        this.setTitle("Statistics Window");
        this.setResizable(false);

        // Create all sub-widgets in the window
        Label targetDirLabel = new Label("Target Directory:");
        targetDirLabel.setPrefWidth(115);


        Label jobsExecutedLabel = new Label("Jobs Executed: ");
        jobsExecutedLabel.setPrefWidth(85);
        String jobsExecuted = Long.toString(this.mw.getJobsExecuted());
        this.jobsExecutedValue.setText(jobsExecuted);

        Label imagesProcessedLabel = new Label("Images Processed: ");
        imagesProcessedLabel.setPrefWidth(120);
        String imagesProcessed = Long.toString(this.mw.getImagesProcessed());
        this.imagesProcessedValue.setText(imagesProcessed);

        Label computeSpeedInvertLabel = new Label("Invert Filter Compute Speed: ");
        computeSpeedInvertLabel.setPrefWidth(170);
        String computeSpeedInvert = Double.toString(this.mw.getComputeSpeedInvert());
        this.computeSpeedInvertValue.setText(computeSpeedInvert + "Mb/ms");

        Label computeSpeedOilLabel = new Label("Oil Filter Compute Speed: ");
        computeSpeedOilLabel.setPrefWidth(160);
        String computeSpeedOil = Double.toString(this.mw.getComputeSpeedOil());
        this.computeSpeedOilValue.setText(computeSpeedOil + "Mb/ms");

        Label computeSpeedSolarizeLabel = new Label("Solarize Filter Compute Speed: ");
        computeSpeedSolarizeLabel.setPrefWidth(180);
        String computeSpeedSolarize = Double.toString(this.mw.getComputeSpeedSolarize());
        this.computeSpeedSolarizeValue.setText(computeSpeedSolarize + "Mb/ms");

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);

        // Create a "cancel" button
        this.cancelButton = new Button("Cancel");
        this.cancelButton.setId("cancelButton");
        this.cancelButton.setPrefHeight(buttonPreferredHeight);
        this.cancelButton.setDisable(true);


        this.closeButton.setOnAction(f -> this.close());

        // Build the scene
        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.getChildren().add(jobsExecutedLabel);
        row1.getChildren().add(jobsExecutedValue);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.getChildren().add(imagesProcessedLabel);
        row2.getChildren().add(imagesProcessedValue);
        layout.getChildren().add(row2);

        HBox row3 = new HBox(5);
        row3.getChildren().add(computeSpeedInvertLabel);
        row3.getChildren().add(computeSpeedInvertValue);
        layout.getChildren().add(row3);

        HBox row4 = new HBox(5);
        row4.getChildren().add(computeSpeedOilLabel);
        row4.getChildren().add(computeSpeedOilValue);
        layout.getChildren().add(row4);

        HBox row5 = new HBox(5);
        row5.getChildren().add(computeSpeedSolarizeLabel);
        row5.getChildren().add(computeSpeedSolarizeValue);
        layout.getChildren().add(row5);

        HBox row6 = new HBox(5);
        row6.getChildren().add(closeButton);
        row6.getChildren().add(cancelButton);
        layout.getChildren().add(row6);

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

}
