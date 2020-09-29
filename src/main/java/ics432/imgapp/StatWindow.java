package ics432.imgapp;

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

    /**
     * Constructor
     *
     * @param windowWidth      The window's width
     * @param windowHeight     The window's height
     * @param X          The horizontal position of the job window
     * @param Y          The vertical position of the job window
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

        this.label1 = new Label("Label1");
        this.label2 = new Label("Label2");


        // Build the scene
        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.getChildren().add(label1);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.getChildren().add(label2);
        layout.getChildren().add(row2);

        HBox row3 = new HBox(5);
        row3.getChildren().add(closeButton);
        layout.getChildren().add(row3);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();

    }

  public void addCloseListener(Runnable listener) {
    this.addEventHandler(WindowEvent.WINDOW_HIDDEN, (event) -> listener.run());
  }


}
