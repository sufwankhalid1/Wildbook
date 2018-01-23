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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private static String urlNameGraphStatus = "IBEISIARestUrlGraphStatus";

    private static String CALLBACK_GRAPH_START_REVIEW = "graph_start_review";
    private static String CALLBACK_GRAPH_START_FINISHED = "graph_start_finished";
    private static String CALLBACK_GRAPH_REVIEW_FORM = "graph_review_form";

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
            String qstring = "?graph_uuid=" + Comm.toFancyUUID(infrId) + "&callback_url=" + Comm.callbackUrlString(context, "&" + CALLBACK_GRAPH_REVIEW_FORM + "&gid=" + infrId.toString());
System.out.println("qstring ===> " + qstring);
            URL u = new URL(Comm.getUrl(urlNameReviewIdentify, context), qstring);
System.out.println("nextGraphReview() -> " + u);
            rtn = Comm.get(u);
        } catch (Exception ex) {
            System.out.println("nextGraphReview() ERROR: " + ex.toString());
            throw new RuntimeException(ex.toString());  //this is kinda dumb but...
        }
//System.out.println("nextGraphReview() rtn => " + rtn);
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

    //this gets all graphs and their pending review counts (via .response)
    public static JSONObject graphStatus(String context) throws RuntimeException, MalformedURLException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        JSONObject rtn = null;
        try {
            rtn = Comm.get(Comm.getUrl(urlNameGraphStatus, context));
        } catch (Exception ex) {
            throw new RuntimeException(ex.toString());  //this is kinda dumb but...
        }
System.out.println("graphStatus() rtn => " + rtn);
        return rtn;
    }

/*

{
    status: {
        cache: -1,
        success: true,
        message: "",
        code: 200
    },
    response: {
        aa62d822-4fd8-c046-a7ea-569bbd277f55: {
            status: "Waiting (Empty Queue)",
            num_aids: 289,
            num_reviews: 2
        }
    }
}

*/

    //just results, or null if trouble
    public static JSONObject graphStatusResults(String context) {
        try {
            JSONObject rtn = graphStatus(context);
            if ((rtn.optJSONObject("status") == null) || !rtn.getJSONObject("status").optBoolean("success", false)) {
                System.out.println("WARNING: graphStatusResults() .status.success not true");
                return null;
            }
            return rtn.optJSONObject("response");
        } catch (Exception ex) {
            System.out.println("WARNING: graphStatusResults() got exception: " + ex.toString());
            return null;
        }
    }


    public static void processCallback(HttpServletRequest request, HttpServletResponse response) throws java.net.MalformedURLException, java.io.IOException {
        String qstr = request.getQueryString();
//String gid = request.getParameter("gid");
System.out.println("processCallback() qstr => " + qstr);
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
        JSONObject rtn = new JSONObject("{\"success\": false}");
        String gid = null;
        Pattern pat = Pattern.compile("&gid=([0-9a-f-]+)");
        Matcher mat = pat.matcher(request.getQueryString());  //note: cant use request.getParameter() as it messes up passthru!!!
        if (mat.find()) gid = mat.group(1);
System.out.println(">>>> gid=" + gid);
        if (gid == null) {
            rtn.put("error", "could not recover gid from request query string");
        } else {
            URL u = new URL(Comm.getUrl(urlNameReviewIdentify, context), "?graph_uuid=" + Comm.toFancyUUID(gid));
            //URL u = Comm.getUrl(urlNameReviewIdentify, context);
System.out.println("attempting passthru to " + u);
            try {
                rtn = RestClient.postStream(u, request.getInputStream());
            } catch (Exception ex) {
                rtn.put("error", ex.toString());
            }
        }
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println(rtn.toString());
        out.close();
    }


}
