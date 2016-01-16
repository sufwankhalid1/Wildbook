package org.ecocean.admin.encounter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.ecocean.util.FileUtilities;

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

    public void export(final Database db, final SearchData search, final Path outputDir) throws DatabaseException, IOException {
        Path outputPath = Paths.get(outputBaseDir.toString(), outputDir.toString());
        try {
            Files.createDirectories(outputPath);
            List<Encounter> encounters = SearchFactory.searchEncounters(db, search);

            Path csv = Paths.get(outputPath.toString(), "encounter_images.csv");

            try (FileWriter output = new FileWriter(csv.toFile())) {
                try (CSVWriter writer = new CSVWriter(output)) {
                    writer.writeNext(cols);

                    for (Encounter encounter : encounters) {
                        writer.writeAll(buildEnc(db, outputPath, encounter));
                    }
                }
            }

            FileUtilities.zipDir(outputPath);
        } finally {
            //
            // Delete all the files you just created. Either the export failed
            // and we need to clear the disk or it succeeded and all the files are
            // zipped up. Also clean up parent directory too.
            //
            FileUtilities.deleteCascade(outputPath);
            FileUtilities.deleteAndPrune(outputPath.getParent()); // prune any empty directories left behind.
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
            Path output = Paths.get(outputDir.toString(), filename);
            if (!Files.exists(output)) {
                try {
                    Files.copy(ma.getFullPath(), output);
                } catch (Exception ex) {
                    throw new DatabaseException("Can't copy file.", ex);
                }
            }

            String[] encArray = new String[cols.length];
            encArray[0] = filename;
            encArray[1] = Integer.toString(ma.getID());

            if (user == null) {
                encArray[2] = null;
                encArray[3] = null;
                encArray[4] = null;
            } else {
                encArray[2] = user.getFullName();
                encArray[3] = user.getEmail();
                if (user.getOrganization() == null) {
                    encArray[4] = null;
                } else {
                    encArray[4] = user.getOrganization().toString();
                }
            }

            if (ma.getSubmittedOn() == null) {
                encArray[5] = null;
            } else {
                encArray[5] = ma.getSubmittedOn().toString();
            }

            if (encounter.getEncDate() == null) {
                encArray[6] = null;
            } else {
                encArray[6] = encounter.getEncDate().toString();
            }

            if (encounter.getStarttime() == null) {
                encArray[7] = null;
            } else {
                encArray[7] = encounter.getStarttime().toString();
            }

            if (encounter.getEndtime() == null) {
                encArray[8] = null;
            } else {
                encArray[8] = encounter.getEndtime().toString();
            }

            if (encounter.getLocation() == null) {
                encArray[9] = null;
                encArray[10] = null;
                encArray[11] = null;
                encArray[12] = null;
                encArray[13] = null;
                encArray[14] = null;
            } else {
                if (encounter.getLocation().getLatitude() == null) {
                    encArray[9] = null;
                } else {
                    encArray[9] = encounter.getLocation().getLatitude().toString();
                }

                if (encounter.getLocation().getLongitude() == null) {
                    encArray[10] = null;
                } else {
                    encArray[10] = encounter.getLocation().getLongitude().toString();
                }

                if (encounter.getLocation().getLocationid() == null) {
                    encArray[11] = null;
                } else {
                    encArray[11] = encounter.getLocation().getLocationid();
                }

                if (encounter.getLocation().getVerbatimLocation() == null) {
                    encArray[12] = null;
                } else {
                    encArray[12] = encounter.getLocation().getVerbatimLocation();
                }

                if (encounter.getLocation().getAccuracy() == null) {
                    encArray[13] = null;
                } else {
                    encArray[13] = encounter.getLocation().getAccuracy().toString();
                }

                if (encounter.getLocation().getPrecisionSource() == null) {
                    encArray[14] = null;
                } else {
                    encArray[14] = encounter.getLocation().getPrecisionSource().toString();
                }
            }

            if (encounter.getComments() == null) {
                encArray[15] = null;
            } else {
                encArray[16] = encounter.getComments();
            }

            if (individual == null) {
                encArray[16] = null;
                encArray[17] = null;
                encArray[18] = null;
                encArray[19] = null;
            } else {
                encArray[16] = individual.getSpecies().getName();
                encArray[17] = individual.getId().toString();
                encArray[18] = individual.getAlternateId();
                encArray[19] = individual.getBio();

            }

            rows.add(encArray);
        });

        return rows;
    }
}
