<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.apache.shiro.crypto.*,org.apache.shiro.util.*,org.apache.shiro.crypto.hash.*,org.ecocean.*,org.ecocean.servlet.ServletUtilities,
java.util.Properties,
javax.jdo.*,
java.util.List,
java.io.File,
org.ecocean.media.MediaSubmission,
java.util.ArrayList" %>


<html>
<head>
	<title>batch kickoff</title>
</head>

<body>
hello.

<%

System.out.println("hello?");


/*
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




public class BatchCompare extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
 

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
*/

    //response.setContentType("text/html");

		//HttpSession session = request.getSession(false);
    String context="context0";
    context = ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
		PersistenceManager pm = myShepherd.getPM();
		String rootDir = getServletContext().getRealPath("/");
System.out.println(rootDir);

		String[] files = request.getParameterValues("f");
		String batchID = Util.generateUUID();
System.out.println(batchID);
System.out.println(files);
		if ((files != null) && (files.length > 0)) {
			String baseDir = ServletUtilities.dataDir(context, rootDir) + "/match_images/" + batchID;
System.out.println(baseDir);
			File bd = new File(baseDir);
			if (!bd.exists()) bd.mkdirs(); //fwiw, should never exist

			BatchCompareProcessor.writeStatusFile(getServletContext(), context, batchID, "{ \"countComplete\": 0, \"countTotal\": " + Integer.toString(files.length) + " }");
					

			/*
			System.out.println("yes? starting proc");
					BatchCompareProcessor proc = new BatchCompareProcessor(getServletContext(), context, "npmCompare", okFiles, batchID);
					Thread t = new Thread(proc);
					t.start();
					session.setAttribute(BatchCompareProcessor.SESSION_KEY_COMPARE, proc);
			System.out.println("yes. out.");

					response.sendRedirect("http://" + CommonConfiguration.getURLLocation(request) + "/batchCompareDone.jsp?batchID=" + batchID);
			*/

		}

/*
		String langCode=ServletUtilities.getLanguageCode(request);
		Properties props = new Properties();
		props = ShepherdProperties.getProperties("submit.properties", langCode,context);

		String msg = "";
*/
/*
		if (formFiles.size() > 0) {


			for (FileItem item : formFiles) {
				//String origFilename = new File(item.getName()).getName();
				String filename = ServletUtilities.cleanFileName(new File(item.getName()).getName());
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
				if (ok) okFiles.add(file.getAbsolutePath());
			}
			msg = "<p>" + props.getProperty("batchCompareImagesReceived").replaceFirst("%d", Integer.toString(okFiles.size())) + "</p>";
			msg += "<p>" + props.getProperty("batchCompareNotFinished").replaceFirst("%countTotal", Integer.toString(okFiles.size())).replaceFirst("%countComplete", "0") + "</p>";
			msg += "<script>window.setTimeout(function() { window.location.href = 'batchCompareDone.jsp'; }, 8000);</script>";
*/

%>

end?

</body></html>

