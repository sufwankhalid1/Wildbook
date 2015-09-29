package db.migration;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.ecocean.SinglePhotoVideo;
import org.ecocean.media.MediaAssetFactory;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.Table;
import com.samsix.util.string.StringUtilities;

public class V2015_08_23_17_21_50__convert_media implements JdbcMigration {
    private String getUUID(final RecordSet rs) throws DatabaseException
    {
        return rs.getString("DATACOLLECTIONEVENTID");
    }

    private int insertMediaAsset(final Database db, final String uuid, final RecordSet rs) throws DatabaseException {
        if (uuid == null) {
            return -1;
        }

        Table tablema = db.getTable("mediaasset");
        String url = SinglePhotoVideo.getUrl("context0", rs.getString("FULLFILESYSTEMPATH"), rs.getString("FILENAME"));
        //
        // Strip off /shepherd_data_dir
        //
        if (url.toLowerCase().startsWith("/shepherd_data_dir")) {
            url = url.substring(18);
        }

        SqlInsertFormatter formatter = new SqlInsertFormatter();
        formatter.append("store", 1);
        formatter.append("path", url);
        formatter.append("thumbstore", 1);
        formatter.append("thumbpath", MediaAssetFactory.getThumbnail(url));

        int mediaid = tablema.insertSequencedRow(formatter, "id");

        insertMediaConversion(db, uuid, mediaid);

        //
        // Update the mediasubmissin_media entry with the new id.
        //
        Table tablemsm = db.getTable("mediasubmission_media");
        tablemsm.updateRow("mediaid = " + mediaid, "oldmediaid = " + StringUtilities.wrapQuotes(uuid));

        return mediaid;
    }

    public void insertMediaConversion(final Database db, final String uuid, final int mediaid) throws DatabaseException
    {
        Table tablemaspv = db.getTable("temp_spv_media");
        SqlInsertFormatter formatter;
        formatter = new SqlInsertFormatter();
        formatter.append("spvid", uuid);
        formatter.append("mediaid", mediaid);
        tablemaspv.insertRow(formatter);
    }


    @Override
    public void migrate(final Connection connection) throws Exception {
        //
        // NOTE: SinglePhotoVideo does NOT have a unique constraint on the filename so that
        // two or more SPV's can point to the same file. Our MediaSubmission code is apparently
        // causing this to happen so that we are getting duplicate medias. So rather than simply
        // looping through the all the SINGLEPHOTOVIDEO we have to do it this more complicated way
        // of doing special things with the duplicates.
        //
        try (Database db = new Database(connection)) {
            Set<String> uuids = new HashSet<String>();

            Table table = db.getTable("SINGLEPHOTOVIDEO");
            String sql = "select \"FULLFILESYSTEMPATH\", count(*) from \"SINGLEPHOTOVIDEO\" group by \"FULLFILESYSTEMPATH\" having count(*) > 1;";
            RecordSet rs = db.getRecordSet(sql);
            while (rs.next()) {
                RecordSet rs2;
                rs2 = table.getRecordSet("\"FULLFILESYSTEMPATH\" = " + StringUtilities.wrapQuotes(rs.getString(1)));
                boolean isFirst = true;
                int mediaid = -1;
                while (rs2.next()) {
                    String uuid = getUUID(rs2);
                    if (uuid == null) {
                        continue;
                    }

                    uuids.add(uuid);
                    if (isFirst) {
                        mediaid = insertMediaAsset(db, uuid, rs2);
                        isFirst = false;
                    } else {
                        //
                        // Map this uuid to the now singular common mediaid in case
                        // these duplicates show up elsewhere in e.g. ENCOUNTER_IMAGES
                        //
                        insertMediaConversion(db, uuid, mediaid);

                        //
                        // Delete the extra mediasubmissions that were duplicates pointing to the same file.
                        //
                        Table tablemsm = db.getTable("mediasubmission_media");
                        tablemsm.deleteRows("oldmediaid = " + StringUtilities.wrapQuotes(uuid));
                    }
                }
            }


            //
            // Get all rows
            //
            rs = table.getRecordSet();
            while (rs.next()) {
                String uuid = getUUID(rs);
                if (uuid == null || uuids.contains(uuid)) {
                    //
                    // We already dealt with these uuid's above because they
                    // were part of the duplicates problem.
                    //
                    continue;
                }

                insertMediaAsset(db, uuid, rs);
            }
        }
    }
}
