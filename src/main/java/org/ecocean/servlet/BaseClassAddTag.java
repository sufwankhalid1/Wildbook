package org.ecocean.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.CommonConfiguration;
import org.ecocean.Encounter;
import org.ecocean.FoundationalPropertiesBase;
import org.ecocean.Measurement;
import org.ecocean.Occurrence;
import org.ecocean.Shepherd;
import org.ecocean.tag.AcousticTag;
import org.ecocean.tag.DigitalArchiveTag;
import org.ecocean.tag.MetalTag;
import org.ecocean.tag.SatelliteTag;

public class BaseClassAddTag extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd=new Shepherd(context);
    myShepherd.setAction("BaseClassAddTag.class");
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked=false;

    String objectID="None";
    String parentType = request.getParameter("parentType");
    objectID=request.getParameter("number");
    
    FoundationalPropertiesBase target = null;
    myShepherd.beginDBTransaction();
    if (parentType==null||parentType.equals("Encounter")) {
      target = (Encounter) target;
      target = myShepherd.getEncounter(objectID);
    }  else if (myShepherd.isOccurrence(objectID)&&parentType.equals("Occurrence")) {
      target = (Occurrence) target;
      target = myShepherd.getOccurrence(objectID);
    } else {
      out.println("Failed to retrieve and appropriate Object to store a new Tag.");
    }
    String tagType = null;
    String serialNumber = null;
    String location = null;
    String tagID = null;
    try {
      tagType = request.getParameter("tagType");
      serialNumber = request.getParameter("serialNumber");
      location = request.getParameter("tagLocation");
      tagID = request.getParameter("tagID");
      if ("metal".equals(tagType)) {
        MetalTag metalTag = new MetalTag();
        target.addBaseMetalTag(metalTag);
        metalTag.setLocation(location);
        metalTag.setId(tagID);
        metalTag.setTagNumber(tagID);
      } else if ("acoustic".equals(tagType)) {
        AcousticTag acousticTag = new AcousticTag();
        target.addBaseAcousticTag(acousticTag);
        acousticTag.setIdNumber(tagID);
        acousticTag.setId(tagID);
        acousticTag.setSerialNumber(serialNumber);
      } else if ("satellite".equals(tagType)) {
        SatelliteTag satelliteTag = null;
        satelliteTag = new SatelliteTag();
        target.addBaseSatelliteTag(satelliteTag);
        satelliteTag.setSerialNumber(serialNumber);
        satelliteTag.setName(tagID);
        satelliteTag.setId(tagID);
      } else if ("dtag".equals(tagID)) {
        DigitalArchiveTag dat = new DigitalArchiveTag();
        target.addBaseDigitalArchiveTag(dat);
        dat.setId(tagID);
        dat.setSerialNumber(serialNumber);
        dat.setDTagID(tagID);
      }
      
    } catch(Exception ex) {
      ex.printStackTrace();
      locked = true;
      myShepherd.rollbackDBTransaction();
      myShepherd.closeDBTransaction();
    }
    if (!locked) {
      myShepherd.commitDBTransaction();
      myShepherd.closeDBTransaction();
      out.println(ServletUtilities.getHeader(request));
      out.println("<p><strong>Success!</strong> I have successfully set the following tag values:</p><br/>");
      out.println("<small><strong>Type:</strong>"+tagType+"</small>");
      if (serialNumber!=null) {
        out.println("<small><strong>Serial Number:</strong>"+serialNumber+"</small>");        
      }
      if (location!=null) {
        out.println("<small><strong>Location:</strong>"+location+"</small>");        
      }
      if (tagID!=null) {
        out.println("<small><strong>Tag ID:</strong>"+tagID+"</small>");        
      }
      if (parentType.equals("Encounter")) {
        out.println("<p><a href=\""+request.getScheme()+"://"+CommonConfiguration.getURLLocation(request)+"/encounters/encounter.jsp?number="+objectID+"\">Return to encounter "+objectID+"</a></p>\n");        
      } else {
        out.println("<p><a href=\""+request.getScheme()+"://" + CommonConfiguration.getURLLocation(request) + "/occurrence.jsp?number=" + request.getParameter("number") + "\">Return to occurrence " + request.getParameter("number") + "</a></p>\n");
      }
      out.println(ServletUtilities.getFooter(context));
    } else {
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Failure!</strong> This encounter is currently being modified by another user, or an exception occurred. Please wait a few seconds before trying to modify this encounter again.");
      out.println(ServletUtilities.getFooter(context));
    }
      
    out.close();
    myShepherd.closeDBTransaction();
  }

  private String getParam(HttpServletRequest request, String paramName) {
    String value = request.getParameter(paramName);
    if (value != null) {
      value = value.trim();
      if (value.length() == 0){
        value = null;
      }
    }
    return value;
  }

}