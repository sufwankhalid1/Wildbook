package org.ecocean.admin.media;

import java.io.File;

import org.ecocean.media.ImageMeta;
import org.ecocean.util.FileUtilities;

import com.samsix.util.UtilException;
import com.samsix.util.app.AbstractApplication;

public class ReadExifData extends AbstractApplication {
    private String filename;

    @Override
    protected void addOptions() {
        super.addOptions();

        addRequiredOption("f", "file to read exif data from");
    }


    @Override
    protected void checkOptions() {
        super.checkOptions();

        filename = getOptionValue("f");
    }

    @Override
    public void run() throws UtilException {
        super.run();


        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File [" + filename + "] does not exist!");
            exit(1);
        }

        ImageMeta meta = null;
        try {
            meta = FileUtilities.getImageMetaData(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            exit(1);
        }

        System.out.println("--");
        if (meta == null) {
            System.out.println("No metadata found.");
        } else {
            System.out.println(meta);
        }
    }

    public static void main(final String args[])
    {
        launch(new ReadExifData(), args);
    }
}
