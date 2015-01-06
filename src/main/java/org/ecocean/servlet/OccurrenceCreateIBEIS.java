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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
/*
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;
import java.util.Arrays;
import java.util.UUID;
*/
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.sql.*;


public class OccurrenceCreateIBEIS extends HttpServlet {

	//5eecb2b2-69ea-6f44-acbf-b8d4e6aaeba8 e.g.

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
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked = false;

		String rootDir = getServletContext().getRealPath("/");
		String baseDir = ServletUtilities.dataDir(context, rootDir);

		Occurrence occ = null;
    String myOccurrenceID="";
    if(request.getParameter("ibeis_encounter_id") != null){
      myOccurrenceID=request.getParameter("ibeis_encounter_id");
      //remove special characters
      //myOccurrenceID=ServletUtilities.cleanFileName(myOccurrenceID);
			occ = myShepherd.getOccurrence(myOccurrenceID);
    }

	String IBEIS_image_path = CommonConfiguration.getProperty("IBEIS_image_path", context);
	if (IBEIS_image_path == null) {
		System.out.println("Please set IBEIS_image_path in CommonConfiguration");
		IBEIS_image_path = "/tmp/broken/imagepath";
	}

	HashMap<String, HashMap<Integer, HashMap>> ienc = getIBEISEncounterStructure(myOccurrenceID, context);

System.out.println("------");
System.out.println(ienc);

	//HashMap<String, HashMap> annots = <HashMap>ienc.get("anns");

	if (ienc.get("anns") == null) {
		//out.println(ServletUtilities.getHeader(request));
		out.println("<p><strong>error!</strong> cannot find annotations for ID <b>" + myOccurrenceID + "</b></p>");
		//out.println(ServletUtilities.getFooter(context));
		out.close();
		return;
	}

	for (HashMap annot : ienc.get("anns").values()) {
System.out.println(" - - - - - - ");
System.out.println(annot);
		//Encounter enc = new Encounter();
//  public Encounter(int day, int month, int year, int hour, String minutes, String size_guess, String location, String submitterName, String submitterEmail, List<SinglePhotoVideo> images) {
		Encounter enc = new Encounter(1, 1, 2014, 22, "30", "Unknown", "", "IBEIS submitter", "submit@ibeis.org", null);
		enc.setEncounterNumber(annot.get("annot_uuid").toString());

		Long etime = null;
		//TODO is there always only one?  i think so
		String iid = annot.get("image_id").toString();
		if (iid != null) {
			int iidInt = Integer.parseInt(iid);
			HashMap idata = ienc.get("imgs").get(iidInt);
//System.out.println(idata);
			Object t = idata.get("image_time_posix");
			if (t != null) etime = Long.parseLong(t.toString(), 10) * 1000;
			File idir = new File(baseDir + "/encounters", enc.subdir());
			if (!idir.exists()) idir.mkdirs();
			File ifile = new File(idir, idata.get("image_uri").toString());
File from = new File(IBEIS_image_path, idata.get("image_uri").toString());
System.out.println("FROM " + from.toString());
System.out.println(ifile.toString() + "<<<?????????????");
			if (from.exists()) {
				if (!ifile.exists()) {
					Files.copy(from.toPath(), ifile.toPath());
				}
				SinglePhotoVideo spv = new SinglePhotoVideo(enc.getCatalogNumber(), ifile);
//System.out.println(ifile);
				enc.addSinglePhotoVideo(spv);
			} else {
				System.out.println("WARNING: " + ifile.toString() + " does not exists; skipping.");
			}
		}

		if ((etime != null) && (etime > 1)) enc.setDateInMilliseconds(etime);

		if (occ == null) {   //new one!  lets make it
			occ = new Occurrence(myOccurrenceID, enc);
			System.out.println("Created new Occurrence " + myOccurrenceID);
		}
		enc.setOccurrenceID(myOccurrenceID);
		occ.addEncounter(enc);
		myShepherd.storeNewOccurrence(occ);


		String indivID = null;
		if (annot.get("label_9") != null) indivID = annot.get("label_9").toString();
		if ((indivID == null) || indivID.equals("")) {
    			enc.setIndividualID("Unassigned");
		} else {
			MarkedIndividual ind = myShepherd.getMarkedIndividual(indivID);
			if (ind == null) {
				try {
					ind = new MarkedIndividual(indivID, enc);
					//ind.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "Created " + newIndividualID + ".</p>");
					ind.setDateTimeCreated(ServletUtilities.getDate());
					myShepherd.addMarkedIndividual(ind);
System.out.println("created new indiv = " + indivID);
				} catch (Exception ex) {
				}
			} else {
System.out.println("existing indiv = " + indivID);
				ind.addEncounter(enc);
				enc.setSex(ind.getSex());  //inherits the individual's sex if we have one
            			myShepherd.commitDBTransaction();
			}
			enc.setIndividualID(indivID);
		}

		myShepherd.storeNewEncounter(enc, enc.getCatalogNumber());
System.out.println(">>>>>>>>>>>>>>>>> stored encounter: " + enc.getCatalogNumber());
	}


