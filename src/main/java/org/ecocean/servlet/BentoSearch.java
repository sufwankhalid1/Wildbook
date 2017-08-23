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


public class BentoSearch extends HttpServlet {

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


  private void setDateLastModified(Occurrence enc) {
    String strOutputDateTime = ServletUtilities.getDate();
    enc.setDWCDateLastModified(strOutputDateTime);
  }


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("OccurrenceAddComment.class");
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    String urlLoc = "//" + CommonConfiguration.getURLLocation(request);
    String message = "";
    
    myShepherd.beginDBTransaction();
    if ((request.getParameter("defaultValue") != null)) {
      
      String day = null;
      if (request.getParameter("day")!=null) {
        day = request.getParameter("day").toString().trim();    
      }
      
      String year = null;
      if (request.getParameter("year")!=null) {
        year = request.getParameter("year").toString().trim();    
      }
      
      String month = null;
      if (request.getParameter("month")!=null) {
        month = request.getParameter("month").toString().trim();    
      }
      
      String vessel = null;
      if (request.getParameter("newVessel")==null) {
        if (request.getParameter("vessel")!=null) {
          vessel = request.getParameter("vessel").toString().trim();
        }
      } else {
        vessel = request.getParameter("newVessel").toString().trim();
      }
      
      String location = null;
      if (request.getParameter("newLocation")==null) {
        if (request.getParameter("location")!=null) {
          location = request.getParameter("location").toString().trim();
        }
      } else {
        location = request.getParameter("newLocation").toString().trim();
      }
      
          
      File uploadDir = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/");

      


      out.println(ServletUtilities.getHeader(request));
      myShepherd.commitDBTransaction();
      out.println("<strong>Success:</strong> I have successfully added your comments.");
      out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + request.getParameter("number") + "\">Return to eoccurrence " + request.getParameter("number") + "</a></p>\n");


      out.println(ServletUtilities.getFooter(context));
    
    
    out.close();
    myShepherd.closeDBTransaction();
    request.setAttribute("result", message);
    request.setAttribute("returnUrl","//"+urlLoc+"/importBento.jsp");
    getServletContext().getRequestDispatcher("/bentoSearchResults.jsp").forward(request, response);
  
    }
    
  }
  
}

