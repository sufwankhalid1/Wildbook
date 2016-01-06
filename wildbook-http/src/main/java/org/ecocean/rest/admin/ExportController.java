package org.ecocean.rest.admin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.Global;
import org.ecocean.admin.encounter.EncounterExport;
import org.ecocean.export.Export;
import org.ecocean.export.ExportFactory;
import org.ecocean.search.SearchData;
import org.ecocean.servlet.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/export")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    @RequestMapping(value = "encounters", method = RequestMethod.POST)
    public int searchEncounters(final HttpServletRequest request,
            final SearchData search) throws DatabaseException, IOException {

        int userid = ServletUtils.getUser(request).getId();

        Export export;
        try (Database db = ServletUtils.getDb(request)) {
            export = ExportFactory.addExport(db, userid, LocalDateTime.now().toString());
        }

        Path outputBaseDir = Paths.get(Global.INST.getAppResources().getString("export.outputdir", "/var/tmp/exports"), "encounters");

        executor.execute(new ExportRunner(Global.INST.getConnectionInfo(), export, outputBaseDir, search));

        return export.getExportId();
    }

    private static class ExportRunner implements Runnable {
        private final ConnectionInfo ci;
        private final Export export;
        private final Path outputBaseDir;
        private final SearchData search;

        public ExportRunner(final ConnectionInfo ci, final Export export, final Path outputBaseDir, final SearchData search) {
            this.ci = ci;
            this.export = export;
            this.outputBaseDir = outputBaseDir;
            this.search = search;
        }

        @Override
        public void run() {
            try (Database db = new Database(ci)) {
                try {
                    export.setStatus(1);
                    ExportFactory.save(db, export);
                    EncounterExport exporter;
                    exporter = new EncounterExport(outputBaseDir);
                    exporter.export(db, search, export.getOutputdir());
                } catch (Throwable ex) {
                    export.setError(ex.toString());
                }

                export.setStatus(2);
                ExportFactory.save(db, export);
            } catch (DatabaseException ex) {
                logger.error("Cannot save export", ex);
            }
        }
    }
}
