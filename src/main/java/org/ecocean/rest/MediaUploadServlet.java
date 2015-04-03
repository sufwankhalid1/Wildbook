package org.ecocean.rest;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.ecocean.CommonConfiguration;
import org.ecocean.ImageProcessor;
import org.ecocean.Shepherd;
import org.ecocean.ShepherdPMF;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.mmutil.FileUtilities;
import org.ecocean.servlet.JavascriptGlobals;
import org.ecocean.servlet.ServletUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.SqlInsertFormatter;

//this to be used with Java Servlet 3.0 API
@MultipartConfig
public class MediaUploadServlet
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(MediaUploadServlet.class);
 
    //
    // this will store uploaded files
    // Let's put them in the user's session object so they don't hang around forever?
    //
    private static Map<String, FileSet> filesMap = new HashMap<String, FileSet>();

    /***************************************************
     * URL: /mediaupload
     * doPost(): upload the files and other parameters
     ****************************************************/
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response)
        throws ServletException, IOException
    {
        // 1. Upload File Using Java Servlet API
        //files.addAll(uploadByJavaServletAPI(request));            
      
        // 1. Upload File Using Apache FileUpload
        FileSet upload = uploadByApacheFileUpload(request);

        FileSet fileset = filesMap.get(upload.getID());
      
        if (fileset == null) {
            fileset = upload;
            filesMap.put(upload.getID(), fileset);
        } else {
            fileset.getFiles().addAll(upload.getFiles());
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
         
         FileSet fileset = filesMap.get(id);
         
         // 2. Get the file of index "f" from the list "files"
         FileMeta getFile = fileset.getFiles().get(Integer.parseInt(value));
 
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
    
    
//    public static FileSet uploadByJavaServletAPI(final HttpServletRequest request)
//        throws IOException, ServletException
//    {
//        FileSet fileset = new FileSet();
//
//        // 1. Get all parts
//        Collection<Part> parts = request.getParts();
//
//        fileset.setID(request.getParameter("id"));
//
//        // 3. Go over each part
//        FileMeta temp = null;
//        for(Part part:parts){   
//
//            // 3.1 if part is multiparts "file"
//            if (part.getContentType() != null) {
//                // 3.2 Create a new FileMeta object
//                temp = new FileMeta();
//                temp.setName(getFilename(part));
//                temp.setSize(part.getSize()/1024 +" Kb");
//                temp.setType(part.getContentType());
//                temp.setContent(part.getInputStream());
//
//                // 3.3 Add created FileMeta object to List<FileMeta> files
//                fileset.getFiles().add(temp);
//            }
//        }
//        return fileset;
//    }

    private static FileSet uploadByApacheFileUpload(final HttpServletRequest request)
        throws IOException, ServletException
    {
        System.out.println("calling upload");
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
                    System.out.println("Items are empty!");
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
                        temp.setSize(item.getSize()/1024+ "Kb");

                        // 2.7 Add created FileMeta object to List<FileMeta> files
                        fileset.getFiles().add(temp);
                    }
                }
                
                String context=ServletUtilities.getContext(request);
                String dataDirName = CommonConfiguration.getDataDirectoryName(context);
                
//                File rootDir = ServletUtilities.dataDir(context, request.getServletContext().getRealPath("/"));
                File rootDir = new File(request.getServletContext().getRealPath("/")).getParentFile();

                File baseDir = new File(new File(dataDirName, "mediasubmission"), fileset.getID());
                
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
                        log.error("Can't parse id [" + fileset.getID() + "]", ex);
                        id = -1;
                    }
                    
                    System.out.println("Saving media with id: " + id);
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

    // this method is used to get file name out of request headers
    // 
    private static String getFilename(Part part)
    {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }
    
    
    public static class FileSet
    {
        private List<FileMeta> files = new ArrayList<FileMeta>();
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
    }
  
  
    //
    // Ultimately we want our file meta array to look like this.
    //
//  { 
//  files:
//    [
//      {
//        url: "http://url.to/file/or/page",
//        thumbnail_url: "http://url.to/thumnail.jpg ",
//        name: "thumb2.jpg",
//        type: "image/jpeg",
//        size: 46353,
//        delete_url: "http://url.to/delete /file/",
//        delete_type: "DELETE"
//      }
//    ]
//}
    @JsonIgnoreProperties({"content"})
    public static class FileMeta
    {
        private String name;
        private String size;
        private String type;
        private String url;
        private String thumbnail_url;

        private InputStream content;
        
        public String getName() {
          return name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public String getSize() {
          return size;
        }

        public void setSize(String size) {
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

        public String getThumbnail_url() {
            return thumbnail_url;
        }

        public void setThumbnail_url(String thumbnail_url) {
            this.thumbnail_url = thumbnail_url;
        }
        
        public InputStream getContent()
        {
          return content;
        }
        
        public void setContent(final InputStream content)
        {
          this.content = content;
        }
    }
    
    
    private static class SaveMedia
        implements Runnable
    {
        private String context;
        private int id;
        private File rootDir;
        private File baseDir;
        private FileMeta file;
        
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
        
        public void run() {
            System.out.println("id: " + id);
            File fullBaseDir = new File(rootDir, baseDir.getPath());
            fullBaseDir.mkdirs();
            
            File fullPath = new File(fullBaseDir, file.getName());
            
            file.setUrl("/" + new File(baseDir.getPath(), file.getName()));
            
            CommonConfiguration.getDataDirectoryName(context);
            try {
                System.out.println("fullPath: " + fullPath);
                FileUtilities.saveStreamToFile(file.content, fullPath);
                                
                //
                // Make Thumbnail
                //
                if (Shepherd.isAcceptableImageFile(file.getName())) {
                    File thumbDir = new File(fullBaseDir, "thumb");
                    thumbDir.mkdirs();

                    file.setThumbnail_url("/" + new File(new File(baseDir.getPath(), "thumb"), file.getName()));

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
                
                System.out.println("About to save SPV...");
                Shepherd shepherd = new Shepherd(context);
                SinglePhotoVideo media = new SinglePhotoVideo(null, fullPath);
                shepherd.getPM().makePersistent(media);
                System.out.println("Done saving SPV");

                ConnectionInfo ci = ShepherdPMF.getConnectionInfo();                
                Database db = new Database(ci);
                
                SqlInsertFormatter formatter = new SqlInsertFormatter();
                formatter.append("mediasubmissionid", id);
                formatter.append("mediaid", media.getDataCollectionEventID());
                System.out.println("About to save link...");
                try {
                    db.getTable("mediasubmission_media").insertRow(formatter);
                } finally {
                    db.release();
                }
            } catch (Exception ex) {
                log.error("Trouble saving media file [" + fullPath.getAbsolutePath() + "]", ex);
            }
        }
    }
}
