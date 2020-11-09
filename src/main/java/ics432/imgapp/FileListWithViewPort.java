package ics432.imgapp;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A helper class that implements a widget that shows
 * an image file list on the left-hand side, and the actual
 * image that correspond to the selected file on the right-hand side.
 * If the widget is set to be "editable", then entries can be
 * removed by the user by using the backspace key.
 */
class FileListWithViewPort extends HBox {

    private final ObservableList<Path> availableFiles;
    private final ListView<Path> availableFilesView;
    private final ImageView iv;
    private final double height;
    private final double width;
    private final Image emptyImage;
    private final Image brokenImage;
    private final SimpleBooleanProperty isEmpty;
    private final boolean isEditable;

    /**
     * Constructor
     *
     * @param width      The widget's width
     * @param height     The widget's height
     * @param isEditable Whether the widget is editable
     */
    FileListWithViewPort(double width, double height,
                         boolean isEditable) {

        this.setSpacing(5);

        double listFraction = 0.4; // Which fraction of the width is the list (the other being the viewport)

        this.height = height;
        this.width = width;
        this.isEditable = isEditable;
        this.isEmpty = new SimpleBooleanProperty(true);  // a boolean that's "observable"

        // Get references to the empty and broken images
        this.emptyImage = Util.loadImageFromResourceFile("main","empty-image.png");
        this.brokenImage = Util.loadImageFromResourceFile("main","broken-image.png");

        // Create all widgets
        availableFiles = FXCollections.observableArrayList();
        availableFilesView = new ListView<>(availableFiles);
        availableFilesView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                Platform.runLater(() -> {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toAbsolutePath().toString());
                    }
                });
            }
        });

        availableFilesView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableFilesView.setPrefWidth(width * listFraction);
        availableFilesView.setMinWidth(width * listFraction);
        availableFilesView.setMaxWidth(width * listFraction);
        availableFilesView.setPrefHeight(height);
        availableFilesView.setMaxHeight(height);

        // Create an image viewport
        iv = new ImageView();
        iv.setPreserveRatio(true);
        displayInViewPort(emptyImage);

        // Update viewport when item is clicked
        availableFilesView.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                displayInViewPort(availableFilesView.getSelectionModel().getSelectedItem());
                this.isEmpty.setValue(availableFilesView.getSelectionModel().getSelectedItem() == null);
            });
        });

        // Update viewport when item is scrolled to or items are deleted
        availableFilesView.setOnKeyPressed(e -> {

            // If the key is BACK_SPACE and the list is editable, remove items
            if (this.isEditable && (e.getCode() == KeyCode.BACK_SPACE)) {

                int to_select_after = Math.max(0, availableFilesView.getSelectionModel().getSelectedIndices().get(0) - 1);
                availableFiles.removeAll(availableFilesView.getSelectionModel().getSelectedItems());
                if (availableFiles.size() > 0) {
                    availableFilesView.getSelectionModel().select(to_select_after);
                }
                this.isEmpty.setValue(availableFilesView.getSelectionModel().getSelectedItem() == null);
            }

            // If the key is UP, DOWN, or BACKSPACE, update the display
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN | e.getCode() == KeyCode.BACK_SPACE) {

                Platform.runLater(() -> {
                    displayInViewPort(availableFilesView.getSelectionModel().getSelectedItem());
                });
            }

        });

        // Add all widgets to the main layout
        this.getChildren().add(availableFilesView);
        this.getChildren().add(iv);
        this.setMaxHeight(height);
    }

    /**
     * Method to register a change listener for "is empty" changes.
     *
     * @param listener The listener method
     */
    public void addEmptinessListener(Consumer<Boolean> listener) {
        // Simply pass to the SimpleBooleanProperty a (more fancy)
        // listener that calls the (less fancy) user-provided listener
        this.isEmpty.addListener((observable, oldValue, newValue) -> {
            listener.accept(newValue);
        });
    }

    /**
     * Method to clear all files from the list
     */
    public void clear() {

        if (! Platform.isFxApplicationThread()) {
            Platform.runLater(this::clearFileList);
        } else {
            this.clearFileList();
        }
    }

    /**
     * Method to clear the file list (should always run in the JavaFX Application thread)
     */
    private void clearFileList() {
        this.availableFiles.clear();
        this.isEmpty.setValue(availableFiles.isEmpty());
        this.displayInViewPort((Path) null);
    }

    /**
     * Method that returns the number of files
     *
     * @return the number of files
     */
    public int getNumFiles() {
        return this.availableFiles.size();
    }

    /**
     * Method to add files to the list
     *
     * @param toAdd List of File objects
     */
    public void addFiles(List<Path> toAdd) {

        if (toAdd == null) return;

        // Add  the files to the file list
        for (Path f : toAdd) {
            // Check that the file is indeed new
            boolean add = true;
            for (Path g : availableFiles) {
                if (f.equals(g)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                availableFiles.add(f);
            }
        }

        // Update the display accordingly
        Platform.runLater(() -> {
            this.isEmpty.setValue(availableFiles.isEmpty());

        });
    }

    /**
     * Method to add one file to the list
     *
     * @param file : the file to add
     */
    public void addFile(Path file) {
        List<Path> toAdd = new ArrayList<>();
        toAdd.add(file);
        this.addFiles(toAdd);
    }

    /**
     * Retrieve the list of files selected by the user
     *
     * @return The list of selected files
     */
    public List<Path> getSelection() {
        return this.availableFilesView.getSelectionModel().getSelectedItems();
    }

    /**
     * Helper method to display and image file in the viewport
     *
     * @param file The image file to display in the viewport
     */
    private void displayInViewPort(Path file) {

        Image img = brokenImage;
        if (file != null) {
            try {
                img = new Image(file.toUri().toURL().toString());
            } catch (MalformedURLException ignore) {
            }
            if (img.isError()) {
                img = brokenImage;
            }
        } else {
            img = emptyImage;

        }
        displayInViewPort(img);
    }

    /**
     * Helper method to display an image in the viewport
     *
     * @param img The image to display in the viewport
     */
    private void displayInViewPort(Image img) {

        this.iv.setImage(null);
        this.iv.setImage(img);

        // Max out the width
        double image_ratio = img.getHeight() / img.getWidth();
        double target_width = 2  * this.width / 3;

        // Reduce it if necessary for height
        if (target_width * image_ratio > this.height) {
            target_width = this.height / image_ratio;
        }

        // Set the viewport's fit width
        iv.setFitWidth(target_width);
    }
}
