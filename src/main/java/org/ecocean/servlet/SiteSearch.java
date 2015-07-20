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
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.MarkedIndividual;
import org.ecocean.Shepherd;

import com.google.gson.Gson;


public class SiteSearch extends HttpServlet {


  @Override
public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }


  @Override
public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  @Override
public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    //set up for response
    response.setContentType("text/json");
    PrintWriter out = response.getWriter();

    String term = request.getParameter("term");
    if ((term == null) || term.equals("")) {
        out.println("[]");
        return;
    }
    String regex = ".*" + term.toLowerCase() + ".*";

    Iterator all = myShepherd.getAllMarkedIndividuals();
    MarkedIndividual ind;
    ArrayList<HashMap> list = new ArrayList<HashMap>();
    while (all.hasNext()) {
        ind = (MarkedIndividual) (all.next());
        if (Pattern.matches(regex, ind.getIndividualID().toLowerCase())
            || (ind.getNickName() != null && Pattern.matches(regex, ind.getNickName().toLowerCase()))) {
            HashMap h = new HashMap();
            if (StringUtils.isBlank(ind.getNickName())) {
                h.put("label", ind.getIndividualID());
            } else {
                h.put("label", ind.getNickName() + "(" + ind.getIndividualID() + ")");
            }
            h.put("value", ind.getIndividualID());
            list.add(h);
        }
    }
    Gson gson = new Gson();
    out.println(gson.toJson(list));
  }

}
