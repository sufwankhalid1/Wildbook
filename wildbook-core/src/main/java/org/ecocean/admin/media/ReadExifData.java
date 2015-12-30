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
        ImageMeta meta;
        try {
            meta = FileUtilities.getImageMetaData(file);
        } catch (Exception ex) {
            logger.error("Can't read metadata for [" + file + "]", ex);
            return;
        }
        if (meta == null) {
            return;
        }

        System.out.println(meta);
    }

    public static void main(final String args[])
    {
        launch(new ReadExifData(), args);
    }
}
