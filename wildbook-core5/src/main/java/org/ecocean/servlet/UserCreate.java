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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ecocean.CommonConfiguration;
import org.ecocean.ContextConfiguration;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;

import com.samsix.database.Database;


public class UserCreate extends HttpServlet {
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        String context="context0";
        //context=ServletUtilities.getContext(request);

        //set up the user directory
        //setup data dir
        String rootWebappPath = getServletContext().getRealPath("/");
        File webappsDir = new File(rootWebappPath).getParentFile();
        File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
        if(!shepherdDataDir.exists()){shepherdDataDir.mkdirs();}
        File usersDir=new File(shepherdDataDir.getAbsolutePath()+"/users");
        if(!usersDir.exists()){usersDir.mkdirs();}

        //set up for response
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        //
        // TODO: get rid of isEdit parameter in the call to this? I have no idea what that was doing.
        // TODO: change username parameter to userid in calls to this.
        //
    //    boolean isEdit=false;
    //    if(request.getParameter("isEdit")!=null){
    //      isEdit=true;
    //    }

        try (Database db = ServletUtils.getDb(request)) {
            Integer userid = NumberUtils.createInteger(request.getParameter("userid"));

            User user = UserFactory.getUserById(db, userid);
            boolean createThisUser;
            if (user == null) {
                createThisUser = true;
                user = new User();
            } else {
                createThisUser = false;
                //
                // if this is not a new user, we need to blow away all old roles
                //
                UserFactory.deleteRoles(db, userid);
            }

            UserSelfUpdate.updateUserFromRequest(request, user);
            UserFactory.saveUser(db, user);


            String addedRoles="";
            ArrayList<String> contexts=ContextConfiguration.getContextNames();
            for (int contextNum = 0; contextNum < contexts.size(); contextNum++) {
                String ctxt = "context" + contextNum;
                Set<String> existingRoles = UserFactory.getAllRolesForUserInContext(db, userid, ctxt);

                String[] roles = request.getParameterValues(context + "rolename");
                if (roles != null) {
                    int numRoles=roles.length;
                    for (int i=0; i < numRoles; i++) {
                        String thisRole = roles[i].trim();
                        if (! StringUtils.isBlank(thisRole) && ! existingRoles.contains(thisRole)) {
                            UserFactory.addRole(db, userid, ctxt, thisRole);
                            addedRoles += "SEPARATORSTART"
                                    + ContextConfiguration.getNameForContext(ctxt)
                                    + ":"
                                    + thisRole
                                    + "SEPARATOREND";
                        }
                    }
                }
            }

            //output success statement
            out.println(ServletUtilities.getHeader(request));
            String message;
            if (createThisUser) {
                message = "created with added roles";
            } else {
                message = "updated and has assigned roles";
            }

            out.println("<strong>Success:</strong> User ["
                    + userid
                    + "] was successfully "
                    + message
                    + ": <ul>"
                    + addedRoles.replaceAll("SEPARATORSTART", "<li>").replaceAll("SEPARATOREND", "</li>")
                    +"</ul>");
            out.println("<p><a href=\"http://"
                    + CommonConfiguration.getURLLocation(request)
                    + "/appadmin/users.jsp?context=context0"
                    + "\">Return to User Administration"
                    + "</a></p>\n");
            out.println(ServletUtilities.getFooter(context));
        } catch(Exception ex) {
            //output failure statement
            out.println(ServletUtilities.getHeader(request));
            out.println("<strong>Failure:</strong>");
            out.println(ex.getMessage());
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/appadmin/users.jsp?context=context0" + "\">Return to User Administration" + "</a></p>\n");
            out.println(ServletUtilities.getFooter(context));
        } finally {
            out.close();
        }
    }
}