	//myShepherd.commitDBTransaction();
	myShepherd.closeDBTransaction();




/*

    if ((myOccurrenceID != null) && (request.getParameter("number") != null) &&  (!myOccurrenceID.trim().equals(""))) {
      myShepherd.beginDBTransaction();
      Encounter enc2make = myShepherd.getEncounter(request.getParameter("number"));
      setDateLastModified(enc2make);


      boolean ok2add=true;

      if (!(myShepherd.isOccurrence(myOccurrenceID))) {


        if ((myShepherd.getOccurrenceForEncounter(enc2make.getCatalogNumber())==null) && (myOccurrenceID != null)) {
          try {
            Occurrence newOccur = new Occurrence(myOccurrenceID.trim(), enc2make);
            newOccur.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "Created " + myOccurrenceID + " from encounter "+request.getParameter("number")+".</p>");
            newOccur.setDateTimeCreated(ServletUtilities.getDate());
            myShepherd.storeNewOccurrence(newOccur);
            
            enc2make.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "Added to new occurrence " + myOccurrenceID + ".</p>");
            enc2make.setOccurrenceID(myOccurrenceID.trim());
          } 
          catch (Exception le) {
            locked = true;
            le.printStackTrace();
            myShepherd.rollbackDBTransaction();
            myShepherd.closeDBTransaction();
          }

          if (!locked&&ok2add) {
            myShepherd.commitDBTransaction();
            myShepherd.closeDBTransaction();


            //output success statement
            out.println(ServletUtilities.getHeader(request));
            out.println("<strong>Success:</strong> Encounter " + request.getParameter("number") + " was successfully used to create occurrence <strong>" + myOccurrenceID + "</strong>.");
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + request.getParameter("number") + "\">Return to encounter #" + request.getParameter("number") + ".</a></p>\n");
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + myOccurrenceID + "\">View <strong>" + myOccurrenceID + ".</strong></a></p>\n");
            out.println(ServletUtilities.getFooter(context));
          } 
          else {
            out.println(ServletUtilities.getHeader(request));
            out.println("<strong>Failure:</strong> Encounter " + request.getParameter("number") + " was NOT used to create a new occurrence. This encounter is currently being modified by another user. Please go back and try to create the new occurrence again in a few seconds.");
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + request.getParameter("number") + "\">Return to encounter " + request.getParameter("number") + ".</a></p>\n");
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + myOccurrenceID + "\">View <strong>" + myOccurrenceID + "</strong></a></p>\n");
            out.println(ServletUtilities.getFooter(context));

          }


        } else {

          myShepherd.rollbackDBTransaction();
          myShepherd.closeDBTransaction();

        }

      } 
      else if ((myShepherd.isOccurrence(myOccurrenceID))) {
        myShepherd.rollbackDBTransaction();
        myShepherd.closeDBTransaction();
        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Error:</strong> An occurrence with this identifier already exists in the database. Select a different identifier and try again.");
        out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + request.getParameter("number") + "\">Return to encounter " + request.getParameter("number") + ".</a></p>\n");
        out.println(ServletUtilities.getFooter(context));

      } 
      else {
        myShepherd.rollbackDBTransaction();
        myShepherd.closeDBTransaction();
        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Error:</strong> You cannot make a new occurrence from this encounter because it is already assigned to another occurrence. Remove it from its previous occurrence if you want to re-assign it elsewhere.");
        out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + request.getParameter("number") + "\">Return to encounter " + request.getParameter("number") + ".</a></p>\n");
        out.println(ServletUtilities.getFooter(context));
      }


    } 
    else {
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Error:</strong> I didn't receive enough data to create a new occurrence from this encounter.");
      out.println(ServletUtilities.getFooter(context));
    }
*/

