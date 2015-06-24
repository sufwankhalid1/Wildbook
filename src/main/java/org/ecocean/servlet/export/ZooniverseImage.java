package org.ecocean.servlet.export;

/*
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
*/
import java.io.IOException;
import java.io.PrintWriter;
/*
import java.io.OutputStream;
import java.io.OutputStreamWriter;
//import java.util.StringTokenizer;
import java.util.Vector;
*/
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.*;
import org.ecocean.servlet.ServletUtilities;

public class ZooniverseImage extends HttpServlet{

  public void init(ServletConfig config) throws ServletException {
      super.init(config);
    }

  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
      doPost(request, response);
  }
    


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //now write out the file
        response.setContentType("text/plain");
        //response.setHeader("Content-Disposition","attachment;filename="+inpFilename);
        //ServletContext ctx = getServletContext();
        //InputStream is = ctx.getResourceAsStream("/encounters/"+emailFilename);
        PrintWriter out = response.getWriter();

        String[] contexts = request.getParameterValues("context");
        if ((contexts == null) || (contexts.length < 1)) {
            out.println("error: no context=XXX passed");
            return;
        }

        HashMap<String,String> imgs = new HashMap<String,String>();
        for (int i = 0 ; i < contexts.length ; i++) {
            Shepherd myShepherd = new Shepherd(contexts[i]);
            Iterator all_spv = myShepherd.getAllSinglePhotoVideosNoQuery();
            while (all_spv.hasNext()) {
                SinglePhotoVideo spv = (SinglePhotoVideo)all_spv.next();
                imgs.put(spv.getFilename(), contexts[i] + ":" + spv.getDataCollectionEventID()); //this means the later contexts will trump existing images
            }
        }
        for (String fname : imgs.keySet()) {
            out.println(imgs.get(fname) + "\t" + fname);
        }
        out.close(); 

    }
  
}
