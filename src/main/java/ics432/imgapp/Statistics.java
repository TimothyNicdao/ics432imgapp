package ics432.imgapp;

import javafx.application.Platform;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.ProgressBar;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class Statistics extends Stage{

    //scene setup
    private Label jobsExecutedLabel = new Label("Jobs Executed: ");
    private Label jobsExecutedValue = new Label("");
   
    private Label imagesProcessedLabel = new Label("Total images processed: ");
    private Label imagesProcessedValue = new Label("");
  
    private Label solarizedLabel = new Label("Average processing speed for the solarize filter: ");
    private Label solarizedSpeedValue = new Label("");
    
    private Label invertLabel = new Label("Average processing speed for the invert filter: ");
    private Label invertSpeedValue = new Label("");

    private Label oil4Label = new Label("Average processing speed for the oil filter: ");
    private Label oil4SpeedValue = new Label("");


    // data setup
    private int jobsExecuted;
    private int imagesProcessed;

    private double speedPerSecondInvert;
    private double invertTotalSize;
    private double invertTotalTime;

    private double speedPerSecondSolarize;
    private double solarizeTotalSize;
    private double solarizeTotalTime;

    private double speedPerSecondOil4;
    private double oil4TotalSize;
    private double oil4TotalTime;
    
    Statistics(int windowWidth, int windowHeight, double X, double Y){
        this.jobsExecuted = 0;
        this.imagesProcessed = 0; 
        
        this.speedPerSecondInvert = 0;
        this.invertTotalSize = 0;
        this.invertTotalTime = 0;

        this.speedPerSecondSolarize = 0;
        this.solarizeTotalSize = 0;
        this.solarizeTotalTime = 0;

        this.speedPerSecondOil4 = 0;
        this.oil4TotalSize = 0;
        this.oil4TotalTime = 0;



        // Initializing window parameters 
        this.setX(X);
        this.setY(Y);
        this.setTitle("Statistics");
        this.setResizable(true);


        // Build the scene
        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().add(jobsExecutedLabel);
        row1.getChildren().add(jobsExecutedValue);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().add(imagesProcessedLabel);
        row2.getChildren().add(imagesProcessedValue);
        layout.getChildren().add(row2);

        HBox row3 = new HBox(5);
        row3.setAlignment(Pos.CENTER_LEFT);
        row3.getChildren().add(solarizedLabel);
        row3.getChildren().add(solarizedSpeedValue);
        layout.getChildren().add(row3);

        HBox row4 = new HBox(5);
        row4.setAlignment(Pos.CENTER_LEFT);
        row4.getChildren().add(invertLabel);
        row4.getChildren().add(invertSpeedValue);
        layout.getChildren().add(row4);

        HBox row5 = new HBox(5);
        row5.setAlignment(Pos.CENTER_LEFT);
        row5.getChildren().add(oil4Label);
        row5.getChildren().add(oil4SpeedValue);
        layout.getChildren().add(row5);


        Scene scene = new Scene(layout, 400, 400 );

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
    }


    /**
     * A function that can update the number of jobs done for the statistic 
     *
     * @param jobsExecuted  The number of jobsDone
     */
    public synchronized void updateJobsExecuted(){
        
        this.jobsExecuted++;
        Platform.runLater(() -> this.jobsExecutedValue.textProperty().bind(new SimpleIntegerProperty(this.jobsExecuted).asString()));
    }


    /**
     * A function that can update the number of images for the statistic 
     *
     * @param jobsExecuted  The number of jobsDone
     */
    public synchronized void updateImagesProcessed(){
        this.imagesProcessed++;
        Platform.runLater(() -> this.imagesProcessedValue.textProperty().bind(new SimpleIntegerProperty(this.imagesProcessed).asString()));
    }

    
    
    /**
     * A function that can update the number of images for the statistic 
     *
     * @param size The input size to be added to the total
     */
    public synchronized void updateFilterSpeeds(double size, String filter, double time){
       
        String speedString = "";
        switch(filter){

            case "Invert":
                invertTotalSize += size; 
                invertTotalTime += time;
                speedPerSecondInvert = (invertTotalSize/invertTotalTime);
                speedString = String.valueOf(speedPerSecondInvert).concat(" Mb/s");
                StringProperty finalSpeedString = new SimpleStringProperty(speedString);
                Platform.runLater(() -> this.invertSpeedValue.textProperty().bind(finalSpeedString));
                break; 

            case "Solarize":
                solarizeTotalSize += size; 
                solarizeTotalTime += time;
                speedPerSecondSolarize = (solarizeTotalSize/solarizeTotalTime);
                speedString = String.valueOf(speedPerSecondSolarize).concat(" Mb/s");
                StringProperty finalSpeedString2 = new SimpleStringProperty(speedString);
                Platform.runLater(() -> this.solarizedSpeedValue.textProperty().bind(finalSpeedString2));
                break; 

            case "Oil4":
                oil4TotalSize += size; 
                oil4TotalTime += time;
                speedPerSecondOil4 = (oil4TotalSize/oil4TotalTime);
                speedString = String.valueOf(speedPerSecondOil4).concat(" Mb/s");
                StringProperty finalSpeedString3 = new SimpleStringProperty(speedString);
                Platform.runLater(() -> this.oil4SpeedValue.textProperty().bind(finalSpeedString3));
                break; 

        };
     
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