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
import org.ecocean.Encounter;
import org.ecocean.Shepherd;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


//(re)set the MarkedIndividual on this Encounter, including altering (possibly existing) previous and new MarkedIndividuals accordingly
public class EncounterSetMarkedIndividual extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    doPost(request, response);
  }

  private void setDateLastModified(Encounter enc) {

    String strOutputDateTime = ServletUtilities.getDate();
    enc.setDWCDateLastModified(strOutputDateTime);
  }


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.beginDBTransaction();
    //set up for response
    response.setContentType("text/json");
    PrintWriter out = response.getWriter();
    boolean locked = false;
    boolean isOwner = true;

    String encID = request.getParameter("encounterID");
    String indivID = request.getParameter("individualID");
    if ((encID == null) || (indivID == null)) {
        response.setStatus(400);
        out.println("{\"success\": false, \"error\": \"must pass encounterID and individualID\"}");
        out.close();
        myShepherd.closeDBTransaction();
        return;
    }
    Encounter enc = myShepherd.getEncounter(encID);
    if (enc == null) {
        response.setStatus(400);
        out.println("{\"success\": false, \"error\": \"invalid encounter ID " + encID + "\"}");
        out.close();
        myShepherd.closeDBTransaction();
        return;
    }
    enc.reassignMarkedIndividual(indivID, myShepherd, context);
    myShepherd.commitDBTransaction();
    out.println("{\"success\": true, \"message\": \"updated encounter " + encID + " to individual " + indivID + "\"}");
    System.out.println("reassigned " + indivID + " to encounter " + encID);
    out.close();
    myShepherd.closeDBTransaction();
  }


}
	
	
