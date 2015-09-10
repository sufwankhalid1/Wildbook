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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.CommonConfiguration;
import org.ecocean.ContextConfiguration;
import org.ecocean.MailThreadExecutorService;
import org.ecocean.NotificationMailer;
import org.ecocean.Shepherd;
import org.ecocean.ShepherdProperties;
import org.ecocean.User;
import org.ecocean.security.Collaboration;

import com.google.gson.Gson;

public class Collaborate extends HttpServlet {


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

		Properties props = new Properties();
		String langCode = ServletUtilities.getLanguageCode(request);
		props = ShepherdProperties.getProperties("collaboration.properties", langCode, context);


    String username = request.getParameter("username");
		String approve = request.getParameter("approve");
		String optionalMessage = request.getParameter("message");
		String currentUsername = ((request.getUserPrincipal() == null) ? "" : request.getUserPrincipal().getName());
    boolean useJson = !(request.getParameter("json") == null);

		HashMap rtn = new HashMap();
		rtn.put("success", false);


		if (request.getUserPrincipal() == null) {
			rtn.put("message", props.getProperty("inviteResponseMessageAnon"));

		} else if (request.getParameter("getNotificationsWidget") != null) {
			rtn.put("content", Collaboration.getNotificationsWidgetHtml(request));
			rtn.put("success", "true");

		} else if (request.getParameter("getNotifications") != null) {
			ArrayList<Collaboration> collabs = Collaboration.collaborationsForUser(context, currentUsername, Collaboration.STATE_INITIALIZED);
System.out.println(collabs);
			String html = "";
			for (Collaboration c : collabs) {
				if (!c.getUsername1().equals(currentUsername)) {  //this user did not initiate
					html += "<div class=\"collaboration-invite-notification\" data-username=\"" + c.getUsername1() + "\">" + c.getUsername1() + " <input class=\"yes\" type=\"button\" value=\"" + props.getProperty("buttonApprove") + "\" /> <input class=\"no\" type=\"button\" value=\"" + props.getProperty("buttonDeny") + "\" /></div>";
				}
			}
			String button = "<p><input onClick=\"$('.popup').remove()\" type=\"button\" value=\"close\" /></p>";
			if (html.equals("")) {
				rtn.put("content", props.getProperty("notificationsNone") + button);
			} else {
				rtn.put("content", "<h2>" + props.getProperty("notificationsTitle") + "</h2>" + html + button);
			}

		} else if ((username == null) || username.equals("")) {
			rtn.put("message", props.getProperty("inviteResponseMessageNoUsername"));

		} else if ((approve != null) && !approve.equals("")) {
			Collaboration collab = Collaboration.collaborationBetweenUsers(context, currentUsername, username);
			if ((collab == null) || !collab.getState().equals(Collaboration.STATE_INITIALIZED)) {
				rtn.put("message", props.getProperty("approvalResponseMessageBad"));
			} else if (approve.equals("yes")) {
				myShepherd.beginDBTransaction();
				collab.setState(Collaboration.STATE_APPROVED);
				myShepherd.commitDBTransaction();
				rtn.put("success", true);
			} else {
				myShepherd.beginDBTransaction();
				collab.setState(Collaboration.STATE_REJECTED);
				myShepherd.commitDBTransaction();
				rtn.put("success", true);
			}

		//plain old invite!
		} else {
			Collaboration collab = Collaboration.collaborationBetweenUsers(context, currentUsername, username);
			if (collab != null) {
				rtn.put("message", props.getProperty("inviteResponseMessageAlready"));
				System.out.println("collab already exists, state=" + collab.getState());
			} else {
				collab = Collaboration.create(currentUsername, username);
  			myShepherd.storeNewCollaboration(collab);

				//TODO move emailing to .create()  ??
				User recip = myShepherd.getUserOLD(username);
				if ((recip != null) && recip.getReceiveEmails() && (recip.getEmailAddress() != null) && !recip.getEmailAddress().equals("")) {
					String mailTo = recip.getEmailAddress();
          Map<String, String> tagMap = new HashMap<>();
          tagMap.put("@CONTEXT_NAME@", ContextConfiguration.getNameForContext(context));
          tagMap.put("@USER@", username);
          tagMap.put("@LINK@", String.format("http://%s/myAccount.jsp", CommonConfiguration.getURLLocation(request)));
          tagMap.put("@TEXT_CONTENT@", optionalMessage == null ? "" : optionalMessage);
					System.out.println("/Collaborate: attempting email to (" + username + ") " + mailTo);
					ThreadPoolExecutor es = MailThreadExecutorService.getExecutorService();
					es.execute(new NotificationMailer(context, null, mailTo, "collaborationInvite", tagMap));
          es.shutdown();
				} else {
					System.out.println("/Collaborate: skipping email to uid=" + username);
				}

				rtn.put("message", props.getProperty("inviteResponseMessageSent"));
				rtn.put("success", true);
			}
		}

    PrintWriter out = response.getWriter();
		if (useJson) {
    	response.setContentType("application/json");
    	response.setCharacterEncoding("UTF-8");
    	String json = new Gson().toJson(rtn);
			out.println(json);

		} else {
    	response.setContentType("text/html");
			out.println(ServletUtilities.getHeader(request));
			if (Boolean.TRUE.equals(rtn.get("success"))) {
				out.println("<p class=\"collaboration-invite-success\">" + props.getProperty("inviteSuccess") + "</p>");
			} else {
				out.println("<p class=\"collaboration-invite-failure\">" + props.getProperty("inviteFailure") + "</p>");
			}
			if (rtn.get("message") != null) out.println("<p class=\"collaboration-invite-message\">" + rtn.get("message") + "</p>");
			out.println(ServletUtilities.getFooter(context));
		}

		out.close();
  }


}


