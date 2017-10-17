package org.ecocean.servlet;

import org.ecocean.*;
import org.ecocean.media.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;

import org.json.JSONObject;
import org.json.JSONException;




public class MediaAssetModify extends HttpServlet {
  /** SLF4J logger instance for writing log entries. */
  public static Logger log = LoggerFactory.getLogger(WorkspaceDelete.class);

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Allow-Methods", "GET, POST");
      if (request.getHeader("Access-Control-Request-Headers") != null) response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
      //response.setContentType("text/plain");
  }



  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setHeader("Access-Control-Allow-Origin", "*");  //allow us stuff from localhost

    String context="context0";
    context=ServletUtilities.getContext(request);
    String langCode = ServletUtilities.getLanguageCode(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("MediaAssetModify.class");
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    //boolean locked = false;

    JSONObject rtn = new JSONObject("{\"success\": \"false\"}");

    boolean isOwner = true;


    // ServletUtilities.informInterestedParties(request, request.getParameter("number"), message,context);
    myShepherd.beginDBTransaction();

    String[] ids = request.getParameterValues("id");
    if ((ids == null) || (ids.length < 1)) {
        throw new IOException("MediaAssetModify servlet requires at least one 'id' argument.");
    }

    JSONObject all = new JSONObject();  //each result, keyed by id
    for (int i = 0 ; i < ids.length ; i++) {
        String id = ids[i];
        JSONObject res = new JSONObject();
        MediaAsset ma = myShepherd.getMediaAsset(id);
        if (ma == null) {
            res.put("success", false);
            res.put("error", "could not load MediaAsset");
            all.put(id, res);
            continue;
        }

        res.put("success", true);  //just means MA loaded
        if (request.getParameter("lat")!=null) {
            Double d = Util.doubleFromString(request.getParameter("lat"));
            if (d == null) {
                res.put("lat", "error: could not convert double");
            } else {
                ma.setUserLatitude(d);
                res.put("lat", d);
            }
        }

        if (request.getParameter("long")!=null) {
            Double d = Util.doubleFromString(request.getParameter("long"));
            if (d == null) {
                res.put("long", "error: could not convert double");
            } else {
                ma.setUserLongitude(d);
                res.put("long", d);
            }
        }

        if (request.getParameter("datetime")!=null) {
            DateTime dt = null;
            try {
                dt = DateTime.parse(request.getParameter("datetime"));
            } catch (Exception ex) { };
            if (dt == null) {
                res.put("datetime", "error: could not parse DateTime");
            } else {
                ma.setUserDateTime(dt);
                res.put("datetime", dt);
            }
        }

        //for labels, we can have more than one
        String[] labelsToAdd = request.getParameterValues("labelAdd");
        if ((labelsToAdd != null) && (labelsToAdd.length > 0)) {
            String added = "";
            for (int j = 0 ; j < labelsToAdd.length ; j++) {
                if (labelsToAdd[j].startsWith("_")) continue;  //considered "system reserved"
                ma.addLabel(labelsToAdd[j]);
                added += labelsToAdd[j] + " ";
            }
            res.put("labelAdd", added);
        }

        String[] labelsToRemove = request.getParameterValues("labelRemove");
        if ((labelsToRemove != null) && (labelsToRemove.length > 0)) {
            String removed = "";
            for (int j = 0 ; j < labelsToRemove.length ; j++) {
                if (labelsToRemove[j].startsWith("_")) continue;  //considered "system reserved"
                ma.removeLabel(labelsToRemove[j]);
                removed += labelsToRemove[j] + " ";
            }
            res.put("labelRemove", removed);
        }

        all.put(id, res);
    }
    rtn.put("success", true);
    rtn.put("results", all);
    
/*
    } catch (Exception edel) {
      locked = true;
      log.warn("Failed to modify MediaAsset: " + request.getParameter("id"), edel);
      edel.printStackTrace();
      myShepherd.rollbackDBTransaction();
    }
*/

    myShepherd.commitDBTransaction();
    out.println(rtn.toString());
    out.close();
    myShepherd.closeDBTransaction();
  }
}
