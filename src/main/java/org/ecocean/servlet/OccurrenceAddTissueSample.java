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
import org.ecocean.genetics.TissueSample;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;


public class OccurrenceAddTissueSample extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  private void setDateLastModified(Occurrence occ) {
    String date = ServletUtilities.getDate();
    occ.setDWCDateLastModified(date);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context = ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("OccurrenceAddTissueSample.class");
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    myShepherd.beginDBTransaction();
    System.out.println("Occurrence Number From jsp : "+request.getParameter("number"));
    if (request.getParameter("number") != null && myShepherd.isOccurrence(request.getParameter("number"))) {
      
      Occurrence occ = myShepherd.getOccurrence(request.getParameter("number"));
      setDateLastModified(occ);
      
      boolean noData = false;
      TissueSample ts = null;
      
      //Required!
      String sampleID = null;
      if (request.getParameter("sampleID")!=null) {
        sampleID = request.getParameter(sampleID);
        // Typically TissueSamples are stored on encounter, working around for occ.
        // The first arg is usually a Enc ID.
        ts = new TissueSample("", sampleID);
        
        String presMethod = null;
        if (request.getParameter("preservationMethod")!=null) {
          presMethod = request.getParameter("preservationMethod");
          ts.setPreservationMethod(presMethod);
        }
        String labID =  null;
        if (request.getParameter("storageLabID")!=null) {
          labID = request.getParameter("storageLabID");
          ts.setStorageLabID(labID);
        }
        String samplingProtocol =  null;
        if (request.getParameter("samplingProtocol")!=null) {
          samplingProtocol = request.getParameter("storageLabID");
          ts.setSamplingProtocol(samplingProtocol);
        }
        String effort =  null;
        if (request.getParameter("samplingEffort")!=null) {
          samplingProtocol = request.getParameter("samplingEffort");
          ts.setSamplingEffort(effort);
        }
        String fieldNumber =  null;
        if (request.getParameter("fieldNumber")!=null) {
          fieldNumber = request.getParameter("fieldNumber");
          ts.setSamplingEffort(fieldNumber);
        }
        String fieldNotes = null;
        if (request.getParameter("fieldNotes")!=null) {
          fieldNotes = request.getParameter("fieldNotes");
          ts.setFieldNotes(fieldNotes);
        }
        String eventRemarks = null;
        if (request.getParameter("eventRemarks")!=null) {
          fieldNotes = request.getParameter("eventRemarks");
          ts.setEventRemarks(eventRemarks);
        }
        String institutionID = null;
        if (request.getParameter("institutionID")!=null) {
          fieldNotes = request.getParameter("institutionID");
          ts.setInstitutionID(institutionID);
        }
        String collectionID = null;
        if (request.getParameter("collectionID")!=null) {
          collectionID = request.getParameter("collectionID");
          ts.setCollectionID(collectionID);
        }
        String collectionCode = null;
        if (request.getParameter("collectionCode")!=null) {
          collectionCode = request.getParameter("collectionCode");
          ts.setCollectionCode(collectionCode);
        }
        String datasetID = null;
        if (request.getParameter("datasetID")!=null) {
          collectionID = request.getParameter("datasetID");
          ts.setDatasetID(datasetID);
        }
        String datasetName = null;
        if (request.getParameter("datasetName")!=null) {
          datasetName = request.getParameter("collectionID");
          ts.setDatasetName(datasetName);
        }
      } else {
        noData = true;
      }

      if (!noData) {
        try {
          myShepherd.getPM().makePersistent(ts);
          myShepherd.commitDBTransaction();
          
          occ.addBaseTissueSample(ts); 
          
          out.println(ServletUtilities.getHeader(request));
          myShepherd.commitDBTransaction();
          out.println("<strong>Success:</strong> Tissue Sample saved to this occurrence.");
          out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + request.getParameter("number") + "\">Return to occurrence " + request.getParameter("number") + "</a></p>\n");
          String message = "A new comment has been added to occurrence " + request.getParameter("number") + ". The new comment is: \n" + request.getParameter("comments");
          out.println(ServletUtilities.getFooter(context));          
          
        } catch (Exception e) {
          e.printStackTrace();
          
          myShepherd.rollbackDBTransaction();
          
          out.println(ServletUtilities.getHeader(request));
          out.println("<strong>Error:</strong> There was an error while saving this Tissue Sample. Please try again.");
          out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + request.getParameter("number") + "\">Return to occurrence " + request.getParameter("number") + "</a></p>\n");
          out.println(ServletUtilities.getFooter(context));
        }
      }

    } else {
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Error:</strong> I don't have enough information to add a tissue sample to this occurrence.");
      out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + request.getParameter("number") + "\">Return to occurrence " + request.getParameter("number") + "</a></p>\n");
      out.println(ServletUtilities.getFooter(context));
    }
    myShepherd.closeDBTransaction();


    out.close();
    myShepherd.closeDBTransaction();
  }
}
  
  
