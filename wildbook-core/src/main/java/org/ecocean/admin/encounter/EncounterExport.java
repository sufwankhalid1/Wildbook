package org.ecocean.admin.encounter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ecocean.Individual;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.search.SearchData;
import org.ecocean.search.SearchFactory;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;

import com.opencsv.CSVWriter;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlStatement;

public class EncounterExport {
    private final Path outputBaseDir;

    //
    // CSV file has these columns in this order.
    //
    private static final String[] cols = {"filename", "mediaid", "contributor_fullname",
                                          "contributor_email", "contributor_organization", "submitted_on",
                                          "enc_date", "enc_start_time", "enc_end_time",
                                          "enc_lat", "enc_long", "enc_loc_id", "enc_loc_verbatim",
                                          "enc_loc_accuracy", "enc_loc_precision", "enc_comments",
                                          "ind_species", "ind_id", "ind_alternate_id", "ind_comments"};

    public EncounterExport(final Path outputBaseDir) {
        this.outputBaseDir = outputBaseDir;
    }

    public void export(final Database db, final SearchData search) throws DatabaseException, IOException {
        Path outputDir = Paths.get(outputBaseDir.toString(), LocalDateTime.now().toString());
        Files.createDirectories(outputDir);
        List<Encounter> encounters = SearchFactory.searchEncounters(db, search);
        createCSV(db, outputDir, encounters);
    }


    private void createCSV(final Database db, final Path outputDir, final List<Encounter> encs) throws DatabaseException, IOException {
        Path csv = Paths.get(outputDir.toString(), "encounter_images.csv");

        try (FileWriter output = new FileWriter(csv.toFile())) {
            try (CSVWriter writer = new CSVWriter(output)) {
                writer.writeNext(cols);

                for (Encounter encounter : encs) {
                    writer.writeAll(buildEnc(db, outputDir, encounter));
                }
            }
        }
    }


    private List<String[]> buildEnc(final Database db, final Path outputDir, final Encounter encounter) throws DatabaseException {
        Individual individual = encounter.getIndividual();

        List<String[]> rows = new ArrayList<>();

        SqlStatement sql = EncounterFactory.getMediaStatement(encounter.getId());
        sql.addLeftOuterJoin(MediaAssetFactory.ALIAS_MEDIAASSET,
                             "submitterid",
                             UserFactory.TABLENAME_USERS,
                             UserFactory.AlIAS_USERS,
                             UserFactory.PK_USERS);
        db.select(sql, (rs) -> {
            MediaAsset ma = MediaAssetFactory.valueOf(rs);
            User user = UserFactory.readUser(rs);

            String filename = ma.getPath().getFileName().toString();

            try {
                Files.copy(ma.getPath(), Paths.get(outputDir.toString(), filename));
            } catch (Exception ex) {
                throw new DatabaseException("Can't copy file.", ex);
            }

            String[] encArray = new String[cols.length];
            encArray[0] = filename;
            encArray[1] = Integer.toString(ma.getID());
            if (user  == null) {
                encArray[2] = null;
                encArray[3] = null;
                encArray[4] = null;
            } else {
                encArray[2] = user.getFullName();
                encArray[3] = user.getEmail();
                encArray[4] = user.getOrganization().toString();
            }
            encArray[5] = ma.getSubmittedOn().toString();

            encArray[6] = encounter.getEncDate().toString();
            encArray[7] = encounter.getStarttime().toString();
            encArray[8] = encounter.getEndtime().toString();
            encArray[9] = encounter.getLocation().getLatitude().toString();
            encArray[10] = encounter.getLocation().getLongitude().toString();
            encArray[11] = encounter.getLocation().getLocationid();
            encArray[12] = encounter.getLocation().getVerbatimLocation();
            encArray[13] = encounter.getLocation().getAccuracy().toString();
            encArray[14] = encounter.getLocation().getPrecisionSource().toString();
            encArray[15] = encounter.getComments();

            encArray[16] = individual.getSpecies().getName();
            encArray[17] = individual.getId().toString();
            encArray[18] = individual.getAlternateId();
            encArray[19] = individual.getBio();

            rows.add(encArray);
        });

        return rows;
    }
}
