package ics432.imgapp;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;

public class Statistics {

    public HashMap<String, Property> content;

    public Statistics() {
        this.content = new HashMap<>();
        this.content.put("num_completed_jobs", new SimpleIntegerProperty(0));
        this.content.put("num_processed_images", new SimpleIntegerProperty(0));
        ICS432ImgApp.filters.forEach((t) -> {
            this.content.put("filter_bytes_" + t.getName(),
                    new SimpleFloatProperty(0));
            this.content.put("filter_time_" + t.getName(),
                    new SimpleFloatProperty(0));
            this.content.put("filter_speed_" + t.getName(),
                    new SimpleFloatProperty(0));
        });

    }

    /**
     * Method to reset the statistics to zero
     */
    public void reset() {
        ((SimpleIntegerProperty)(this.content.get("num_completed_jobs"))).set(0);
        ((SimpleIntegerProperty)(this.content.get("num_processed_images"))).set(0);
        ICS432ImgApp.filters.forEach((t) -> {
            ((SimpleFloatProperty)(this.content.get("filter_bytes_" + t.getName()))).set(0.0F);
            ((SimpleFloatProperty)(this.content.get("filter_time_" + t.getName()))).set(0.0F);
            ((SimpleFloatProperty)(this.content.get("filter_speed_" + t.getName()))).set(0.0F);
        });
    }

    public synchronized void newlyCompletedJob(ImgTransform transform, float mb, float sec)  {
        SimpleIntegerProperty p1 = (SimpleIntegerProperty)this.content.get("num_completed_jobs");
        p1.set(p1.get() + 1);
        SimpleFloatProperty p2 = (SimpleFloatProperty)this.content.get("filter_bytes_" + transform.getName());
        p2.set(p2.get() + mb);
        SimpleFloatProperty p3 = (SimpleFloatProperty)this.content.get("filter_time_" + transform.getName());
        p3.set(p3.get() + sec);
        SimpleFloatProperty p4 = (SimpleFloatProperty)this.content.get("filter_speed_" + transform.getName());
        p4.set(p2.get() / p3.get());
    }

    public synchronized void newlyProcessedImage(ImgTransform transform)  {
        SimpleIntegerProperty p1 = (SimpleIntegerProperty) this.content.get("num_processed_images");
        p1.set(p1.get()+ 1);
    }

    public String toString(String pName) {
        Property p = this.content.get(pName);
        if (p instanceof SimpleIntegerProperty) {
            return Integer.toString(((SimpleIntegerProperty)p).get());
        } else if (p instanceof SimpleFloatProperty) {
            return String.format("%.2f", ((SimpleFloatProperty)p).get());
        } else {
            return null;
        }
    }
}
