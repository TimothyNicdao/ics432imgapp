package ics432.imgapp;

import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;
import javafx.application.Application;
import javafx.stage.Stage;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;

/**
 * Top-level class
 */
public class ICS432ImgApp extends Application {

    /**
     * The available filters
     */
    public static ArrayList<ImgTransform> filters;


    /**
     * The execution statistics
     */
    public static Statistics statistics;

    // Static Initialization code
    static {

        // Filters
        ImgTransform invertFilter = new ImgTransform("Invert", new InvertFilter());
        ImgTransform solarizeFilter = new ImgTransform("Solarize", new SolarizeFilter());
        ImgTransform medianFilter = new ImgTransform("Median" , new MedianFilter());
        OilFilter of = new OilFilter();
        of.setRange(4);
        ImgTransform oil4Filter = new ImgTransform("Oil4", of);
        filters = new ArrayList<>();
        filters.add(invertFilter);
        filters.add(solarizeFilter);
        filters.add(oil4Filter);
        filters.add(medianFilter);

        // Statistics
        statistics = new Statistics();
    }

    /**
     * start() Javafx Method
     *
     * @param primaryStage  The primary stage
     */
    @Override
    public void start(Stage primaryStage) {
        // Pop up the main window
        new MainWindow(primaryStage, 1500, 750);


    }

    public static double getPeakRAM() {

        double memory_usage = 0.0;
        try {
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                memory_usage += pool.getPeakUsage().getUsed();
            }
            // we print the result in the console
//            System.out.println(memory_usage / (1000 * 1000) + "MB");

        } catch (Throwable t) {
            System.err.println("Exception in agent: " + t);
        }
        return memory_usage / (1000  * 1000);
    }

}
