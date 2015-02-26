package org.ecocean.rest;


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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

//this to be used with Java Servlet 3.0 API
@MultipartConfig
public class MediaUploadServlet
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;
 
    //
    // this will store uploaded files
    // Let's put them in the user's session object?
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

        FileSet fileset = filesMap.get(upload.getUUID());
      
        if (fileset == null) {
            fileset = upload;
        } else {
            fileset.getFiles().addAll(upload.getFiles());
        }
 
        // 2. Set response type to json
        response.setContentType("application/json");
 
        // 3. Convert List<FileMeta> into JSON format
        ObjectMapper mapper = new ObjectMapper();
 
        // 4. Send resutl to client
        mapper.writeValue(response.getOutputStream(), fileset);
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

         String uuid = request.getParameter("uuid");
         
         if (uuid == null) {
             return;
         }
         
         
         FileSet fileset = filesMap.get(uuid);
         
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
    
    
    public static FileSet uploadByJavaServletAPI(final HttpServletRequest request)
        throws IOException, ServletException
    {
        FileSet fileset = new FileSet();

        // 1. Get all parts
        Collection<Part> parts = request.getParts(); // This compiles command-line!!! wtf?

        fileset.setUUID(request.getParameter("uuid"));

        // 3. Go over each part
        FileMeta temp = null;
        for(Part part:parts){   

            // 3.1 if part is multiparts "file"
            if (part.getContentType() != null) {
                // 3.2 Create a new FileMeta object
                temp = new FileMeta();
                temp.setName(getFilename(part));
                temp.setSize(part.getSize()/1024 +" Kb");
                temp.setType(part.getContentType());
                temp.setContent(part.getInputStream());

                // 3.3 Add created FileMeta object to List<FileMeta> files
                fileset.getFiles().add(temp);
            }
        }
        return fileset;
    }

    private static FileSet uploadByApacheFileUpload(final HttpServletRequest request)
        throws IOException, ServletException
    {
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

                // 2.4 Go over each FileItem
                for(FileItem item:items) {

                    // 2.5 if FileItem is not of type "file"
                    if (item.isFormField())  {
                        if (item.getFieldName().equals("uuid")) {
                            fileset.setUUID(item.getString());
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
        private String uuid;
    
        public String getUUID()
        {
            return uuid;
        }
    
        public void setUUID(final String uuid)
        {
            this.uuid = uuid;
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
        
        public InputStream getContent()
        {
          return content;
        }
        
        public void setContent(final InputStream content)
        {
          this.content = content;
        }
    }
}
