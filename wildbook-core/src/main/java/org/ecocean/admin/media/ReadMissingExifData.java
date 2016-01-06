package org.ecocean.admin.media;

import java.nio.file.Path;

import org.ecocean.Global;
import org.ecocean.media.AssetStore;
import org.ecocean.media.ImageMeta;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.util.FileUtilities;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.Table;
import com.samsix.util.UtilException;
import com.samsix.util.app.AbstractApplication;

public class ReadMissingExifData extends AbstractApplication {
    private int storeid;
    private long total;
    private long count = 0;
    private boolean all;

    @Override
    protected void addOptions() {
        super.addOptions();

        addRequiredOption("s", "storeid", "id of local asset store to use");
        addFlagOption("all", "all", "Re read exif data for all images, not just missing ones");
    }


    @Override
    protected void checkOptions() {
        super.checkOptions();

        storeid = Integer.parseInt(getOptionValue("s"));
        all = hasOption("all");
    }

    @Override
    public void run() throws UtilException {
        super.run();

        Global.INST.init(null, null);

        //
        // TODO: Deal with different thumb and mid asset stores?
        // For now we are storing the mid in the thumb asset store which is
        // assumed to be the main asset store. Weird yes. Just trying to get this
        // working.
        //
        AssetStore store = AssetStore.get(storeid);

        try (Database db = Global.INST.getDb()) {
            String criteria = "type = 1 and store = " + storeid;
            if (! all) {
                criteria += " and metatimestamp is null and metalat is null and metalong is null";
            }

            Table table = db.getTable(MediaAssetFactory.TABLENAME_MEDIAASSET);
            total = table.getCount(criteria);

            table.select((rs) -> {
                MediaAsset ma = MediaAssetFactory.valueOf(rs);

                reportProgress(ma);

                final Path file = store.getFullPath(ma.getPath());
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

                SqlUpdateFormatter formatter = new SqlUpdateFormatter();
                formatter.append("metatimestamp", meta.getTimestamp());
                formatter.append("metalat", meta.getLatitude());
                formatter.append("metalong", meta.getLongitude());

                table.updateRow(formatter.getUpdateClause(), "id = " + ma.getID());
            }, criteria);

            System.out.println("\nFinished!\n");
            exit();
        } catch (DatabaseException ex) {
            throw new UtilException("Trouble reading exif data.", ex);
        }
    }

    private void reportProgress(final MediaAsset ma) {
        System.out.print("\rProcessing " + ++count + " of " + total + " (id:" + ma.getID() + ")...........");
    }

    public static void main(final String args[])
    {
        launch(new ReadMissingExifData(), args);
    }
}
