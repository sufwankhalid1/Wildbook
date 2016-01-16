package org.ecocean.rest.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.ecocean.Global;
import org.ecocean.admin.encounter.EncounterExport;
import org.ecocean.export.Export;
import org.ecocean.export.ExportFactory;
import org.ecocean.search.SearchData;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.util.ErrorInfo;
import org.ecocean.util.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/export")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    private static Path getOutputBaseDir(final String type) {
        return Paths.get(Global.INST.getAppResources().getString("export.outputdir", "/var/tmp/exports"), type);
    }

    @RequestMapping(value = "encounters", method = RequestMethod.POST)
    public int searchEncounters(final HttpServletRequest request,
                                @RequestBody final SearchData search)
        throws DatabaseException, IOException {

        int userid = ServletUtils.getUser(request).getId();

        Export export = new Export();
        export.setUserId(userid);
        export.setOutputdir(UUID.randomUUID().toString());
        export.setType("encounter");
        export.setParamters(new Gson().toJson(search));

        try (Database db = ServletUtils.getDb(request)) {
            ExportFactory.save(db, export);
        }

        executor.execute(new ExportRunner(Global.INST.getConnectionInfo(),
                                          export,
                                          getOutputBaseDir(export.getType()),
                                          search));

        return export.getExportId();
    }

////    @RequestMapping(value = "/download/{id}", produces="application/zip", method = RequestMethod.GET)
//    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
//    public ResponseEntity<?> download(final HttpServletRequest request,
//                                      final HttpServletResponse response,
//                                      @PathVariable("id") final int id) throws DatabaseException, IOException {
//        try (Database db = ServletUtils.getDb(request)) {
//            Export export = ExportFactory.getById(db, id);
//
//            if (export == null) {
//                throw new IllegalArgumentException("Download not found.");
//            }
//
//            User user = ServletUtils.getUser(request);
//            if (user == null) {
//                throw new SecurityException("You are not authorized.");
//            }
//
//            if ( !user.getId().equals(export.getUserId())) {
//                throw new SecurityException("You are not the user who initiated this export.");
//            }
//
//            String fileName = export.getOutputdir() + ".zip";
//
//            Path zippath = Paths.get(getOutputBaseDir(export.getType()).toString(), fileName);
//            if (!Files.exists(zippath)) {
//                if (export.isDelivered()) {
//                    throw new IllegalArgumentException("Our records show that you already downloaded this export zip.");
//                } else {
//                    try {
//                        export.setDelivered(true);
//                        ExportFactory.save(db, export);
//                    } catch (DatabaseException ex) {
//                        logger.error("Can't set delivered status", ex);
//                    }
//                    throw new IllegalArgumentException("This export has been deleted for an unknow reason.");
//                }
//            }
//
////            response.setContentType("application/zip");
//            //response.setHeader("Content-Disposition", "attachment; filename=export" + export.getExportId() + ".zip");
//
//            ResponseEntity<InputStreamResource> result;
//            result = ResponseEntity.ok()
//                                   .contentLength(Files.size(zippath))
//                                   .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                                   .body(new InputStreamResource(new FileInputStream(zippath.toFile())));
//            try {
//                export.setDelivered(true);
//                ExportFactory.save(db, export);
//            } catch (DatabaseException ex) {
//                logger.error("Can't set delivered status", ex);
//            }
//            //Files.delete(zippath);
//
//            return result;
//        } catch (Exception ex) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorInfo(ex));
//        }
//    }


    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    public void download2(final HttpServletRequest request,
                          final HttpServletResponse response,
                          @PathVariable("id") final int id) throws DatabaseException, IOException {
        try (Database db = ServletUtils.getDb(request)) {
            Export export = ExportFactory.getById(db, id);

            if (export == null) {
                throw new IllegalArgumentException("Download not found.");
            }

//            User user = ServletUtils.getUser(request);
//            if (user == null) {
//                throw new SecurityException("You are not authorized.");
//            }
//
//            if ( !user.getId().equals(export.getUserId())) {
//                throw new SecurityException("You are not the user who initiated this export.");
//            }

            String fileName = export.getFullOutputDir() + ".zip";

            Path zippath = Paths.get(getOutputBaseDir(export.getType()).toString(), fileName);
            if (!Files.exists(zippath)) {
                if (export.isDelivered()) {
                    throw new IllegalArgumentException("Our records show that you already downloaded this export zip.");
                } else {
                    try {
                        export.setDelivered(true);
                        ExportFactory.save(db, export);
                    } catch (DatabaseException ex) {
                        logger.error("Can't set delivered status", ex);
                    }
                    //
                    // Log it to our system so that we can check on the file path.
                    //
                    logger.error("The export [" + zippath + "] is missing.");
                    throw new IllegalArgumentException("The export has been deleted for an unknown reason.");
                }
            }

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");

            InputStream inputStream = new FileInputStream(zippath.toFile());
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            inputStream.close();

            try {
                export.setDelivered(true);
                ExportFactory.save(db, export);
            } catch (DatabaseException ex) {
                logger.error("Can't set delivered status", ex);
            }
            Files.delete(zippath);
            FileUtilities.deleteAndPrune(zippath.getParent());
        }
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
                } catch (Throwable ex) {
                    logger.error("Cannot set status to running, aborting export...", ex);
                    return;
                }

                try {
                    EncounterExport exporter = new EncounterExport(outputBaseDir);
                    exporter.export(db, search, export.getFullOutputDir());
                } catch (Throwable ex) {
                    export.setError(new ErrorInfo(ex));
                }

                try {
                    export.setStatus(2);
                    ExportFactory.save(db, export);
                } catch (DatabaseException ex) {
                    logger.error("Cannot save export upon completion", ex);
                }
            }
        }
    }
}
