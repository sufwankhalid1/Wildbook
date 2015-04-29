package org.ecocean.rest;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.CommonConfiguration;
import org.ecocean.GeoFileProcessor;
import org.ecocean.ImageProcessor;
import org.ecocean.Shepherd;
import org.ecocean.ShepherdPMF;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.mmutil.FileUtilities;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.util.string.StringUtilities;

@MultipartConfig
public class MediaUploadServlet
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(MediaUploadServlet.class);

    private static final String FILES_MAP = "filesMap";

    //
    // this will store uploaded files
    // Let's put them in the user's session object so they don't hang around forever.
    //
    public static void clearFileSet(final HttpSession session,
                                    final String msid)
    {
        Map<String, FileSet> filesMap = getFilesMap(session);
        filesMap.remove(String.valueOf(msid));
    }


    public static void deleteFileFromSet(final HttpServletRequest request,
                                         final long msid,
                                         final String filename)
    {
        Map<String, FileSet> filesMap = getFilesMap(request.getSession());
        FileSet fileSet = filesMap.get(String.valueOf(msid));

        if (fileSet == null) {
            return;
        }

        String context = ServletUtilities.getContext(request);
        File baseDir = getBaseDir(context, String.valueOf(msid));

        for (FileMeta file : new ArrayList<FileMeta>(fileSet.files)) {
            if (file.name.equalsIgnoreCase(filename)) {
                new Thread(new DeleteMedia(context,
                           msid,
                           getRootDir(request),
                           baseDir,
                           file)).start();
                fileSet.files.remove(file);
            }
        }
    }


    @SuppressWarnings("unchecked")
    private static Map<String, FileSet> getFilesMap(final HttpSession session)
    {
        Map<String, FileSet> filesMap;
        filesMap = (Map<String, FileSet>) session.getAttribute(FILES_MAP);
        if (filesMap == null) {
            filesMap = new HashMap<String, FileSet>();
            session.setAttribute(FILES_MAP, filesMap);
        }

        return filesMap;
    }


    /***************************************************
     * URL: /mediaupload
     * doPost(): upload the files and other parameters
     ****************************************************/
    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response)
        throws ServletException, IOException
    {
        // Upload File Using Apache FileUpload
        FileSet upload = uploadByApacheFileUpload(request);

        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.quickLog("upload", upload.toString()));
        }
        Map<String, FileSet> filesMap = getFilesMap(request.getSession());
        FileSet fileset = filesMap.get(upload.getID());

        if (fileset == null) {
            fileset = upload;
            filesMap.put(upload.getID(), fileset);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(LogBuilder.quickLog("Found fileset", fileset.toString()));
                logger.debug(LogBuilder.quickLog("Number of new files added", upload.getFiles().size()));
            }
            fileset.getFiles().addAll(upload.getFiles());
        }

        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.quickLog("Current fileset", fileset.toString()));
        }

        // 2. Set response type to json
        response.setContentType("application/json");

        // 3. Convert List<FileMeta> into JSON format
        ObjectMapper mapper = new ObjectMapper();

        // 4. Send resutl to client
        mapper.writeValue(response.getOutputStream(), fileset);




