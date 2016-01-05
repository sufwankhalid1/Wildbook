package org.ecocean.rest.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.admin.encounter.EncounterExport;
import org.ecocean.search.SearchData;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/export")
public class ExportController {
    @RequestMapping(value = "encounters", method = RequestMethod.POST)
    public void searchEncounters(final HttpServletRequest request,
            final SearchData search) throws DatabaseException, IOException {
        try (Database db = ServletUtils.getDb(request)) {
            EncounterExport exporter = new EncounterExport();
            exporter.export(db, search);
        }
    }
}
