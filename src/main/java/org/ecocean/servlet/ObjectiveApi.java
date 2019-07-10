/*
 * Wildbook - A Mark-Recapture Framework
 * Copyright (C) 2011-2018 Jason Holmberg
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.jdo.Query;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.Shepherd;
import org.ecocean.Objective;
import org.ecocean.Annotation;
import org.ecocean.User;
import org.ecocean.Util;
import org.ecocean.Encounter;
import org.ecocean.media.Feature;
import org.ecocean.media.MediaAsset;
import org.ecocean.MarkedIndividual;
import org.ecocean.AccessControl;
import org.json.JSONObject;
import org.json.JSONArray;

/*
import org.apache.commons.lang3.StringUtils;
import org.ecocean.MarkedIndividual;
import org.ecocean.CommonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
*/


public class ObjectiveApi extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }


    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        String context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
        myShepherd.beginDBTransaction();
        JSONObject jsonIn = null;
        try {
            jsonIn = ServletUtilities.jsonFromHttpServletRequest(request);
        } catch (Exception ex) {
            jsonIn = new JSONObject();
            jsonIn.put("id", request.getParameter("id"));
        }

        String rtn = process(jsonIn, myShepherd, request);

        myShepherd.rollbackDBTransaction();

        PrintWriter out = response.getWriter();
        response.setContentType("text/json");
        out.println(rtn);
        out.close();
    }

    public String process(JSONObject jsonIn, Shepherd myShepherd, HttpServletRequest request) {
        if (jsonIn == null) throw new RuntimeException("null jsonIn");

        User user = AccessControl.getUser(request, myShepherd);
        boolean isAdmin = false;
        if (user != null) isAdmin = myShepherd.doesUserHaveRole(user.getUsername(), "admin", myShepherd.getContext());

        JSONObject rtn = new JSONObject("{\"success\": false}");
        if (user == null) {
            rtn.put("error", "current design requires login");
            return rtn.toString();
        }

        String id = jsonIn.optString("id", null);
        rtn.put("_currentServerTime", System.currentTimeMillis());

        //we should have other options here, like timeframes or completeness, IDs only, etc...
        if (id == null) {  //listing
            String jdoql = "SELECT FROM org.ecocean.Objective";
            //TODO make it so admin can see all users
            jdoql += " WHERE owner.uuid == '" + user.getUUID() + "'";
            
            Query query = myShepherd.getPM().newQuery(jdoql);
            query.setOrdering("created");  //modified?
            //query.setRange(......)
            Collection c = (Collection) (query.execute());
            List<Objective> objs = new ArrayList<Objective>(c);
            query.closeAll();
            JSONArray jo = new JSONArray();
            for (Objective obj : objs) {
                jo.put(obj.toJSONObject());
            }
            rtn.put("objectives", jo);
            rtn.put("success", true);
            return rtn.toString();
        }

        Objective obj = Objective.load(id, myShepherd);
        if (obj == null) {
            rtn.put("error", "invalid Objective id=" + id);
        } else {
            rtn.put("success", true);
            rtn.put("objective", obj.toJSONObject());
        }

/*
        if (rtn.optBoolean("success", false)) {
            myShepherd.commitDBTransaction();
        } else {
            myShepherd.rollbackDBTransaction();
        }
*/

        return rtn.toString();
    }
}
