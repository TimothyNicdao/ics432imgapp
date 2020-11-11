package ics432.imgapp;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.image.Image;

class WorkUnit{

    public Path targetDir;
    public double readTime = 0;
    public double processTime = 0;
    public double writeTime = 0;
    public double totalTime = 0;
    public Path   inputFile = null;
    public Image  image = null;
    public boolean poisoned = false; 
    public BufferedImage bufferedImage = null; 
    public ImgTransform imgTransform;
    public JobWindow jw;

    WorkUnit(JobWindow jw, ImgTransform imgTransform, Path targetDir, Path inputFile){
        this.jw = jw; 
        this.imgTransform = imgTransform;
        this.targetDir = targetDir;
        this.inputFile = inputFile; 
    }


}