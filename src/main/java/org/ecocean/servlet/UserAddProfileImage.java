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
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.CommonConfiguration;
import org.ecocean.Shepherd;
import org.ecocean.ShepherdPMF;
import org.ecocean.User;
import org.ecocean.media.AssetStore;
import org.ecocean.media.AssetType;
import org.ecocean.media.MediaAsset;
import org.ecocean.mmutil.MediaUtilities;
import org.ecocean.mmutil.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;
import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;

/**
 * Uploads a new image to the file system and associates the image with an Encounter record
 *
 * @author jholmber
 */
public class UserAddProfileImage extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(UserAddProfileImage.class);


    @Override
    public void init(final ServletConfig config)
        throws ServletException
    {
        super.init(config);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        String context="context0";
        //context=ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);

        String username = null;
        ImageInfo imageInfo = null;

        // reading parts has to be done all in one go
        try {
            int maxSize = CommonConfiguration.getMaxMediaSizeInMegabytes(context) * 1048576;
            MultipartParser mp = new MultipartParser(request, maxSize);
            Part part;
            while ((part = mp.readNextPart()) != null) {
                if (part instanceof ParamPart && StringUtilities.isEmpty(username)) {
                    username = getUsername(request, context, (ParamPart)part);
                } else if (part instanceof FilePart) {
                    imageInfo = getImage((FilePart)part);
                }
            }
        } catch (IllegalArgumentException e) {
            showResult(request, response, context, username,
                       "Failure!", e.getMessage());
            return;
        }

        if (StringUtilities.isEmpty(username)) {
            showResult(request, response, context, username,
                       "Failure!", "Can't find the username.");
            return;
        }

        if (imageInfo == null) {
            showResult(request, response, context, username,
                       "Failure!", "No image submitted.");
            return;
        }

        ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
        try (Database db = new Database(ci)) {
            AssetStore assetStore = AssetStore.getDefault();
            if (assetStore == null) {
                throw new IllegalArgumentException("Can't load the default asset store");
            }

            MediaAsset asset = importImage(db, assetStore,
                                           imageInfo.image, imageInfo.filename,
                                           username);
            if (asset == null) {
                throw new IllegalArgumentException("Couldn't import the image");
            }

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
    private String getUsername(final HttpServletRequest request,
                               final String context, final ParamPart part)
        throws IOException, UnsupportedEncodingException
    {
        // our username if we're on a MyAccount (non-admin?) page
        if (request.getRequestURL().indexOf("MyAccount")!=-1){
            return request.getUserPrincipal().getName();
        }

        String name = part.getName();
        if (name.equals("username")) {
            return part.getStringValue();
        }

        return null;
    }

    /**
     * Grab the submitted file and store in a temp file, returning it.
     */
    private ImageInfo getImage(final FilePart part)
        throws IOException
    {
        String fileName = part.getFileName();

        if (fileName == null || fileName.equals("")) {
            throw new IllegalArgumentException("No image submitted.");
        }

        fileName = ServletUtilities.cleanFileName(part.getFileName());

        log.debug("Import user image: " + fileName);

        if (MediaUtilities.isAcceptableImageFile(fileName)) {
            File temp = File.createTempFile(fileName, null);
            temp.deleteOnExit();
            part.writeTo(temp);
            return new ImageInfo(temp, fileName);
        } else {
            throw new IllegalArgumentException("'" + fileName + "' can not be used as an image.");
        }
    }

    /**
     * Copy the submitted file into the AssetStore under the user's
     * subdirectory.
     */
    private MediaAsset importImage(final Database db,
                                   final AssetStore store,
                                   final File image,
                                   final String filename,
                                   final String username)
        throws IOException, UnsupportedEncodingException
    {
        String dest = username + "/" + filename;

        log.debug("Storing user image to: " + dest);

        MediaAsset ma = store.copyIn(db, image, dest, AssetType.ORIGINAL);

        image.delete();

        return ma;
    }

    private void showResult(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final String context,
                            final String username,
                            final String type,
                            final String message)
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

    /** Internal utility tuple. */
    private static class ImageInfo {
        File image;
        String filename;

        ImageInfo(final File f, final String n) {
            image = f;
            filename = n;
        }
    }
}
