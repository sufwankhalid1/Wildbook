package org.ecocean.rest.admin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.Individual;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.rest.search.SearchData;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVWriter;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/export")
public class ExportController {
    private static final String path = "/var/wildbook/exports";

    //
    // CSV file has these columns in this order.
    //
    private static final String[] cols = {"mediaasset_filename", "mediaasset_original_filename", "mediaasset_mediaid", "mediaasset_contributor_fullname",
                                          "mediaasset_contributor_email", "mediaasset_contributor_organization", "mediaasset_submitted_on",
                                          "enc_date", "enc_start_time", "enc_end_time", "enc_lat", "enc_long", "enc_loc_id", "enc_loc_verbatim",
                                          "enc_loc_precision", "enc_comments", "ind_speicies", "ind_id", "ind_alternate_id", "ind_comments"};

    @RequestMapping(value = "encounters", method = RequestMethod.POST)
    public void searchEncounters(final HttpServletRequest request,
            final SearchData search) throws DatabaseException, IOException {
        try (Database db = ServletUtils.getDb(request)) {
            List<Encounter> encounters = AdminSearchController.searchEncounters(db, search);
            createCSV(db, encounters);
        }
    }

    private void createCSV(final Database db, final List<Encounter> encs) throws DatabaseException, IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

        try (FileWriter output = new FileWriter(path + File.separator + "Encounter_CSV_" + timeStamp + ".csv")) {
            try (CSVWriter writer = new CSVWriter(output)) {
                writer.writeNext(cols);
                for (Iterator<Encounter> ii = encs.iterator(); ii.hasNext();) {
                    writer.writeAll(buildEnc(db, ii.next()));
                }
            }
        }
    }

    private List<String[]> buildEnc(final Database db, final Encounter encounter) throws DatabaseException {
        Individual individual = encounter.getIndividual();

        List<String[]> rows = new ArrayList<>();

        db.select(EncounterFactory.getMediaStatement(encounter.getId()), (rs) -> {
            MediaAsset ma = MediaAssetFactory.valueOf(rs);
            User user = UserFactory.readUser(rs);

            String[] encArray = new String[cols.length];
            //
            // TODO: media filename
            // encArray[0] = filename
            // encArray[1] = original_file
            //

            encArray[2] = Integer.toString(ma.getID());
            encArray[3] = user.getFullName();
            encArray[4] = user.getEmail();
            encArray[5] = user.getOrganization().toString();
            encArray[6] = ma.getSubmittedOn().toString();

            encArray[7] = encounter.getEncDate().toString();
            encArray[8] = encounter.getStarttime().toString();
            encArray[9] = encounter.getEndtime().toString();
            encArray[10] = encounter.getLocation().getLatitude().toString();
            encArray[11] = encounter.getLocation().getLongitude().toString();
            encArray[12] = encounter.getLocation().getLocationid();
            encArray[13] = encounter.getLocation().getVerbatimLocation();
            //
            //TODO: location precision
            //location precision
            //encArray[14] = encounter.getLocation()
            //
            encArray[15] = encounter.getComments();


            encArray[16] = individual.getSpecies().getName();
            encArray[17] = individual.getId().toString();
            encArray[18] = individual.getAlternateId();
            encArray[19] = individual.getComments();

            rows.add(encArray);
        });

        return rows;
    }


}
