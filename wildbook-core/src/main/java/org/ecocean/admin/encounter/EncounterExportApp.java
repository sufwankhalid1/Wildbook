package org.ecocean.admin.encounter;

import java.nio.file.Paths;

import org.ecocean.Global;
import org.ecocean.search.EncounterSearch;
import org.ecocean.search.SearchData;

import com.samsix.database.Database;
import com.samsix.util.UtilException;
import com.samsix.util.app.AbstractApplication;

public class EncounterExportApp extends AbstractApplication {

    @Override
    protected void addOptions() {
        super.addOptions();
    }

    @Override
    protected void checkOptions() {
        super.checkOptions();
    }

    @Override
    public void run() throws UtilException {
        super.run();

        Global.INST.init(null, null);

        SearchData search = new SearchData();
        search.encounter = new EncounterSearch();
        search.encounter.comments = "e";

        try (Database db = Global.INST.getDb()) {
            EncounterExport exporter = new EncounterExport(Paths.get("/var/tmp"));
            exporter.export(db, search);

            exit();
        } catch (Throwable ex) {
            throw new UtilException("Trouble exporting.", ex);
        }
    }

    public static void main(final String args[])
    {
        launch(new EncounterExportApp(), args);
    }
}