//        String context="context0";
//        context=ServletUtilities.getContext(request);
//        Shepherd myShepherd = new Shepherd(context);
//        myShepherd.beginDBTransaction();
//
//        MediaSubmission ms = null;
//        try {
//            PersistenceManager pm;
//            pm = ShepherdPMF.getPMF(context).getPersistenceManager();
//          ms = ((MediaSubmission) (pm.getObjectById(pm.newObjectIdInstance(MediaSubmission.class, mediaid), true)));
//        } catch (Exception nsoe) {
//          return null;
//        }
//        try {
//            //
//            // Try null for encounter id as this is not attached to an encounter.
//            // Hopefully we won't need to specify it.
//            //
//          SinglePhotoVideo newSPV = new SinglePhotoVideo(null,(new File(fullPathFilename)));
//          ms.getMedia().add(newSPV);
////          ms.refreshAssetFormats(context, ServletUtilities.dataDir(context, rootWebappPath).getAbsolutePath(), newSPV, false);
//          myShepherd.commitDBTransaction();
//        } catch (Exception le) {
//          myShepherd.rollbackDBTransaction();
//          myShepherd.closeDBTransaction();
//        } finally {
//            myShepherd.closeDBTransaction();
//        }
    }


    /***************************************************
     * URL: /upload?f=value
     * doGet(): get file of index "f" from List<FileMeta> as an attachment
     ****************************************************/
    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response)
        throws ServletException, IOException
    {
         // 1. Get f from URL upload?f="?"
         String value = request.getParameter("f");

         if (value == null) {
             return;
         }

         String id = request.getParameter("id");

         //
         // TODO: Add id to the "get" url so that we can read it here.
         //
         if (id == null) {
             return;
         }

         Map<String, FileSet> filesMap = getFilesMap(request.getSession());
         FileSet fileset = filesMap.get(id);

         // 2. Get the file of index "f" from the list "files"
         FileMeta getFile = fileset.getFiles().get(Integer.parseInt(value));

         if (logger.isDebugEnabled()) {
             logger.debug(new LogBuilder("GET").appendVar("id", value).appendVar("file", getFile).toString());
         }

         try {
             // 3. Set the response content type = file content type
             response.setContentType(getFile.getType());

             // 4. Set header Content-disposition
             response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getName()+"\"");

             // 5. Copy file inputstream to response outputstream
             InputStream input = getFile.getContent();
             OutputStream output = response.getOutputStream();
             byte[] buffer = new byte[1024*10];

             for (int length = 0; (length = input.read(buffer)) > 0;) {
                 output.write(buffer, 0, length);
             }

             output.close();
             input.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
    }

    private static File getBaseDir(final String context,
                                   final String msid)
    {
        String dataDirName = CommonConfiguration.getDataDirectoryName(context);
        return new File(new File(dataDirName, "mediasubmission"), msid);
    }

    private static File getThumbnailFile(final File baseDir,
                                         final String fileName)
    {
        return new File(new File(baseDir.getPath(), "thumb"), fileName);
    }

    private static File getRootDir(final HttpServletRequest request)
    {
//        return ServletUtilities.dataDir(context, request.getServletContext().getRealPath("/"));
        return new File(request.getServletContext().getRealPath("/")).getParentFile();
    }

    private static FileSet uploadByApacheFileUpload(final HttpServletRequest request)
        throws IOException, ServletException
    {
        //System.out.println("calling upload");
        FileSet fileset = new FileSet();

        // 1. Check request has multipart content
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        FileMeta temp = null;

        // 2. If yes (it has multipart "files")
        if (isMultipart) {

            // 2.1 instantiate Apache FileUpload classes
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            // 2.2 Parse the request
            try {
                // 2.3 Get all uploaded FileItem
                List<FileItem> items = upload.parseRequest(request);

                if (items.isEmpty()) {
                    logger.warn("Items are empty!");
                    return fileset;
                }

                // 2.4 Go over each FileItem
                for(FileItem item:items) {

                    // 2.5 if FileItem is not of type "file"
                    if (item.isFormField())  {
                        if (item.getFieldName().equals("mediaid")) {
                            fileset.setID(item.getString());
                        }
                    } else {
                        // 2.7 Create FileMeta object
                        temp = new FileMeta();
                        temp.setName(item.getName());
                        temp.setContent(item.getInputStream());
                        temp.setType(item.getContentType());
                        temp.setSize(item.getSize());

                        // 2.7 Add created FileMeta object to List<FileMeta> files
                        fileset.getFiles().add(temp);
                    }
                }

                String context = ServletUtilities.getContext(request);

                File rootDir = getRootDir(request);
                File baseDir = getBaseDir(context, fileset.getID());

                for (FileMeta file : fileset.getFiles()) {
                    //
                    // TODO: Save contents of image to a file and create a thumbnail.
                    // Then add url and thumbnail_url to the FileMeta class. Maybe add
                    // thumbnail image binary to FileMeta as well so that we can just
                    // return that in a get? Or should it be statically delivered?
                    // Both could be statically delivered if we save them to a proper
                    // spot and then we can get rid of using the get method?
                    //
                    int id;
                    try {
                        id = Integer.parseInt(fileset.getID());
                    } catch(NumberFormatException ex) {
                        logger.error("Can't parse id [" + fileset.getID() + "]", ex);
                        id = -1;
                    }

                    new Thread(new SaveMedia(context,
                                             id,
                                             rootDir,
                                             baseDir,
                                             file)).start();
                }
            } catch (FileUploadException ex) {
                ex.printStackTrace();
            }
        }
        return fileset;
    }