	response.sendRedirect("http://" + CommonConfiguration.getURLLocation(request) + "/occurrenceIBEIS.jsp?number=" + myOccurrenceID);
/*
      out.println(ServletUtilities.getHeader(request));
      out.println("<p><strong>success!</strong> check out <a href=\"occurrenceIBEIS.jsp?number=" + myOccurrenceID + "\">the Occurrence [ID " + myOccurrenceID + "]</a></p>");
      out.println(ServletUtilities.getFooter(context));

    out.close();
*/
    
  }

	public HashMap getIBEISEncounterStructure(String eid, String context) {
		Connection c = null;
		Statement st = null;
		HashMap<String, HashMap<Integer, HashMap>> rtn = new HashMap<String, HashMap<Integer, HashMap>>();
		//HashMap<String, HashMap> rtn = new HashMap<String, HashMap>();
/*
//INSERT INTO "images" VALUES(2,X'343490D82F94F5E0D6C20DCBE3137BF7','d8903434-942f-e0f5-d6c2-0dcbe3137bf7.jpg','.jpg','easy2.JPG',1035,576,103,-1.0,-1.0,0,0,'');
long a = Long.decode("0x343490D82F94F5E0");
long b = 0xD6C20DCBE3137BF7;
UUID u = new UUID(a,b);
*/
/*
long a = Long.decode("0x343490D82F94F5E0");
long b = Long.decode("0xD6C20DCBE3137BF7");
UUID u = new UUID(a,b);
///UUID u = UUID.fromString("d8903434-942f-e0f5-d6c2-0dcbe3137bf7");
System.out.println(u.toString());
System.out.println("?? d8903434-942f-e0f5-d6c2-0dcbe3137bf7");

b037fed5-66c1-4853-8a63-eefb63ce7c42 - ibeis
d5fe37b0-c166-5348-8a63-eefb63ce7c42 - java
*/


		String IBEIS_DB_path = CommonConfiguration.getProperty("IBEIS_DB_path", context);
		if (IBEIS_DB_path == null) {
			System.out.println("Please set IBEIS_DB_path in CommonConfiguration");
			IBEIS_DB_path = "/tmp/broken/path";
		}

		try {
			int found = -1;
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:" + IBEIS_DB_path);
			st = c.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM encounters");
			while (rs.next()) {
				UUID uuid = bytesToUUID(rs.getBytes("encounter_uuid"));
				System.out.println("[" + uuid.toString() + "] " + rs.getString("encounter_text"));
				if (uuid.toString().equals(eid)) found = rs.getInt("encounter_rowid");
//TODO drop out once found
			}
			rs.close();
			st.close();

System.out.println("found="+found);

			if (found > -1) {
				st = c.createStatement();
				HashMap<Integer, HashMap> imgs = new HashMap();
				rs = st.executeQuery("SELECT * FROM encounter_image_relationship JOIN images USING (image_rowid) WHERE encounter_rowid=" + found);
				while (rs.next()) {
					HashMap img = new HashMap();
					int iid = rs.getInt("image_rowid");
					img.put("id", iid);
					img.put("image_uri", rs.getString("image_uri"));
					img.put("image_height", rs.getInt("image_height"));
					img.put("image_width", rs.getInt("image_width"));
					img.put("image_gps_lat", rs.getFloat("image_gps_lat"));
					img.put("image_gps_lon", rs.getFloat("image_gps_lon"));
					img.put("image_time_posix", rs.getInt("image_time_posix"));
					imgs.put(iid, img);
					//imgIDs.add(rs.getInt("image_rowid"));
				}
				rs.close();
				st.close();
//System.out.println("imgIDs =" + imgIDs.toString());

				HashMap anns = new HashMap();

				List<Integer> annIDs = new ArrayList<Integer>();
				for (int iid : imgs.keySet()) {
System.out.println("IMG ID >>"+iid);
					st = c.createStatement();
					rs = st.executeQuery("SELECT * FROM annotations WHERE image_rowid=" + iid);
					while (rs.next()) {
System.out.println(" .... aid=" + rs.getInt("annot_rowid"));
						HashMap ann = new HashMap();
						int aid = rs.getInt("annot_rowid");
						ann.put("id", aid);

				UUID uuid = bytesToUUID(rs.getBytes("annot_uuid"));
				ann.put("annot_uuid", uuid.toString());

						ann.put("image_id", iid);
						anns.put(aid, ann);
						annIDs.add(aid);
					}
					rs.close();
					st.close();
System.out.println("(END IMG ID)<<"+iid);
				}

				for (int aid : annIDs) {
					st = c.createStatement();
					rs = st.executeQuery("SELECT lblannot_value, lbltype_rowid FROM lblannot JOIN annotation_lblannot_relationship USING (lblannot_rowid) WHERE annot_rowid=" + aid);
					while (rs.next()) {
System.out.println(aid + ": value = " + rs.getString("lblannot_value"));
						Object o = anns.get(aid);
						HashMap h = (HashMap)o;
						h.put("label_" + rs.getInt("lbltype_rowid"), rs.getString("lblannot_value"));
					}
					rs.close();
					st.close();
				}

/*
System.out.println("======================================================================================= done");
System.out.println(anns);
System.out.println("=======================================================================================");
System.out.println(imgs);
System.out.println("=======================================================================================");
*/

				rtn.put("imgs", imgs);
				rtn.put("anns", anns);
			}

			c.close();

    } catch ( Exception e ) {
      System.err.println("db error: " + e.getClass().getName() + ": " + e.getMessage() );
    }

    return rtn;
	}

	//used by bytesToUUID below
	public long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(64);
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getLong();
	}

	//takes in input 16-byte array and returns a UUID.  it breaks the 16-byte array into two longs to do this (due to constructor of UUID)
	public UUID bytesToUUID(byte[] bytesIn) {
		//not sure why only the *first* long is all flipped around, but it is. second is not.  otherwise the below single line would work. :/
		//Long a = bytesToLong(Arrays.copyOfRange(bytesIn, 0, 8));
		byte[] byteA1 = new byte[8];
		byte[] byteA2 = Arrays.copyOfRange(bytesIn, 0 ,8);
		byteA1[0] = byteA2[3];
		byteA1[1] = byteA2[2];
		byteA1[2] = byteA2[1];
		byteA1[3] = byteA2[0];
		byteA1[4] = byteA2[5];
		byteA1[5] = byteA2[4];
		byteA1[6] = byteA2[7];
		byteA1[7] = byteA2[6];
		Long a = bytesToLong(byteA1);

		Long b = bytesToLong(Arrays.copyOfRange(bytesIn, 8, 16));
		return new UUID(a,b);
	}

}


