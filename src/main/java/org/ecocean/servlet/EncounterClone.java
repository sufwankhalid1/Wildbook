package org.ecocean.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;

import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.*;

public class EncounterClone extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      ServletUtilities.doOptions(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setHeader("Access-Control-Allow-Origin", "*");  //allow us stuff from localhost
    JSONObject jreq = ServletUtilities.jsonFromHttpServletRequest(request);
    JSONObject res = new JSONObject("{\"success\": false, \"error\": \"unknown\"}");
    String context = ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.beginDBTransaction();

    String id = jreq.optString("id", null);
    if (id == null) {
        res.put("error", "no Encounter id passed");
    } else {
        Encounter enc = myShepherd.getEncounter(id);
        if (enc == null) {
            res.put("error", "unknown Encounter id " + id);
        } else {
            //Encounter newEnc = enc.cloneWithoutAnnotations();
            //newEnc.setIndividualID(enc.getIndividualID);  //TODO shouldnt some of this stuff be in .clone ?
            Encounter newEnc = enc.getClone();
            newEnc.setCatalogNumber(Util.generateUUID());
            newEnc.setComments("cloned from Encounter " + enc.getCatalogNumber());
            newEnc.setDWCDateLastModified();

            ArrayList<Annotation> anns = new ArrayList<Annotation>();
            JSONArray jarr = jreq.optJSONArray("assets");
            String species = enc.getTaxonomyString();
            if ((jarr != null) && (jarr.length() > 0)) {
                for (int i = 0 ; i < jarr.length() ; i++) {
                    MediaAsset ma = MediaAssetFactory.load(jarr.optInt(i, -1), myShepherd);
                    if (ma == null) continue;
                    anns.add(new Annotation(species, ma));
                }
            }
            if (anns.size() > 0) newEnc.setAnnotations(anns);
            myShepherd.getPM().makePersistent(newEnc);
            myShepherd.commitDBTransaction();
            res.remove("error");
            res.put("success", true);
            res.put("newEncounterId", newEnc.getCatalogNumber());
        }
    }
    PrintWriter out = response.getWriter();
    out.println(res.toString());
    out.close();
    myShepherd.closeDBTransaction();
  }

}
