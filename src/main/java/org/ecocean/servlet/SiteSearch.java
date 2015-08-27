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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.ShepherdPMF;
import org.ecocean.rest.SimpleFactory;
import org.ecocean.rest.SimpleIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.util.string.StringUtilities;


public class SiteSearch extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(SiteSearch.class);

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
        //set up for response
        response.setContentType("text/json");
        PrintWriter out = response.getWriter();

        String term = request.getParameter("term");
        if ((term == null) || term.equals("")) {
            out.println("[]");
            return;
        }

        try (Database db = ShepherdPMF.getDb()) {
            String search = StringUtilities.wrapQuotes("%" + term.toLowerCase() + "%");
            ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

            RecordSet rs;

            String sql;
            sql = "select * from individuals where lower(alternateid) like "
                    + search
                    + " OR lower(nickname) like "
                    + search;

            rs = db.getRecordSet(sql);
            while (rs.next()) {
                SimpleIndividual individual = SimpleFactory.readSimpleIndividual(rs);

                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("label", individual.getDisplayName());
                hm.put("value", String.valueOf(individual.getId()));
                hm.put("type", "individual");
                hm.put("species", individual.getSpecies());
                hm.put("speciesdisplay", individual.getSpeciesDisplayName());
                list.add(hm);
            }

            //
            // Query on Users
            //
            sql = "select * from \"USERS\" where lower(\"FULLNAME\") like "
                    + search
                    + " OR lower(\"USERNAME\") like "
                    + search;

            rs = db.getRecordSet(sql);
            while (rs.next()) {
                HashMap<String, String> hm = new HashMap<String, String>();
                String fullname = rs.getString("FULLNAME");
                String username = rs.getString("USERNAME");
                if (StringUtils.isBlank(fullname)) {
                    hm.put("label", username);
                } else {
                    hm.put("label", fullname + " (" + username + ")");
                }
                hm.put("value", username);
                hm.put("type", "user");
                list.add(hm);
            }

            //
            // return our results
            //
            out.println(new Gson().toJson(list));
        } catch (DatabaseException ex) {
            throw new ServletException(ex);
        }
    }
}
