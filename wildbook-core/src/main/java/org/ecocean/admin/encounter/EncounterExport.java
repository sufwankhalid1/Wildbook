package org.ecocean.admin.encounter;

import org.ecocean.Global;

import com.samsix.util.UtilException;
import com.samsix.util.app.AbstractApplication;

public class EncounterExport extends AbstractApplication {

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

        ExportController.searchEncounters(request, new SearchData);

        try {
            exit();
        } catch (Throwable ex) {
            throw new UtilException("Trouble reading exif data.", ex);
        }
    }

    public static void main(final String args[])
    {
        launch(new EncounterExport(), args);
    }

    static class SearchData {

    }
}

