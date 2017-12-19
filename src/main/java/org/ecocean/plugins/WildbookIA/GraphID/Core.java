/*
 * Wildbook - A Mark-Recapture Framework
 * Copyright (C) 2017 Jason Holmberg
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

package org.ecocean.plugins.WildbookIA;

import java.util.UUID;
import java.net.URL;
import java.util.Collections;
import org.ecocean.Annotation;
import org.ecocean.Util;
import org.ecocean.RestClient;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.identity.IBEISIA;
import java.util.HashMap;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.builder.ToStringBuilder;

/*
    these constraints should be reflected (enforced) in the db -- hopefully we can do that via datanucleus setup???  TODO
*/
public class Core {

    private static String urlNameAdderAnnotmatch = "IBEISIARestUrlV2MatchAdder";
    private static String urlNameAdderStaging = "IBEISIARestUrlV2ReviewAdder";
    private static String urlNameReviewIdentify = "IBEISIARestUrlIdentifyV2Review";
    private static String urlNameStartIdentify = "IBEISIARestUrlStartIdentifyV2Annotations";

    private static String CALLBACK_GRAPH_START_REVIEW = "graph_start_review";
    private static String CALLBACK_GRAPH_START_FINISHED = "graph_start_finished";
    private static String CALLBACK_GRAPH_REVIEW_FORM = "graph_review_form";

/*
    //this gets a little hacky to handle also Staging.  :( 
    public static JSONObject sendToIA(List<Annotmatch> list, String context) {
        if ((list == null) || (list.size() < 1)) return null;
        boolean isStaging = (list.get(0) instanceof Staging);
        JSONObject rtn = null;
        HashMap<String,Object> data = null;
        try {
            rtn = Comm.post(isStaging ? urlNameAdderStaging : urlNameAdderAnnotmatch, context, adderData(list));
        } catch (Exception ex) {
        }
        return rtn;
    }

    protected static HashMap<String,Object> adderData(List<Annotmatch> list) {
        if ((list == null) || (list.size() < 1)) return null;
        boolean isStaging = (list.get(0) instanceof Staging);
        String prefix = isStaging ? "review" : "match";
        HashMap<String,Object> map = new HashMap<String,Object>();
        List<JSONObject> a1 = new ArrayList<JSONObject>();
        List<JSONObject> a2 = new ArrayList<JSONObject>();
        List<String> ed = new ArrayList<String>();

        for (Annotmatch am : list) {
            a1.add(Comm.toFancyUUID(am.getAnnot1().getId()));
            a2.add(Comm.toFancyUUID(am.getAnnot2().getId()));
            ed.add(am.getEvidenceDecision());
        }

        map.put(prefix + "_annot_uuid1_list", a1);
        map.put(prefix + "_annot_uuid2_list", a2);
        map.put(prefix + "_evidence_decision_list", ed);
        return map;
    }

*/

    public static JSONObject startGraph(List<Annotation> annots, String context) throws RuntimeException, MalformedURLException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        HashMap<String,Object> map = new HashMap<String,Object>();
        //TODO handle null annots cuz we dont sendAnnotations(all) 
        if ((annots != null) && (annots.size() > 0)) {
            JSONObject sentResult = IBEISIA.sendAnnotations(new ArrayList<Annotation>(annots), context);
            List<JSONObject> alist = new ArrayList<JSONObject>();
            for (Annotation ann : annots) {
                alist.add(Comm.toFancyUUID(ann.getId()));
            }
            map.put("annot_uuid_list", alist);
        }
        map.put("review_callback_url", Comm.callbackUrlString(context, "&" + CALLBACK_GRAPH_START_REVIEW));
        map.put("finished_callback_url", Comm.callbackUrlString(context, "&" + CALLBACK_GRAPH_START_FINISHED));
System.out.println("startGraph() map => " + map);
        JSONObject rtn = Comm.post(urlNameStartIdentify, context, map);
System.out.println("startGraph() rtn => " + rtn);
//////TODO set INFR_UUID yes????
        return rtn;
    }

//// 'http://lev.cs.rpi.edu:5005/api/review/query/graph/v2/?graph_uuid={"__UUID__":"04584cfa-a361-f0e5-fc4e-1da8dcf93e62"}&callback_url=http://example.com/foo'

    /*
       notes on return code (which will show up as exceptions, e.g. java.lang.RuntimeException: Failed : HTTP error code : 602)
       602 = invalid/unknown id
    */

    public static String nextGraphReview(UUID infrId, String context) throws RuntimeException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        JSONObject rtn = null;
        try {
            String qstring = "?graph_uuid=" + Comm.toFancyUUID(infrId) + "&callback_url=" + Comm.callbackUrlString(context, "&" + CALLBACK_GRAPH_REVIEW_FORM);
            URL u = new URL(Comm.getUrl(urlNameReviewIdentify, context), qstring);
System.out.println("nextGraphReview() -> " + u);
            rtn = Comm.get(u);
        } catch (Exception ex) {
            throw new RuntimeException(ex.toString());  //this is kinda dumb but...
        }
System.out.println("nextGraphReview() rtn => " + rtn);
        if (rtn == null) return null; //TODO RuntimeException instead?
        return rtn.optString("response", null);  //TODO ditto above... exception if no "response" ?
    }

    public static JSONObject syncGraph(UUID infrId, String context) throws RuntimeException, MalformedURLException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        JSONObject rtn = null;
        try {
            String qstring = "?query_uuid=" + infrId.toString();
            URL u = new URL(Comm.getUrl(urlNameStartIdentify, context), qstring);
            rtn = Comm.get(u);
        } catch (Exception ex) {
            throw new RuntimeException(ex.toString());  //this is kinda dumb but...
        }
System.out.println("syncGraph() rtn => " + rtn);
        return rtn;
    }

    public static void processCallback(HttpServletRequest request, HttpServletResponse response) throws java.net.MalformedURLException, java.io.IOException {
        String qstr = request.getQueryString();
        if (qstr == null) throw new RuntimeException("null query string!");  //extremely unlikely to ever happen here
        if (qstr.indexOf(CALLBACK_GRAPH_REVIEW_FORM) > -1) {
            processCallbackGraphReviewForm(request, response);
        } else {
            System.out.println("WARNING: Annotmatch.processCallback() failed to do anything with qstr=" + qstr);
        }
    }

    //individual helpers from above
    private static void processCallbackGraphReviewForm(HttpServletRequest request, HttpServletResponse response) throws java.net.MalformedURLException, java.io.IOException {
        //note: cannot use getContext() -- it messes up postStream()!  GRRRRRR  FIXME
        //String context = ServletUtilities.getContext(request);
        String context = "context0";
UUID id = UUID.fromString("037e720b-9753-7bdc-8c64-2e234d196f82");  //TODO make this real (id per species)
        URL u = new URL(Comm.getUrl(urlNameReviewIdentify, context), "?graph_uuid=" + Comm.toFancyUUID(id));
        //URL u = Comm.getUrl(urlNameReviewIdentify, context);
System.out.println("attempting passthru to " + u);
        JSONObject rtn = new JSONObject("{\"success\": false}");
        try {
            rtn = RestClient.postStream(u, request.getInputStream());
        } catch (Exception ex) {
            rtn.put("error", ex.toString());
        }
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println(rtn.toString());
        out.close();
    }


}
