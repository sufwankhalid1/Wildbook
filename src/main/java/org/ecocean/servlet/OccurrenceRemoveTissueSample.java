/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
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
import org.ecocean.genetics.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class OccurrenceRemoveTissueSample extends HttpServlet {

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
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("OccurrenceRemoveTissueSample.class");
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked = false;

    String occId = null;

    occId = request.getParameter("occId");
    myShepherd.beginDBTransaction();
    if ((myShepherd.isOccurrence(occId)) && (request.getParameter("sampleID") != null) && (!request.getParameter("sampleID").equals(""))) {
      Occurrence occ = myShepherd.getOccurrence(occId);
      try {     
          TissueSample ts = new TissueSample();
 
          String sampleID = request.getParameter("sampleID");
          ts = occ.getTissueSampleByID(sampleID);
          occ.baseClassRemoveTissueSample(sampleID);
          occ.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br />" + "Removed tissue sample ID "+request.getParameter("sampleID")+".<br />");
          
          myShepherd.throwAwayTissueSample(ts);             
      } catch (Exception le) {
        locked = true;
        le.printStackTrace();
        myShepherd.rollbackDBTransaction();
        //myShepherd.closeDBTransaction();
      }

      if (!locked) {
        myShepherd.commitDBTransaction();
        //myShepherd.closeDBTransaction();
        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Success!</strong> I have successfully removed tissue sample "+request.getParameter("sampleID")+" for occurrence " + occId + ".</p>");

        out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + occId + "\">Return to occurrence " + occId + "</a></p>\n");
        out.println(ServletUtilities.getFooter(context));
        } else {

        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Failure!</strong> This encounter is currently being modified by another user or is inaccessible. Please wait a few seconds before trying to modify this occurrence again.");

        out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + occId + "\">Return to occurrence " + occId + "</a></p>\n");
        out.println(ServletUtilities.getFooter(context));
      }
    } else {
      myShepherd.rollbackDBTransaction();
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Error:</strong> I was unable to remove the tissue sample. I cannot find the occurrence that you intended it for in the database.");
      out.println(ServletUtilities.getFooter(context));

    }
    out.close();
    myShepherd.closeDBTransaction();
  }


}
  
  