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
import com.samsix.util.OsUtils;

public class EncounterExport {
    private final Path outputBaseDir;

    //
    // CSV file has these columns in this order.
    //


    private static final int COL_FILENAME = 0;
    private static final int COL_MEDIAID = 1;
    private static final int COL_CONTRIBUTOR_FULLNAME = 2;
    private static final int COL_CONTRIBUTOR_EMAIL = 3;
    private static final int COL_CONTRIBUTOR_ORGANIZATION = 4;
    private static final int COL_SUBMITTED_ON = 5;
    private static final int COL_ENC_DATE = 6;
    private static final int COL_ENC_START_TIME = 7;
    private static final int COL_ENC_END_TIME = 8;
    private static final int COL_ENC_LAT = 9;
    private static final int COL_ENC_LONG = 10;
    private static final int COL_ENC_LOC_ID = 11;
    private static final int COL_ENC_LOC_VERBATIM = 12;
    private static final int COL_ENC_LOC_ACCURACY = 13;
    private static final int COL_ENC__LOC_PRECISION = 14;
    private static final int COL_ENC_COMMENTS = 15;
    private static final int COL_IND_SPECIES = 16;
    private static final int COL_IND_ID = 17;
    private static final int COL_IND_ALTERNATE_ID = 18;
    private static final int COL_IND_COMMENTS = 19;

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

//            String filename = ma.getPath().getFileName().toString();
            String filename = null;
            if (encounter.getEncDate() != null) {
                filename = encounter.getEncDate().toString();
            }
            if (user != null && user.getFullName() != null) {
                if (filename == null) {
                    filename = "";
                } else {
                    filename += "_";
                }
                filename += user.getFullName().replace(" ", "_");
            }
            if (filename == null) {
                filename = "";
            } else {
                filename += "_";
            }
            filename += ma.getID() + "." + OsUtils.getFileExtension(ma.getPath().toString());

            Path output = Paths.get(outputDir.toString(), filename);
            if (!Files.exists(output)) {
                try {
                    Files.copy(ma.getFullPath(), output);
                } catch (Exception ex) {
                    throw new DatabaseException("Can't copy file.", ex);
                }
            }

            String[] encArray = new String[cols.length];
            encArray[COL_FILENAME] = filename;
            encArray[COL_MEDIAID] = Integer.toString(ma.getID());

            if (user == null) {
                encArray[COL_CONTRIBUTOR_FULLNAME] = null;
                encArray[COL_CONTRIBUTOR_EMAIL] = null;
                encArray[COL_CONTRIBUTOR_ORGANIZATION] = null;
            } else {
                encArray[COL_CONTRIBUTOR_FULLNAME] = user.getFullName();
                encArray[COL_CONTRIBUTOR_EMAIL] = user.getEmail();
                if (user.getOrganization() == null) {
                    encArray[COL_CONTRIBUTOR_ORGANIZATION] = null;
                } else {
                    encArray[COL_CONTRIBUTOR_ORGANIZATION] = user.getOrganization().toString();
                }
            }

            if (ma.getSubmittedOn() == null) {
                encArray[COL_SUBMITTED_ON] = null;
            } else {
                encArray[COL_SUBMITTED_ON] = ma.getSubmittedOn().toString();
            }

            if (encounter.getEncDate() == null) {
                encArray[COL_ENC_DATE] = null;
            } else {
                encArray[COL_ENC_DATE] = encounter.getEncDate().toString();
            }

            if (encounter.getStarttime() == null) {
                encArray[COL_ENC_START_TIME] = null;
            } else {
                encArray[COL_ENC_START_TIME] = encounter.getStarttime().toString();
            }

            if (encounter.getEndtime() == null) {
                encArray[COL_ENC_END_TIME] = null;
            } else {
                encArray[COL_ENC_END_TIME] = encounter.getEndtime().toString();
            }

            if (encounter.getLocation() == null) {
                encArray[COL_ENC_LAT] = null;
                encArray[COL_ENC_LONG] = null;
                encArray[COL_ENC_LOC_ID] = null;
                encArray[COL_ENC_LOC_VERBATIM] = null;
                encArray[COL_ENC_LOC_ACCURACY] = null;
                encArray[COL_ENC__LOC_PRECISION] = null;
            } else {
                if (encounter.getLocation().getLatitude() == null) {
                    encArray[COL_ENC_LAT] = null;
                } else {
                    encArray[COL_ENC_LAT] = encounter.getLocation().getLatitude().toString();
                }

                if (encounter.getLocation().getLongitude() == null) {
                    encArray[COL_ENC_LONG] = null;
                } else {
                    encArray[COL_ENC_LONG] = encounter.getLocation().getLongitude().toString();
                }

                if (encounter.getLocation().getLocationid() == null) {
                    encArray[COL_ENC_LOC_ID] = null;
                } else {
                    encArray[COL_ENC_LOC_ID] = encounter.getLocation().getLocationid();
                }

                if (encounter.getLocation().getVerbatimLocation() == null) {
                    encArray[COL_ENC_LOC_VERBATIM] = null;
                } else {
                    encArray[COL_ENC_LOC_VERBATIM] = encounter.getLocation().getVerbatimLocation();
                }

                if (encounter.getLocation().getAccuracy() == null) {
                    encArray[COL_ENC_LOC_ACCURACY] = null;
                } else {
                    encArray[COL_ENC_LOC_ACCURACY] = encounter.getLocation().getAccuracy().toString();
                }

                if (encounter.getLocation().getPrecisionSource() == null) {
                    encArray[COL_ENC__LOC_PRECISION] = null;
                } else {
                    encArray[COL_ENC__LOC_PRECISION] = encounter.getLocation().getPrecisionSource().toString();
                }
            }

            if (encounter.getComments() == null) {
                encArray[COL_ENC_COMMENTS] = null;
            } else {
                encArray[COL_ENC_COMMENTS] = encounter.getComments();
            }

            if (individual == null) {
                encArray[COL_IND_SPECIES] = null;
                encArray[COL_IND_ID] = null;
                encArray[COL_IND_ALTERNATE_ID] = null;
                encArray[COL_IND_COMMENTS] = null;
            } else {
                if (individual.getSpecies() != null) {
                    encArray[COL_IND_SPECIES] = individual.getSpecies().getName();
                } else {
                    encArray[COL_IND_SPECIES] = null;
                }
                encArray[COL_IND_ID] = individual.getId().toString();
                encArray[COL_IND_ALTERNATE_ID] = individual.getAlternateId();
                encArray[COL_IND_COMMENTS] = individual.getBio();

            }

            rows.add(encArray);
        });

        return rows;
    }
}
