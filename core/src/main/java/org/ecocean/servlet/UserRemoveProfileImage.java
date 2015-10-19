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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.CommonConfiguration;
import org.ecocean.Shepherd;
import org.ecocean.User;


public class UserRemoveProfileImage extends HttpServlet {


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
        throws ServletException, IOException
    {
        String context="context0";
        //context=ServletUtilities.getContext(request);

        Shepherd myShepherd = new Shepherd(context);
        //set up for response
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        if (username != null) username = username.trim();

        if (request.getRequestURL().indexOf("MyAccount") != -1) {
            username = request.getUserPrincipal().getName();
        }

        String deleteMessage = null;

        myShepherd.beginDBTransaction();
        User user = myShepherd.getUserOLD(username);
        if (user != null) {
            user.setUserImage(null);
        }
        myShepherd.commitDBTransaction();
        myShepherd.closeDBTransaction();

        out.println(ServletUtilities.getHeader(request));

        if (!username.equals("None")) {
            out.println("<strong>Success!</strong> I have successfully removed the profile photo.");
        }
        else {
            out.println("<strong>Failure:</strong> No such user exists in the library.");
        }

        if (request.getRequestURL().indexOf("MyAccount") != -1) {
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) +
                        "/myAccount.jsp\">Return to My Account.</a></p>\n");

        }
        else {
            out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) +
                        "/appadmin/users.jsp?context=context0&isEdit=true&username=" + username +
                        "#editUser\">Return to User Management</a></p>\n");
        }

        if (deleteMessage != null) {
            out.println("<strong>Error:</strong>" + deleteMessage);
        }

        out.println(ServletUtilities.getFooter(context));

        out.close();
    }
}
