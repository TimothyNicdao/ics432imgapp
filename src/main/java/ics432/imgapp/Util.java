package ics432.imgapp;

import javafx.scene.image.Image;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A helper class that implements static helper methods
 */
class Util {

    /**
     * Helper method to load an image from a resource file (with a URL)
     *
     * @param srcSubDir The directory in the src directory
     * @param filename The resource file name
     *
     * @return an image
     */
    static Image loadImageFromResourceFile(String srcSubDir, String filename) {

        Image image = null;
        Path path = Paths.get("src", srcSubDir, "resources", filename).toAbsolutePath();
        return loadImageFromPath(path);

    }

    // /**
    //  * Helper method to load an image from a /tmp/*.jpg file
    //  *
    //  * @param filename The resource file name
    //  *
    //  * @return an image
    //  */
    // static Image loadImageFromTmp(String filename) {

    //     Image image = null;
    //     Path path = Paths.get("/tmp/", filename).toAbsolutePath();
    //     return loadImageFromPath(path);
    // }
        /**
     * Helper method to load an image from a jpg file
     *
     * @param dirPath  the directory absolute path
     * @param filename The resource file name
     *
     * @return an image
     */
    static Image loadImageFromDir(String dirPath, String filename) {
        Path path = Paths.get(dirPath, filename).toAbsolutePath();
        return loadImageFromPath(path);
    }

    /**
     * Helper method to load an image from an absolute path
     *
     * @param path The path
     *
     * @return an image or null if there is an error
     */
    static Image loadImageFromPath(Path path) {

        Image image = null;
        try {
            image = new Image(path.toUri().toURL().toString());
        } catch (MalformedURLException e) {
            return null;
        }
        if (image.isError()) {
            return null;
        }
        return image;
    }

    /**
     *  Helper class
     */
    public static class PairOfStrings {
        public String first, second;
        PairOfStrings(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}
