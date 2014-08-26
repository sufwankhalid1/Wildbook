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
import java.io.IOException;
import java.io.PrintWriter;


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

		HttpSession session = request.getSession(false);
    String context="context0";
    context = ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
		String rootDir = getServletContext().getRealPath("/");

    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

System.out.println("yes? starting proc");
BatchCompareProcessor proc = new BatchCompareProcessor();
Thread t = new Thread(proc);
t.start();
System.out.println("yes. out.");

		List<FileItem> formFiles = new ArrayList<FileItem>();

  	//Calendar date = Calendar.getInstance();

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

		} else {
			//doneMessage = "Sorry this Servlet only handles file upload request";
		}

		String msg = "";
		if (formFiles.size() > 0) {
			//String baseDir = ServletUtilities.dataDir(context, rootDir);
String baseDir = "/tmp/updir";
			for (FileItem item : formFiles) {
				File dir = new File(baseDir);

				//String origFilename = new File(item.getName()).getName();
				String filename = ServletUtilities.cleanFileName(new File(item.getName()).getName());
				File file = new File(baseDir, filename);
    		//String fullPath = file.getAbsolutePath();
System.out.println(filename + " -> " + file.toString());
				try {
					item.write(file);  //TODO catch errors and return them, duh
				} catch (Exception ex) {
					System.out.println("could not write file: " + ex.toString());
				}
msg += filename + " ";
			}
    }

		out.println(ServletUtilities.getHeader(request));
		out.println("<p>" + msg + "</p>");
		out.println(ServletUtilities.getFooter(context));
		out.close();
  }


}



