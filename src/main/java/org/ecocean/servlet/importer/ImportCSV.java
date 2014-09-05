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

package org.ecocean.servlet.importer;

//////
import java.io.*;
import java.util.*;
//import java.lang.*;
//import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.*;

import au.com.bytecode.opencsv.*;
/////

import org.ecocean.CommonConfiguration;
/*
import org.ecocean.Encounter;
import org.ecocean.Shepherd;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.User;
*/
import org.ecocean.*;
import org.ecocean.servlet.*;
import org.ecocean.tag.AcousticTag;
import org.ecocean.tag.MetalTag;
import org.ecocean.tag.SatelliteTag;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ImportCSV extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
 

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		HashMap fv = new HashMap();

		HttpSession session = request.getSession(true);
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
		//request.getSession()getServlet().getServletContext().getRealPath("/"));
		String rootDir = getServletContext().getRealPath("/");
		String baseDir = ServletUtilities.dataDir(context, rootDir);


    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

		String doneMessage = "";
		FileItem csvFileItem = null;
		String imageSourceDir = null;

		boolean emptyFirst = true;
		String batchID = "1234";


  	//Calendar date = Calendar.getInstance();

		List<String> rowErrors = new ArrayList<String>();
		List<String> rowSuccesses = new ArrayList<String>();

		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
				upload.setHeaderEncoding("UTF-8");
				List<FileItem> multiparts = upload.parseRequest(request);
				//List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);

				for(FileItem item : multiparts){
					if (item.isFormField()) {  //plain field
						if (item.getFieldName().equals("imageDir")) imageSourceDir = CommonConfiguration.getProperty("importCSVImageDirectory", context) + "/" + item.getString("UTF-8").trim();

					} else {  //file
						csvFileItem = item;
					}
				}

			} catch (Exception ex) {
				doneMessage = "File Upload Failed due to " + ex;
			}

		} else {
			doneMessage = "Sorry this Servlet only handles file upload request";
		}

		if ((imageSourceDir == null) || imageSourceDir.equals("")) {
			out.println(ServletUtilities.getHeader(request));
			out.println("<p>You must select an image source directory.</p>");
			out.println(ServletUtilities.getFooter(context));

		} else if (csvFileItem == null) {
			out.println(ServletUtilities.getHeader(request));
			out.println("<p>Import file failed. " + doneMessage + "</p>");
			out.println(ServletUtilities.getFooter(context));

		} else {
			File tmpFile = new File("/tmp/import-" + batchID);  //TODO context etc!
			
			try {
				csvFileItem.write(tmpFile);
			} catch (Exception ex) {
				out.println(ServletUtilities.getHeader(request));
				out.println("could not write out csv file: " + ex.toString());
				out.println(ServletUtilities.getFooter(context));
				return;
			}

			if (emptyFirst) {
				myShepherd.beginDBTransaction();
				myShepherd.getPM().newQuery(MarkedIndividual.class).deletePersistentAll();
				myShepherd.commitDBTransaction();

				myShepherd.beginDBTransaction();
				myShepherd.getPM().newQuery(Occurrence.class).deletePersistentAll();
				myShepherd.commitDBTransaction();

				//not sure why encounters are handled differently, but am just copying from DeleteAllDataPermanently
				Iterator allEncounters = myShepherd.getAllEncountersNoFilter();
				while (allEncounters.hasNext()) {
					Encounter enc = (Encounter)allEncounters.next();
					myShepherd.beginDBTransaction();
					myShepherd.throwAwayEncounter(enc);
					myShepherd.commitDBTransaction();
        }
			}

			CSVReader reader = new CSVReader(new FileReader(tmpFile), ',', CSVWriter.NO_QUOTE_CHARACTER);
			List<String[]> allLines = reader.readAll();
			int rowNum = 0;
			for (String[] f : allLines) {
				String datestring = f[1];

				String filename = f[7];
				File img = null;
				if ((filename != null) && !filename.equals("")) img = Util.findFileInDirectoryWithCache(filename, new File(imageSourceDir));

				//if the (source) image file doesnt exist, we just skip it... fail!
				if (img != null) {
//System.out.println("!!! found filename " + filename + " at: " + img.getAbsoluteFile().toString());
					String encID = null;  //currently does not exist at all in data
					String indivID = f[15];
// date = 1, 
System.out.println("enc(" + encID + "), indiv(" + indivID + ")");
					myShepherd.beginDBTransaction();
					Encounter enc = null;
					if ((encID != null) && !encID.equals("")) enc = myShepherd.getEncounter(encID);
					if (enc == null) {
						Calendar cal = Calendar.getInstance();
						Date d = null;
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/d/y H:m:s");
						try {
							d = simpleDateFormat.parse(datestring);
						} catch (Exception ex) {
							System.out.println("failed to parse datestring=" + datestring);
						}
						if (d != null) cal.setTime(d);
						enc = new Encounter(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), 0,"00","","","test","test@test.test",null);
						if ((encID == null) || encID.equals("")) encID = enc.generateEncounterNumber();
						enc.setEncounterNumber(encID);
						enc.setState("unapproved");
					}

					File targetDir = new File(enc.dir(baseDir));
					if (!targetDir.exists()) targetDir.mkdirs();
					File targetFile = new File(targetDir, filename);
					//Files.createSymbolicLink(targetFile.toPath(), img.toPath())
					Files.copy(img.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					SinglePhotoVideo spv = new SinglePhotoVideo(encID, targetFile);
System.out.println(img.toString() + " being set on enc=" + encID);
					enc.addSinglePhotoVideo(spv);

					enc.setAlternateID(batchID + "." + Integer.toString(rowNum));

					//now handle individual
					MarkedIndividual indiv = null;
					if ((indivID != null) && !indivID.equals("")) {
						indiv = myShepherd.getMarkedIndividual(indivID);
						if (indiv == null) {
							indiv = new MarkedIndividual(indivID, enc);
						}
						indiv.addEncounter(enc);
						//enc.assignToMarkedIndividual(indivID);
					}
					myShepherd.storeNewEncounter(enc, encID);
					if (indiv != null) myShepherd.storeNewMarkedIndividual(indiv);
System.out.println(encID + " -> " + filename);
					rowSuccesses.add(encID);

				} else {
					rowErrors.add(filename);
				}

				rowNum++;
				System.out.println("---");
			}
		}
					myShepherd.commitDBTransaction();
    			//myShepherd.closeDBTransaction();
		String h = "";
		if (rowSuccesses.size() < 1) {
			h += "<p><b>No successful imports.</b></p>";
		} else {
			h += "<p>Imported <b>" + rowSuccesses.size() + " records successfully</b>.</p>";
System.out.println("starting batch " + batchID);
			BatchCompareProcessor proc = new BatchCompareProcessor(getServletContext(), context, "npmProcess", rowSuccesses);
			session.setAttribute(BatchCompareProcessor.SESSION_KEY, proc);
			Thread t = new Thread(proc);
			t.start();
System.out.println("thread forked");
		}

		if (rowErrors.size() < 1) {
			h += "<p>No errors</p>";
		} else {
			h += "<p>Found <b>" + rowErrors.size() + " rows with error</b> (usually invalid or missing image files):<ul>";
			for (String fname : rowErrors) {
				h += "<li>" + fname + "</li>";
			}
			h += "</ul></p>";
		}
		h += "<p><a href=\"encounters/searchResults.jsp?state=unapproved\">List all encounters</a></p><p><a href=\"batchCompare.jsp\">Continue to upload of images to match</a></p>";

		out.println(ServletUtilities.getHeader(request));
		out.println(h);
		out.println(ServletUtilities.getFooter(context));
/*
      //if (request.getRemoteUser() != null) {




      //return a forward to display.jsp
      System.out.println("Ending data submission.");
      if (!spamBot) {
        response.sendRedirect("http://" + CommonConfiguration.getURLLocation(request) + "/confirmSubmit.jsp?number=" + encID);
      } else {
        response.sendRedirect("http://" + CommonConfiguration.getURLLocation(request) + "/spambot.jsp");
      }


    }  //end "if (fileSuccess)

    myShepherd.closeDBTransaction();
    //return null;
*/
  }


}



