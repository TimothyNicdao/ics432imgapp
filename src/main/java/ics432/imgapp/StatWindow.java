package ics432.imgapp;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
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

class StatWindow extends Stage {

    private final Button closeButton;
    private final Label label1;
    private final Label label2;
    private final Label label3;
    private final Label label4;
    private final Label label5;
    private final Label text1;
    private final Label text2;
    private final Label text3;
    private final Label text4;
    private final Label text5;
    private final Label unit1;
    private final Label unit2;
    private final Label unit3;

    /**
     * Constructor
     *
     * @param windowWidth      The window's width
     * @param windowHeight     The window's height
     * @param X          The horizontal position of the statistics window
     * @param Y          The vertical position of the statistics window
     */
    StatWindow(int windowWidth, int windowHeight, double X, double Y) {
        // Keep track of whether the jobs were cancelled via the cancel button

        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // Set up the window
        this.setX(X);
        this.setY(Y);
        this.setTitle("Display of Statistics");
        this.setResizable(false);

        // Make this window non closable
        this.setOnCloseRequest(Event::consume);

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);
        this.closeButton.setOnAction(f -> this.close());

        this.text1 = new Label("The total # of successfully processed images: ");
        this.label1 = new Label();
        this.label1.textProperty().bind(new SimpleIntegerProperty(MainWindow.numSuc).asString());

        this.text2 = new Label("The total # of executed jobs: ");
        this.label2 = new Label();
        this.label2.textProperty().bind(new SimpleIntegerProperty(MainWindow.numExec).asString());

        this.text3 = new Label("The average time (Invert): ");
        this.label3 = new Label();
        this.label3.textProperty().bind(new SimpleDoubleProperty(MainWindow.avg_invert).asString());

        this.text4 = new Label("The average time (Solarize): ");
        this.label4 = new Label();
        this.label4.textProperty().bind(new SimpleDoubleProperty(MainWindow.avg_solarize).asString());

        this.text5 = new Label("The average time (Oil4): ");
        this.label5 = new Label();
        this.label5.textProperty().bind(new SimpleDoubleProperty(MainWindow.avg_oil4).asString());

        this.unit1 = new Label("MB/second");
        this.unit2 = new Label("MB/second");
        this.unit3 = new Label("MB/second");


        // Build the scene
        VBox layout = new VBox(10);

        HBox row1 = new HBox(5);
        row1.getChildren().add(text1);
        row1.getChildren().add(label1);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.getChildren().add(text2);
        row2.getChildren().add(label2);
        layout.getChildren().add(row2);

        HBox row3 = new HBox(5);
        row3.getChildren().add(text3);
        row3.getChildren().add(label3);
        row3.getChildren().add(unit1);
        layout.getChildren().add(row3);

        HBox row4 = new HBox(5);
        row4.getChildren().add(text4);
        row4.getChildren().add(label4);
        row4.getChildren().add(unit2);
        layout.getChildren().add(row4);

        HBox row5 = new HBox(5);
        row5.getChildren().add(text5);
        row5.getChildren().add(label5);
        row5.getChildren().add(unit3);
        layout.getChildren().add(row5);

        HBox row6 = new HBox(5);
        row6.getChildren().add(closeButton);
        layout.getChildren().add(row6);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();

    }

  public void addCloseListener(Runnable listener) {
    this.addEventHandler(WindowEvent.WINDOW_HIDDEN, (event) -> listener.run());
  }

  public synchronized void function1(){
    Platform.runLater(() -> this.label1.textProperty().bind(new SimpleIntegerProperty(MainWindow.numSuc).asString()));
  }

  public synchronized void function2(){
    Platform.runLater(() -> this.label2.textProperty().bind(new SimpleIntegerProperty(MainWindow.numExec).asString()));
  }

  public synchronized void function3(double num, String type){
      String a = "Invert";
      String b = "Solarize";
      String c = "Oil4";

      if(type.equals(a)){
        MainWindow.avg_invert = num;
        Platform.runLater(() -> this.label3.textProperty().bind(new SimpleDoubleProperty(MainWindow.avg_invert).asString()));
      }
      else if(type.equals(b)){
        MainWindow.avg_solarize = num;
        Platform.runLater(() -> this.label4.textProperty().bind(new SimpleDoubleProperty(MainWindow.avg_solarize).asString()));
      }
      else if(type.equals(c)){
        MainWindow.avg_oil4 = num;
        Platform.runLater(() -> this.label5.textProperty().bind(new SimpleDoubleProperty(MainWindow.avg_oil4).asString()));
      }
  }


}
