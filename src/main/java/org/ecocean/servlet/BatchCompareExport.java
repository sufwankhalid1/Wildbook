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

public class BatchCompareExport extends HttpServlet {

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

		String langCode=ServletUtilities.getLanguageCode(request);
		Properties props = new Properties();
		props = ShepherdProperties.getProperties("submit.properties", langCode,context);

		String batchID = request.getParameter("batchID");
		String data = request.getParameter("data");

    PrintWriter out = response.getWriter();

		String batchDir = null;
		if ((batchID != null) && (batchID.indexOf(".") < 0)) batchDir = ServletUtilities.dataDir(context, rootDir) + "/match_images/" + batchID;

		if (batchDir == null) {
    	response.setContentType("text/json");
			out.println("{ \"error\": \"bad batchID\" }");

		} else if (data != null) {
System.out.println("data="+data);
			boolean ok = BatchCompareProcessor.writeStatusFile(getServletContext(), context, batchID, data);
    	response.setContentType("text/json");
			out.println("{ \"saved\": " + ok + "}");

		} else if (request.getParameter("export") != null) {
			try {
				PrintWriter statusOut = new PrintWriter(batchDir + "/export.txt");
				statusOut.println("pretend this is real content!");
				statusOut.close();
				String url = "http://" + request.getServerName();
				if (request.getServerPort() != 80) url += ":" + request.getServerPort();
				url += "/" + CommonConfiguration.getDataDirectoryName(context) + "/match_images/" + batchID + "/export.txt";
				response.sendRedirect(url);
			} catch (Exception ex) {
    		response.setContentType("text/plain");
				out.println("could not write " + batchDir + "/export.txt: " + ex.toString());
			}

		} else {
    	response.setContentType("text/json");
			out.println("{ \"error\": \"no data?\" }");
		}
/*
		out.println(ServletUtilities.getHeader(request));
		out.println(msg);
		out.println(ServletUtilities.getFooter(context));
*/
		out.close();
  }


}



