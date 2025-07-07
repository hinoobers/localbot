package org.hinoob.localbot.util;

import java.io.File;

public class GeoguessPicture {

    private File file;
    private String country;
    private boolean needsChange = false;

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

    public void reset() {
        GeoguessPicture previous = this;
        GeoguessPicture newPicture = getRandom(previous);
        if (newPicture != null) {
            this.file = newPicture.getFile();
            this.country = newPicture.getCountry();
            this.needsChange = true;
        }
    }

    public boolean needsChange() {
        return needsChange;
    }

    public void setNeedsChange(boolean needsChange) {
        this.needsChange = needsChange;
    }

    public static GeoguessPicture getRandom(GeoguessPicture previous) {
        File[] files = new File("geopics").listFiles();
        if (files == null || files.length == 0) {
            return null; // No pictures available
        }

        File randomFile;
        do {
            int randomIndex = (int) (Math.random() * files.length);
            randomFile = files[randomIndex];
        } while (previous != null && previous.getFile().equals(randomFile));

        String country = randomFile.getName().substring(0, randomFile.getName().lastIndexOf('.')).split("_")[0];
        return new GeoguessPicture(randomFile, country);
    }
}
