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

import java.util.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.*;

import org.ecocean.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import com.google.gson.Gson;

import java.net.URL;
import java.net.URLConnection;

/*
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
*/

public class BatchCompare extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
 

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();

		boolean fromRemote = false;

		String[] remoteFiles = request.getParameterValues("file");

		HttpSession session = request.getSession(false);
    String context="context0";
    context = ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
		String rootDir = getServletContext().getRealPath("/");

		String langCode=ServletUtilities.getLanguageCode(request);
		Properties props = new Properties();
		props = ShepherdProperties.getProperties("submit.properties", langCode,context);

		String batchID = Util.generateUUID();

		if (request.getParameter("batchID") != null) batchID = request.getParameter("batchID");

		List<FileItem> formFiles = new ArrayList<FileItem>();

		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
				upload.setHeaderEncoding("UTF-8");
				List<FileItem> multiparts = upload.parseRequest(request);

				for(FileItem item : multiparts){
					if (item.isFormField()) {  //plain field
						//TODO ?

					} else {  //file
						if (myShepherd.isAcceptableImageFile(item.getName()) || myShepherd.isAcceptableVideoFile(item.getName()) ) {
							formFiles.add(item);
						}
					}
				}

			} catch (Exception ex) {
				//doneMessage = "File Upload Failed due to " + ex;
			}

		//special case, we grab the files and start processing them
		} else if ((remoteFiles != null) && (remoteFiles.length > 0)) {
			List<String> gotFiles = new ArrayList<String>();
			String batchDir = ServletUtilities.dataDir(context, rootDir) + "/match_images/" + batchID;
			File bd = new File(batchDir);
			if (!bd.exists()) bd.mkdirs();
			Map<String,List<String>> fileMapEmpty = new HashMap<String,List<String>>();
			for (int i = 0 ; i < remoteFiles.length ; i++) {
				String got = getFile(batchDir, remoteFiles[i]);
				if (got != null) {
					fileMapEmpty.put(got, null);
					gotFiles.add(got);
				}
			}
			response.addHeader("Access-Control-Allow-Origin", "*");
    	response.setContentType("application/json");
    	PrintWriter out = response.getWriter();
			if (gotFiles.size() > 0) {
				BatchCompareProcessor.writeStatusFile(getServletContext(), context, batchID, "{ \"images\": " + gson.toJson(gotFiles) + ", \"countComplete\": 0, \"countTotal\": " + Integer.toString(gotFiles.size()) + " }");
				startCompare(context, fileMapEmpty, batchID);
				HashMap<String,Object> j = new HashMap<String,Object>();
				j.put("url", "http://" + CommonConfiguration.getURLLocation(request) + "/batchCompareDone.jsp?batchID=" + batchID);
				j.put("batchID", batchID);
				j.put("files", gotFiles);
				out.println(gson.toJson(j));
			} else {
				out.println("{\"error\": \"no files\"}");
			}
			out.close();
			return;

		} else {  //must already have files and this is what to search against
			String batchDir = null;
			if ((batchID != null) && (batchID.indexOf(".") < 0)) batchDir = ServletUtilities.dataDir(context, rootDir) + "/match_images/" + batchID;
			String jsonText = new String(Files.readAllBytes(Paths.get(batchDir + "/status.json")));
			Map<String,Object> d = new HashMap<String,Object>();
			d = (Map<String,Object>) gson.fromJson(jsonText, d.getClass());
System.out.println(d);
			List<String> fnames = (ArrayList<String>)d.get("images");
System.out.println(fnames);

			Map<String,List<String>> fileMap = new HashMap<String,List<String>>();
			if (fnames != null) {
				for (int i = 0 ; i < fnames.size() ; i++) {
					String f = request.getParameter("img-" + i + "-category");
					if ((f != null) && !f.equals("")) {
						List<String> inds = new ArrayList<String>();
						inds.add(f);
						fileMap.put(fnames.get(i), inds);
					} else {
						fileMap.put(fnames.get(i), null);
					}
				}

			} else {
System.out.println("json .images was empty ... hmm... bailing");
				response.sendRedirect("http://" + CommonConfiguration.getURLLocation(request) + "/batchCompareDone.jsp?batchID=" + batchID);
				return;
			}

System.out.println("NOW DOING FOLLOWUP ON " + batchID);
System.out.println(fileMap);

			startCompare(context, fileMap, batchID);

			BatchCompareProcessor.writeStatusFile(getServletContext(), context, batchID, "{ \"filters\": true, \"images\": " + gson.toJson(fnames) + ", \"countComplete\": 0, \"countTotal\": " + Integer.toString(fnames.size()) + " }");

		response.sendRedirect("http://" + CommonConfiguration.getURLLocation(request) + "/batchCompareDone.jsp?batchID=" + batchID);
return;


		}

		List<String> okFiles = new ArrayList<String>();
		if (formFiles.size() > 0) {
			List<String> fnames = new ArrayList<String>();
			String baseDir = ServletUtilities.dataDir(context, rootDir) + "/match_images/" + batchID;
			File bd = new File(baseDir);
			if (!bd.exists()) bd.mkdirs(); //fwiw, should never exist

			Map<String,List<String>> fileMapEmpty = new HashMap<String,List<String>>();  //we use this when we are fromRemote -- cuz no second step is done
			for (FileItem item : formFiles) {
				//String origFilename = new File(item.getName()).getName();
				String filename = ServletUtilities.cleanFileName(new File(item.getName()).getName());
				fnames.add(filename);
				File file = new File(baseDir, filename);
    		//String fullPath = file.getAbsolutePath();
System.out.println(filename + " -> " + file.toString());
				boolean ok = true;
				try {
					item.write(file);  //TODO catch errors and return them, duh
				} catch (Exception ex) {
					System.out.println("could not write file: " + ex.toString());
					ok = false;
				}
				if (ok) {
					okFiles.add(file.getAbsolutePath());
					fileMapEmpty.put(filename, null);
				}
			}

			BatchCompareProcessor.writeStatusFile(getServletContext(), context, batchID, "{ \"images\": " + gson.toJson(fnames) + ", \"countComplete\": 0, \"countTotal\": " + Integer.toString(okFiles.size()) + " }");

			//set up for response
			if (fromRemote) {
				startCompare(context, fileMapEmpty, batchID);
    		response.setContentType("application/json");
    		PrintWriter out = response.getWriter();
				HashMap<String,Object> j = new HashMap<String,Object>();
				j.put("url", "http://" + CommonConfiguration.getURLLocation(request) + "/batchCompareDone.jsp?batchID=" + batchID);
				j.put("batchID", batchID);
				j.put("files", okFiles);
				out.println(gson.toJson(j));
				out.close();

			} else {
				response.sendRedirect("http://" + CommonConfiguration.getURLLocation(request) + "/batchCompareDone.jsp?batchID=" + batchID);
			}

		}

  }


	private void startCompare(String context, Map<String,List<String>> fileMap, String batchID) {
System.out.println("yes? starting proc");
		BatchCompareProcessor proc = new BatchCompareProcessor(getServletContext(), context, "npmCompare", fileMap, batchID);
		Thread t = new Thread(proc);
		t.start();
		//session.setAttribute(BatchCompareProcessor.SESSION_KEY_COMPARE, proc);
System.out.println("yes. out.");
	}

	private String getFile(String targetDir, String u) {
		String filename = "tmp";
		int i = u.lastIndexOf("/");
		if (i > -1) filename = u.substring(i + 1);
System.out.println("getFile( "+ u + "->" + targetDir + " (" + filename);

		try {
			URL url = new URL(u);
			URLConnection conn = url.openConnection();
			InputStream input = conn.getInputStream();
			byte[] buffer = new byte[4096];
			int n = -1;
			OutputStream output = new FileOutputStream(new File(targetDir, filename));
			while ((n = input.read(buffer)) != -1) {
				output.write(buffer, 0, n);
			}
			output.close();
		} catch (Exception ex) {
			return null;
		}

		return filename;
	}
}



