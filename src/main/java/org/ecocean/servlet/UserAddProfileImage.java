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

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

import org.ecocean.*;
import org.ecocean.media.*;
import org.ecocean.mmutil.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.samsix.database.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uploads a new image to the file system and associates the image with an Encounter record
 *
 * @author jholmber
 */
public class UserAddProfileImage extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(UserAddProfileImage.class);


    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String context="context0";
        //context=ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);

        String username = getUsername(request, context);
        if (StringUtilities.isEmpty(username)) {
            showResult(request, response, context, username,
                       "Failure!", "Can't find the username");
            return;
        }

        ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
        try (Database db = new Database(ci)) {
            AssetStore assetStore = AssetStore.loadDefault(db);
            if (assetStore == null) {
                throw new IllegalArgumentException("Can't load the default asset store");
            }

            MediaAsset asset = importImage(request, context, assetStore, username);
            if (asset == null) {
                throw new IllegalArgumentException("Couldn't import the image");
            }
            asset.save(db);

            myShepherd.beginDBTransaction();
            User user = myShepherd.getUser(username);
            if (user == null) {
                throw new IllegalArgumentException("Couldn't find the user '" + username + "'");
            }

            user.setUserImage(asset);

            myShepherd.commitDBTransaction();
            myShepherd.closeDBTransaction();

            showResult(request, response, context, username,
                       "Success!",
                       "I have successfully uploaded the user profile image file.");
        } catch (Exception e) {
            myShepherd.rollbackDBTransaction();
            myShepherd.closeDBTransaction();
            e.printStackTrace();
            showResult(request, response, context, username, "Failure!", e.getMessage());
        }
    }

    /**
     * Grab the username from the request url or form request.
     */
    private String getUsername(HttpServletRequest request, String context)
        throws IOException, UnsupportedEncodingException
    {
        // our username if we're on a MyAccount (non-admin?) page
        if (request.getRequestURL().indexOf("MyAccount")!=-1){
            return request.getUserPrincipal().getName();
        }

        // from the form
        int maxSize = CommonConfiguration.getMaxMediaSizeInMegabytes(context) * 1048576;
        MultipartParser mp = new MultipartParser(request, maxSize);

        Part part;
        while ((part = mp.readNextPart()) != null) {
            String name = part.getName();
            if (part.isParam() && name.equals("username")) {
                ParamPart paramPart = (ParamPart)part;
                return paramPart.getStringValue();
            }
        }

        return null;
    }

    /**
     * Copy the submitted file into the AssetStore under the user's
     * subdirectory.
     */
    private MediaAsset importImage(HttpServletRequest request, String context,
                                   AssetStore store, String username)
        throws IOException, UnsupportedEncodingException
    {
        int maxSize = CommonConfiguration.getMaxMediaSizeInMegabytes(context) * 1048576;
        MultipartParser mp = new MultipartParser(request, maxSize);

        Part part;
        while ((part = mp.readNextPart()) != null) {
            if (!part.isFile()) continue;

            FilePart filePart = (FilePart)part;

            String fileName = ServletUtilities.cleanFileName(filePart.getFileName());

            log.debug("Test import user image " + fileName);

            if (MediaUtilities.isAcceptableImageFile(fileName)) {
                // store in user's dir
                String dest = username + "/" + fileName;

                log.debug("Storing import user image " + dest);

                return store.copyIn(filePart, dest, AssetType.ORIGINAL);
            } else {
                throw new IllegalArgumentException("'" + fileName + "' can not be used as an image.");
            }
        }

        return null;
    }

    private void showResult(HttpServletRequest request,
                            HttpServletResponse response,
                            String context,
                            String username,
                            String type,
                            String message)
    {
        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/html");

            out.println(ServletUtilities.getHeader(request));

            out.println("<strong>" + type + "</strong>");
            out.println(message);

            if (request.getRequestURL().indexOf("MyAccount") != -1) {
                out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) +
                            "/myAccount.jsp\">Return to My Account.</a></p>\n");
            }
            else {
                out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) +
                            "/appadmin/users.jsp?context=context0&isEdit=true&username=" + username +
                            "#editUser\">Return to User Management.</a></p>\n");
            }

            out.println(ServletUtilities.getFooter(context));

            //out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
