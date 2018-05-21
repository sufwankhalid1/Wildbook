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
import org.ecocean.Survey;
import org.ecocean.Shepherd;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class SurveyRemoveDataFile extends HttpServlet {

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
    myShepherd.setAction("SurveyRemoveDataFile.class");
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked = false;

    String fileName = "None";
    String surveyID = "None";


    fileName = request.getParameter("filename").replaceAll("%20", " ");
    surveyID = request.getParameter("survey");
    myShepherd.beginDBTransaction();
    if ((request.getParameter("survey") != null) && (request.getParameter("filename") != null) && (myShepherd.isSurvey(surveyID))) {
      Survey sv = myShepherd.getSurvey(surveyID);

      int positionInList = 0;
      try {
        sv.removeDataFile(fileName);
        sv.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "Removed survey " + surveyID + " data file: " + fileName + ".</p>");

      } catch (Exception le) {
        locked = true;
        myShepherd.rollbackDBTransaction();
      }

      if (!locked) {
        myShepherd.commitDBTransaction();
        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Success!</strong> I have successfully removed the data file. When returning to the survey page, please make sure to refresh your browser to see the changes. Changes may not be visible until you have done so.");

        out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/surveys/survey.jsp?surveyID=" + surveyID + "\">Return to survey " + surveyID + "</a></p>\n");
        out.println(ServletUtilities.getFooter(context));
      } else {

        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Failure!</strong> This record is currently being modified by another user. Please wait a few seconds before trying to remove this data file again.");

        out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/surveys/survey.jsp?surveyID=" + surveyID + "\">Return to survey " + surveyID + "</a></p>\n");
        out.println(ServletUtilities.getFooter(context));

      }
    } else {
      myShepherd.rollbackDBTransaction();
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Error:</strong> I was unable to remove your data file. I cannot find the record that you intended it for in the database, or I wasn't sure what file you wanted to remove.");
      out.println(ServletUtilities.getFooter(context));

    }
    out.close();
    myShepherd.closeDBTransaction();
  }

}
	
	