//    // this method is used to get file name out of request headers
//    //
//    private static String getFilename(Part part)
//    {
//        for (String cd : part.getHeader("content-disposition").split(";")) {
//            if (cd.trim().startsWith("filename")) {
//                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
//                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
//            }
//        }
//        return null;
//    }


    public static class FileSet
    {
        private final List<FileMeta> files = new ArrayList<FileMeta>();
        private String id;

        public String getID()
        {
            return id;
        }

        public void setID(final String id)
        {
            this.id = id;
        }

        public List<FileMeta> getFiles() {
            return files;
        }

        @Override
        public String toString()
        {
            ToStringBuilder builder = new ToStringBuilder(this);
            return builder.append("id", id)
                          .append("files", files).toString();
        }
    }


    @JsonIgnoreProperties({"content"})
    public static class FileMeta
    {
        private String name;
        private long size;
        private String type;
        private String url;
        private String thumbnailUrl;

        private InputStream content;

        public String getName() {
          return name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public long getSize() {
          return size;
        }

        public void setSize(long size) {
          this.size = size;
        }

        public String getType() {
          return type;
        }

        public void setType(String type) {
          this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public InputStream getContent()
        {
          return content;
        }

        public void setContent(final InputStream content)
        {
          this.content = content;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }


    private static class SaveMedia
        implements Runnable
    {
        private final String context;
        private final int id;
        private final File rootDir;
        private final File baseDir;
        private final FileMeta file;

        public SaveMedia(final String context,
                         final int id,
                         final File rootDir,
                         final File baseDir,
                         final FileMeta file)
        {
            this.context = context;
            this.id = id;
            this.rootDir = rootDir;
            this.baseDir = baseDir;
            this.file = file;
        }

        @Override
        public void run() {
            if (logger.isDebugEnabled()) {
                logger.debug(LogBuilder.quickLog("Saving media", id));
            }

            File fullBaseDir = new File(rootDir, baseDir.getPath());
            fullBaseDir.mkdirs();

            File fullPath = new File(fullBaseDir, file.getName());

            file.setUrl("/" + new File(baseDir.getPath(), file.getName()));

            CommonConfiguration.getDataDirectoryName(context);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(LogBuilder.quickLog("fullPath", fullPath.toString()));
                }
                FileUtilities.saveStreamToFile(file.content, fullPath);

                //
                // Make Thumbnail
                //
                if (Shepherd.isAcceptableImageFile(file.getName())) {
                    File thumbDir = new File(fullBaseDir, "thumb");
                    thumbDir.mkdirs();

                    file.setThumbnailUrl("/" + getThumbnailFile(baseDir, file.getName()));

                    File thumbPath = new File(thumbDir, file.getName());

                    ImageProcessor iproc;
                    iproc = new ImageProcessor(context,
                                               "resize",
                                               100,
                                               75,
                                               fullPath.getAbsolutePath(),
                                               thumbPath.getAbsolutePath(),
                                               null);
                    iproc.run();
                }

                if (file.getName().indexOf(".kmz") > -1) {
                    GeoFileProcessor gproc;
                    gproc = new GeoFileProcessor(fullPath.getAbsolutePath());
                    gproc.run();
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("About to save SPV...");
                }
                Shepherd shepherd = new Shepherd(context);
                SinglePhotoVideo media = new SinglePhotoVideo(null, fullPath);
                shepherd.getPM().makePersistent(media);
                if (logger.isDebugEnabled()) {
                    logger.debug("Done saving SPV");
                }

                ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
                Database db = new Database(ci);

                SqlInsertFormatter formatter = new SqlInsertFormatter();
                formatter.append("mediasubmissionid", id);
                formatter.append("mediaid", media.getDataCollectionEventID());
                if (logger.isDebugEnabled()) {
                    logger.debug(LogBuilder.quickLog("About to save link to media", media.getDataCollectionEventID()));
                }
                try {
                    db.getTable("mediasubmission_media").insertRow(formatter);
                } finally {
                    db.release();
                }
            } catch (Exception ex) {
                logger.error("Trouble saving media file [" + fullPath.getAbsolutePath() + "]", ex);
            }
        }
    }


    private static class DeleteMedia
        implements Runnable
    {
        private final String context;
        private final long id;
        private final File rootDir;
        private final File baseDir;
        private final FileMeta file;

        public DeleteMedia(final String context,
                           final long id,
                           final File rootDir,
                           final File baseDir,
                           final FileMeta file)
        {
            this.context = context;
            this.id = id;
            this.rootDir = rootDir;
            this.baseDir = baseDir;
            this.file = file;
        }

        @Override
        public void run() {
            if (logger.isDebugEnabled()) {
                logger.debug(LogBuilder.quickLog("Deleting media", id));
            }

            File fullBaseDir = new File(rootDir, baseDir.getPath());
            fullBaseDir.mkdirs();

            File fullPath = new File(fullBaseDir, file.getName());

            CommonConfiguration.getDataDirectoryName(context);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(LogBuilder.quickLog("fullPath", fullPath.toString()));
                }
                fullPath.delete();

                //
                // Make Thumbnail
                //
                if (Shepherd.isAcceptableImageFile(file.getName())) {
                    File thumbDir = new File(fullBaseDir, "thumb");
                    File thumbPath = new File(thumbDir, file.getName());
                    thumbPath.delete();
                }

                if (file.getName().indexOf(".kmz") > -1) {
                    new File(fullPath.getAbsolutePath() + ".json").delete();
                }

                ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
                Database db = new Database(ci);

                try {
                    String sql;
                    sql = "SELECT mediaid FROM mediasubmission_media msm"
                          + " INNER JOIN \"SINGLEPHOTOVIDEO\" spv ON spv.\"DATACOLLECTIONEVENTID\" = msm.mediaid"
                          + " WHERE spv.\"FILENAME\"= " + StringUtilities.wrapQuotes(file.getName())
                          + " AND mediasubmissionid = " + id;
                    RecordSet rs = db.getRecordSet(sql);
                    if (!rs.next()) {
                        return;
                    }

                    String mediaid = rs.getString("mediaid");

                    //
                    // Delete SPV
                    //
                    if (logger.isDebugEnabled()) {
                        logger.debug("About to delete SPV...");
                    }

                    Shepherd shepherd = new Shepherd(context);
                    PersistenceManager pm = shepherd.getPM();
                    Object obj;
                    obj = pm.getObjectById(pm.newObjectIdInstance(SinglePhotoVideo.class, mediaid ), true);
                    pm.deletePersistent(obj);


                    SqlWhereFormatter formatter = new SqlWhereFormatter();
                    formatter.append("mediasubmissionid", id);
                    formatter.append("mediaid", mediaid);
                    if (logger.isDebugEnabled()) {
                        logger.debug(LogBuilder.quickLog("About to delete link to media", mediaid));
                    }
                    db.getTable("mediasubmission_media").deleteRows(formatter.getWhereClause());
                } finally {
                    db.release();
                }
            } catch (Exception ex) {
                logger.error("Trouble deleting media file [" + fullPath.getAbsolutePath() + "]", ex);
            }
        }
    }
}
