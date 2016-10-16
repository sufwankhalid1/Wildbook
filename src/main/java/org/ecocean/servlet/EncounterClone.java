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
import java.util.List;

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
            //get the MediaAssets once
            List<MediaAsset> mas = new ArrayList<MediaAsset>();
            JSONArray jarr = jreq.optJSONArray("assets");
            if ((jarr != null) && (jarr.length() > 0)) {
                for (int i = 0 ; i < jarr.length() ; i++) {
                    MediaAsset ma = MediaAssetFactory.load(jarr.optInt(i, -1), myShepherd);
                    if (ma != null) mas.add(ma);
                }
            }

            String species = enc.getTaxonomyString();
            int number = jreq.optInt("number", 1);  //how many clones to make
            JSONArray encsMade = new JSONArray();

            for (int i = 0 ; i < number ; i++) {
                Encounter newEnc = enc.cloneWithoutAnnotations();
                newEnc.setComments((i+1) + " of " + number + " cloned from Encounter " + enc.getCatalogNumber());

                ArrayList<Annotation> anns = new ArrayList<Annotation>();
                for (MediaAsset ma : mas) {
                    anns.add(new Annotation(species, ma));
                }
                if (anns.size() > 0) newEnc.setAnnotations(anns);

                System.out.println("INFO: cloned " + newEnc.getCatalogNumber() + " (" + (i+1) + " of " + number + ") from " + enc + " including: " + anns);
                myShepherd.getPM().makePersistent(newEnc);
                encsMade.put(newEnc.getCatalogNumber());
            }
            myShepherd.commitDBTransaction();

            res.remove("error");
            res.put("success", true);
            res.put("encounterIds", encsMade);
        }
    }
    PrintWriter out = response.getWriter();
    out.println(res.toString());
    out.close();
    myShepherd.closeDBTransaction();
  }

}
