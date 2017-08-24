/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2017 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean.servlet.export;

import org.ecocean.*;
import org.ecocean.servlet.ServletUtilities;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.OutputStream;


public class BentoDownload extends HttpServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.beginDBTransaction();
    myShepherd.setAction("BentoDownload.class");

    String urlLoc = "//" + CommonConfiguration.getURLLocation(request);

    response.setContentType("text/html");
    request.setAttribute("returnUrl","//"+urlLoc+"/bentoSearch.jsp");

    String path = null;
    if (request.getParameter("path")!= null) {
      // This servlet should ONLY allow downloads from the bento_sheet directory.
      if (request.getParameter("path").contains("bento_sheets")&&(request.getParameter("path").toLowerCase().endsWith("csv")||request.getParameter("path").toLowerCase().endsWith("xlsx"))) {
        path = request.getParameter("path");
        path = path.replace("/opt/tomcat", System.getProperty("catalina.base"));
      }
    }
    
    if (path!=null) {      
      File file = new File(path);
      ServletContext servletContext = getServletContext();
      String mimeType = servletContext.getMimeType(path);
      
      if (mimeType==null) {
        mimeType = "application/octet-stream"; 
      }
      System.out.println("MimeType: "+mimeType);
      System.out.println("Can read? "+file.canRead());
      System.out.println("Length? "+file.length());
      
      response.setContentType(mimeType);
      response.setContentLength((int) file.length());
      response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
      
      OutputStream os = null;
      FileInputStream is = null;
      try {
        is = new FileInputStream(file);
        os = response.getOutputStream();
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      byte[] buffer = new byte[8192];
      int bytesRead = -1;
      
      
      while ((bytesRead = is.read(buffer))!=-1) {
        os.write(buffer, 0, bytesRead);
      }
      
      is.close();
      os.flush();
    }
    myShepherd.closeDBTransaction();    
  }  
  
}







