package org.ecocean.servlet.importer;

import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.ecocean.*;
import org.ecocean.servlet.*;
import org.ecocean.tag.DigitalArchiveTag;
import org.ecocean.tag.SatelliteTag;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ecocean.media.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImportBento extends HttpServlet {
  /**
   * 
   */
  
  private static final long serialVersionUID = 1L;
  private static PrintWriter out;
  private static String context; 

  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request,  HttpServletResponse response) throws ServletException,  IOException {
    doPost(request,  response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,  IOException { 
    out = response.getWriter();
    context = ServletUtilities.getContext(request);
    System.out.println("=========== Preparing to import bento files. ===========");
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("ImportBento.class");
      
    String message = "";
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    
    if (isMultipart==true) {
      
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "-1");
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Allow-Credentials", "true");
      response.setHeader("Access-Control-Allow-Methods", "POST");
      response.setHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setHeader("Access-Control-Max-Age", "86400");
      
      DiskFileItemFactory factory = new DiskFileItemFactory();

      ServletContext servletContext = this.getServletConfig().getServletContext();
      File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
      factory.setRepository(repository);
      
      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setFileSizeMax(1024*1024*50);
      upload.setSizeMax(1024*1024*50);
      
      List<FileItem> items = null;
      try {
        items = upload.parseRequest(request);
      } catch (FileUploadException e) {
        message += "<p>Failed to upload files. Could not parse the user's request.</p>";
        e.printStackTrace();
      }
      
      for (FileItem fileItem : items) {
        if (!fileItem.isFormField()) {
          
          String fieldName = fileItem.getFieldName();
          String fileName = fileItem.getName();
          String contentType = fileItem.getContentType();
          boolean inMemory = fileItem.isInMemory();
          
          File uploadedFile = null;
          File uploadDir = null;
          if (fileName!=null) {
            try {
              uploadDir = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/");
              uploadedFile = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/"+fileName);
              if (!uploadDir.exists()) {
                uploadDir.mkdir();
              }
              if (!uploadedFile.isDirectory()) {
                fileItem.write(uploadedFile);
                message += "<p>The file "+uploadedFile+" was saved successfully.</p>";                
              } else {
                message += "<p>I cannot upload merely a directory.</p>";
              }
            } catch (Exception e) {
              message += "<p>There was an error trying to save the file "+uploadedFile+".</p>";
              e.printStackTrace();
            }                      
          }
        }
      }
    }
    myShepherd.closeDBTransaction();
    request.setAttribute("result", message);
    getServletContext().getRequestDispatcher("/bentoUploadResult.jsp").forward(request, response);
  }
}






