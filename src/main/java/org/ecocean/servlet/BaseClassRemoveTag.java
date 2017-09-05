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

import org.ecocean.CommonConfiguration;
import org.ecocean.Occurrence;
import org.ecocean.Shepherd;
import org.ecocean.tag.AcousticTag;
import org.ecocean.tag.DigitalArchiveTag;
import org.ecocean.tag.MetalTag;
import org.ecocean.tag.SatelliteTag;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


public class BaseClassRemoveTag extends HttpServlet {
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
    
    // Work only for occurrences so far, can be set up for enc easily with cast...
    
    request.setCharacterEncoding("UTF-8");
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("BaseClassRemoveTag.class");
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    
    String id = null;
    String tagType = null;
    String occID = null;
    
    boolean success = false;
    if (request.getParameter("id")!=null&&request.getParameter("tagType")!=null&&request.getParameter("occID")!=null) {
      
      id = request.getParameter("id");
      tagType = request.getParameter("tagType");
      occID = request.getParameter("occID");
      
      Occurrence occ = null;
      try {
        occ = myShepherd.getOccurrence(occID);        
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("There was no occurrence found to delete a tag from.");
      }
      
      try {
        if (tagType.equals("dat")) {     
          ArrayList<DigitalArchiveTag> tags = occ.getBaseDigitalArchiveTagArrayList();
          for (DigitalArchiveTag tag : tags) {
            System.out.println("Here's the digital archive tag we are deleting: "+tag.toString());
            System.out.println("This tag ID :"+tag.getId()+" Sent ID : "+id);
            if (tag.getId().equals(id)) {
              tags.remove(tag);
              occ.clearBaseDigitalArchiveTags();
              occ.addBaseDigitalArchiveTagArrayList(tags);
              success = true;
              System.out.println("Checking tag...");
              break;
            }
          }
        } else if (tagType.equals("satellite")) {
          ArrayList<SatelliteTag> tags = occ.getBaseSatelliteTagArrayList();
          for (SatelliteTag tag : tags) {
            System.out.println("Here's the satellite tag we are deleting: "+tag.toString());
            System.out.println("This tag ID :"+tag.getId()+" Sent ID : "+id);
            if (tag.getId().equals(id)) {
              tags.remove(tag);
              occ.clearBaseSatelliteTags();
              occ.addBaseSatelliteTagArrayList(tags);
              success = true;
              System.out.println("Checking tag...");
              break;
            }
          }
          
        } else if (tagType.equals("acoustic")) {
          ArrayList<AcousticTag> tags = occ.getBaseAcousticTagArrayList();
          for (AcousticTag tag : tags) {
            System.out.println("Here's the acoustic tag we are deleting: "+tag.toString());
            System.out.println("This tag ID :"+tag.getId()+" Sent ID : "+id);
            if (tag.getId().equals(id)) {
              tags.remove(tag);
              occ.clearBaseAcousticTags();
              occ.addBaseAcousticTagArrayList(tags);
              success = true;
              System.out.println("Checking tag...");
              break;
            }
          }
          
        } else if (tagType.equals("metal")) {
          ArrayList<MetalTag> tags = occ.getBaseMetalTagArrayList();
          for (MetalTag tag : tags) {
            System.out.println("Here's the metal tag we are deleting: "+tag.toString());
            System.out.println("This tag ID :"+tag.getId()+" Sent ID : "+id);
            if (tag.getId().equals(id)) {
              tags.remove(tag);
              occ.clearBaseMetalTags();
              occ.addBaseMetalTagArrayList(tags);
              success = true;
              break;
            }
          }
        }        
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Hit an error iterating through the OCC's tags...");
      }
      
      out.println(ServletUtilities.getHeader(request));
      if (success) {
        out.println("<p><strong>Success!</strong> I have deleted the selected tag from the database.</p><br/>");
        out.println("<small><strong>Type:</strong>"+tagType+"</small>");
        System.out.println("Success!");

        out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + request.getParameter("number") + "\">Return to occurrence " + request.getParameter("number") + "</a></p>\n");
      } else {
        out.println("<strong>Failure!</strong> There was a problem deleting this tag from the database.");
        out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + request.getParameter("number") + "\">Return to occurrence " + request.getParameter("number") + "</a></p>\n");
        System.out.println("Failure!");
      }
      out.println(ServletUtilities.getFooter(context));
        
      out.close();
      myShepherd.closeDBTransaction();
    }
  }
}





