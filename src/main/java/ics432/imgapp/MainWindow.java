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

import java.io.IOException;
import java.io.File;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import ics432.imgapp.Job.ImgTransformOutcome;

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
    private Slider procThreadSlider;
    private Slider DPThreadSlider;
    protected ArrayBlockingQueue<WorkUnit> toRead;
    protected ArrayBlockingQueue<WorkUnit> toProcess;
    protected ArrayBlockingQueue<WorkUnit> toWrite;
    private ReaderThread readerThread;
    private ProcessorThread processorThread;
    private WriterThread writerThread;
    private int procThreadAmount = 1;
    public int dpThreadAmount = 1;
    private ArrayList<ProcessorThread> procThreadArrList;

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

        // Create ProdCons buffers
        this.toRead = new ArrayBlockingQueue(50);
        this.toProcess  = new ArrayBlockingQueue(16);
        this.toWrite  = new ArrayBlockingQueue(16);

        this.procThreadArrList = new ArrayList();

        // Create and start all threads
        this.readerThread = new ReaderThread(this);
        this.processorThread = new ProcessorThread(this);
        this.writerThread = new WriterThread(this);

        this.readerThread.setDaemon(true);
        this.readerThread.start();
        this.processorThread.setDaemon(true);
        this.processorThread.start();
        this.writerThread.setDaemon(true);
        this.writerThread.start();
        

        // Create all widgets
        Button addFilesButton = new Button("Add Image Files");
        addFilesButton.setPrefHeight(buttonPreferredHeight);
        addFilesButton.setId("addFilesButton"); // for TestFX

        Button viewStatsButton = new Button("View Stats");
        viewStatsButton.setPrefHeight(buttonPreferredHeight);
        viewStatsButton.setId("viewStatsButton"); // for TestFX

        multithreadingCheckBox = new CheckBox("Multithreading");
        multithreadingCheckBox.setSelected(true);


        this.procThreadSlider = new Slider(1, Runtime.getRuntime().availableProcessors(), 1);
        this.procThreadSlider.setPrefWidth(350);
        this.procThreadSlider.setBlockIncrement(1);
        this.procThreadSlider.setMajorTickUnit(1);
        this.procThreadSlider.setMinorTickCount(0);
        this.procThreadSlider.setShowTickLabels(true);
        this.procThreadSlider.setSnapToTicks(true);

        this.DPThreadSlider = new Slider(1, Runtime.getRuntime().availableProcessors(), 1);
        this.DPThreadSlider.setPrefWidth(350);
        this.DPThreadSlider.setBlockIncrement(1);
        this.DPThreadSlider.setMajorTickUnit(1);
        this.DPThreadSlider.setMinorTickCount(0);
        this.DPThreadSlider.setShowTickLabels(true);
        this.DPThreadSlider.setSnapToTicks(true);

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
            this.procThreadSlider.setDisable(true);
            this.DPThreadSlider.setDisable(true);
            this.pendingJobCount += 1;
            this.jobID += 1;
            this.dpThreadAmount = (int) this.DPThreadSlider.getValue();
            if (this.procThreadAmount != this.procThreadSlider.getValue()) {
                if (this.procThreadAmount < this.procThreadSlider.getValue()) {
                    while (this.procThreadAmount != this.procThreadSlider.getValue()) {
                        ProcessorThread newThread = new ProcessorThread(this);
                        this.procThreadArrList.add(newThread);
                        newThread.setDaemon(true);
                        newThread.start();
                        this.procThreadAmount++;
                    }
                } else {
                    while (this.procThreadAmount != this.procThreadSlider.getValue()) {
                        ProcessorThread temp = this.procThreadArrList.get(this.procThreadAmount -2);
                        temp.stopThread();
                        this.procThreadArrList.remove(this.procThreadAmount -2);
                        this.procThreadAmount--;
                    }
                }
            }
            

            JobWindow jw = new JobWindow(
                    (int) (windowWidth * 0.8), (int) (windowHeight * 0.8),
                    this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                    this.primaryStage.getY() + 50 + this.pendingJobCount * 10,
                    this.jobID,
                    new  ArrayList<>(this.fileListWithViewPort.getSelection()),
                    this.multithreadingCheckBox.isSelected(),
                    16,this);

            jw.addCloseListener(() -> {
                this.pendingJobCount -= 1;
                if (pendingJobCount == 0) {
                    this.quitButton.setDisable(false);
                    this.procThreadSlider.setDisable(false);
                    this.DPThreadSlider.setDisable(false);
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
        toprow.getChildren().add(new Label("#Processor Threads"));
        toprow.getChildren().add(procThreadSlider);
        toprow.getChildren().add(new Label("#DP Threads"));
        toprow.getChildren().add(DPThreadSlider);
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

    
    /**
     * Nested ReaderThread class
     */
    private class ReaderThread extends Thread {

        private MainWindow mw;

        public ReaderThread(MainWindow mw) {
            this.mw = mw;
        }

        @Override
        public void run() {

            while (true) {


                WorkUnit wu;

                try {
                    wu = this.mw.toRead.take();
                } catch (InterruptedException e) {
                    //  We're canceled
                    break;
                }

                if (wu.end == true) {
                    try {
                        try{
                            wu.readInputFile();
                        } catch (IOException e) {
                            
                        }
                        
                        this.mw.toProcess.put(wu);
                    } catch (InterruptedException e) {
                        //  We're canceled
                        break;
                    }

                } else {
                    try {
                        long t1 = System.currentTimeMillis();
                        wu.readInputFile();
                        long t2 = System.currentTimeMillis();
                        wu.givenJob.profiling.readTime += (t2 - t1) / 1000F;
                    } catch (IOException e) {
                        wu.givenJob.outcomes.add(wu.givenJob.new ImgTransformOutcome(false, wu.inputFile, null, e));
                        continue;
                    }

                try {
                    this.mw.toProcess.put(wu);
                } catch (InterruptedException e) {
                    // We're canceled
                    break;
                }
                }
                    
            }
        }
    }



    /**
     * Nested ProcessorThread class
     */
    private class ProcessorThread extends Thread {

        private MainWindow mw;
        private volatile boolean runThread = true;

        public ProcessorThread(MainWindow mw) {
            this.mw = mw;
        }

        public void stopThread() {
            runThread = false;
            interrupt();
        }

        @Override
        public void run() {

            while (runThread) {

                WorkUnit wu;
                try {
                    wu = this.mw.toProcess.take();
                } catch (InterruptedException e) {
                    //  We're canceled
                    break;
                }

                if (wu.end == true) {
                    try {
                        wu.processImage();
                        this.mw.toWrite.put(wu);
                    } catch (InterruptedException e) {
                        //  We're canceled
                        break;
                    }

                } else {
                    long t1 = System.currentTimeMillis();
                    wu.processImage();
                    long t2 = System.currentTimeMillis();
                    wu.givenJob.profiling.processingTime += (t2 - t1) / 1000F;

                    try {
                        this.mw.toWrite.put(wu);
                    } catch (InterruptedException e) {
                        // We're canceled
                        break;
                    }
                }
                    
            }
        }
    }


    /**
     * Nested WriterThread class
     */
    private class WriterThread extends Thread {

        private MainWindow mw;

        public WriterThread(MainWindow mw) {
            this.mw = mw;
        }

        @Override
        public void run() {

            int count = 0;
            while (true) {

                WorkUnit wu;
                try {
                    wu = this.mw.toWrite.take();
                } catch (InterruptedException e) {
                    //  We're canceled
                    break;
                }

                ImgTransformOutcome outcome = null;

                if (wu.end == true) {
                    wu.givenJob.jobWindow.jobDone = true;
                } else {
                    try {
                    long t1 = System.currentTimeMillis();
                    wu.writeImage();
                    long t2 = System.currentTimeMillis();
                    wu.givenJob.profiling.writeTime += (t2 - t1) / 1000F;
                    outcome = wu.givenJob.new ImgTransformOutcome(true, wu.inputFile, wu.outputFile, null);
                    } catch (IOException e) {
                        outcome = wu.givenJob.new ImgTransformOutcome(false, wu.inputFile, null, e);
                    }

                    wu.givenJob.outcomes.add(outcome);

                    if (wu.givenJob.jobWindow != null) {
                        double progress = 1.0 * (++count) / wu.givenJob.inputFiles.size();
                        wu.givenJob.jobWindow.updateProgress(progress);
                    }

                    if (outcome.success) {
                        ICS432ImgApp.statistics.newlyProcessedImage(wu.givenJob.imgTransform);
                    }

                    if (wu.givenJob.jobWindow != null) {
                        if (outcome.success) {
                            wu.givenJob.jobWindow.updateDisplayAfterImgProcessed(outcome.outputFile);
                        }
                    }
                }
                
            }
        }
    }
}
