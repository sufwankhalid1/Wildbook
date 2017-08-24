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

package org.ecocean.servlet;

import org.ecocean.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


public class BentoSearch extends HttpServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  PrintWriter out = null;
  ArrayList<String> files = new ArrayList<String>();

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.beginDBTransaction();
    myShepherd.setAction("BentoSearch.class");
    out = response.getWriter();
    System.out.println("Searching saved bento files...");
    //set up for response
    response.setContentType("text/html");
    
    String urlLoc = "//" + CommonConfiguration.getURLLocation(request);
    request.setAttribute("returnUrl","//"+urlLoc+"/bentoSearch.jsp");
    String message = " ";
    String criteria = "<label>Criteria: ";
    if (request.getParameter("defaultValue") != null) {
      
      String startDate = null;
      if (request.getParameter("startDate")!=null) {
        startDate = request.getParameter("startDate").toString().trim();    
        criteria += "Start Date "+startDate+", ";
      }
      
      String endDate = null;
      if (request.getParameter("endDate")!=null) {
        endDate = request.getParameter("endDate").toString().trim();  
        criteria += "End Date "+endDate+", ";
      }
      
      String vessel = null;
      if (request.getParameter("newVessel")==null||request.getParameter("newVessel").equals("")) {
        if (request.getParameter("vessel")!=null&&!request.getParameter("vessel").equals("")) {
          vessel = request.getParameter("vessel").toString().trim();
          criteria += "Vessel "+vessel+", ";
        }
      } else {
        vessel = request.getParameter("newVessel").toString().trim();
        criteria += "Vessel "+vessel+", ";
      }
      
      String location = null;
      if (request.getParameter("newLocation")==null||request.getParameter("newLocation").equals("")) {
        if (request.getParameter("location")!=null&&!request.getParameter("location").equals("")) {
          location = request.getParameter("location").toString().trim();
          criteria += "Location "+location+", ";
        }
      } else {
        location = request.getParameter("newLocation").toString().trim();
        criteria += "Location "+location+", ";
      }
      
      String fileType = null;
      if (request.getParameter("fileType")!=null&&!request.getParameter("fileType").equals("")) {
        fileType = request.getParameter("fileType").toString().trim();
        criteria += "File Type "+fileType+", ";
      }
      
      if (criteria.length() > 20) {
        criteria = criteria.substring(0, criteria.length()-3);
        criteria += ".</label>";
      } else {
        criteria += "None.</label>";
      }
      request.setAttribute("criteria", criteria);
      
          
      File bentoDir = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/");

      ArrayList<String> files = new ArrayList<String>();
      
      // Retrieve all saved bento files . 
      files = getBentoFiles(bentoDir, myShepherd);
      
      for (String file : files) {
        message += file;
      }
      
      System.out.println(files.size());
      System.out.println(files.toString());

      
      if (message!=null) {
        request.setAttribute("result", message);        
      }
    } else {
      System.out.println("There was something wrong with this search request.");
    }
    myShepherd.closeDBTransaction();
    request.setAttribute("returnUrl","//"+urlLoc+"/bentoSearch.jsp");
    getServletContext().getRequestDispatcher("/bentoSearchResults.jsp").forward(request, response);    
    out.close();    
  }
  
  public ArrayList<String> getBentoFiles(File path, Shepherd myShepherd) {
    try {
      if (path.isDirectory()) {
        String[] subDirs = path.list();
        System.out.println("There are "+subDirs.length+" files in the folder"+path.getAbsolutePath());
        for (int i=0;subDirs!=null&&i<subDirs.length;i++ ) {
          if (!path.getAbsolutePath().contains("/images")) {
            getBentoFiles(new File(path, subDirs[i]), myShepherd);            
          }
        }
      } 
      if (path.isFile()) {
        String name = path.getName();
        String absPath = path.getAbsolutePath();
        String servletArg = "/BentoDownload?path=";
        files.add("<li><a href=\""+servletArg+absPath+"\""+path.getAbsolutePath()+">"+name+"</a><li/>");  
      }
      if (path.isDirectory()) {
        System.out.println("Found Directory: "+path.getAbsolutePath());
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Failed to traverse Excel files at path "+path.getAbsolutePath()); 
    }
    return files;
  }
  
}

