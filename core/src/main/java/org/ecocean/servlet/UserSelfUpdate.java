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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ecocean.CommonConfiguration;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;

import com.samsix.database.Database;


public class UserSelfUpdate extends HttpServlet {
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }


    public static void updateUserFromRequest(final HttpServletRequest request, final User user) {
        //set password
        String password = StringUtils.trimToNull(request.getParameter("password"));
        String password2 = StringUtils.trimToNull(request.getParameter("password2"));
        if (password != null && password2 != null) {
            if (password.equals(password2)) {
                user.resetPassword(password);
            } else {
                throw new IllegalArgumentException("Your passwords don't match.");
            }
        }

        user.setFullName(StringUtils.trimToNull(request.getParameter("fullName")));
        user.setEmail(StringUtils.trimToNull(request.getParameter("emailAddress")));

        //
        // TODO: Have new User object handle these things eventually. In some way, maybe not
        // like this?
        //
//        if(request.getParameter("receiveEmails")!=null){
//          newUser.setReceiveEmails(true);
//        }
//        else{newUser.setReceiveEmails(false);}
//
//
//        if((request.getParameter("affiliation")!=null)&&(!request.getParameter("affiliation").trim().equals(""))){
//          newUser.setAffiliation(request.getParameter("affiliation").trim());
//        }
//        else if(isEdit&&(request.getParameter("affiliation")!=null)&&(request.getParameter("affiliation").trim().equals(""))){newUser.setAffiliation(null);}
//
//        if((request.getParameter("userProject")!=null)&&(!request.getParameter("userProject").trim().equals(""))){
//          newUser.setUserProject(request.getParameter("userProject").trim());
//        }
//        else if(isEdit&&(request.getParameter("userProject")!=null)&&(request.getParameter("userProject").trim().equals(""))){newUser.setUserProject(null);}
//
//        if((request.getParameter("userStatement")!=null)&&(!request.getParameter("userStatement").trim().equals(""))){
//          newUser.setUserStatement(request.getParameter("userStatement").trim());
//        }
//        else if(isEdit&&(request.getParameter("userStatement")!=null)&&(request.getParameter("userStatement").trim().equals(""))){newUser.setUserStatement(null);}
//
//        if((request.getParameter("userURL")!=null)&&(!request.getParameter("userURL").trim().equals(""))){
//          newUser.setUserURL(request.getParameter("userURL").trim());
//        }
//        else if(isEdit&&(request.getParameter("userURL")!=null)&&(request.getParameter("userURL").trim().equals(""))){newUser.setUserURL(null);}
//
//        newUser.RefreshDate();
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
        if (!shepherdDataDir.exists()) {
            shepherdDataDir.mkdirs();
        }

        File usersDir=new File(shepherdDataDir.getAbsolutePath()+"/users");
        if (!usersDir.exists()) {
            usersDir.mkdirs();
        }

        //set up for response
        response.setContentType("text/html");

        Integer userid = NumberUtils.createInteger(request.getUserPrincipal().getName());

        PrintWriter out = response.getWriter();
        try (Database db = ServletUtilities.getDb(request)) {
            User user = UserFactory.getUserById(db, userid);

            if (user == null) {
                out.close();
                return;
            }

            updateUserFromRequest(request, user);
            UserFactory.saveUser(db, user);

            //output success statement
            out.println(ServletUtilities.getHeader(request));

            out.println("<strong>Success:</strong> User '" + userid + "' was successfully updated.");

            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/myAccount.jsp" + "\">Return to Your Account" + "</a></p>\n");
            out.println(ServletUtilities.getFooter(context));
            out.close();
        } catch (Exception ex) {
            //output failure statement
            out.println(ServletUtilities.getHeader(request));
            out.println("<strong>Failure:</strong> User was NOT found.");
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request)
                        + "/myAccount.jsp" + "\">Return to our Account" + "</a></p>\n");
            out.println(ServletUtilities.getFooter(context));
            out.close();
        }
    }
}


