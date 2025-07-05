package org.hinoob.localbot.util;

import java.io.File;

public class GeoguessPicture {

    private File file;
    private String country;

    public GeoguessPicture(File file, String country) {
        this.file = file;
        this.country = country;
    }

    public File getFile() {
        return file;
    }

    public String getCountry() {
        return country;
    }

    public static GeoguessPicture getRandom() {
        File folder = new File("geopics");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (files == null || files.length == 0) {
            return null; // No pictures found
        }

        int randomIndex = (int) (Math.random() * files.length);
        File randomFile = files[randomIndex];
        String country = randomFile.getName().substring(0, randomFile.getName().lastIndexOf('.')).split("_")[0];
        return new GeoguessPicture(randomFile, country);
    }
}
