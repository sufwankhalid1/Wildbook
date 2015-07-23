package org.ecocean.rest;

import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Date;
//import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
*/
import org.ecocean.servlet.ServletUtilities;
//import org.ecocean.CommonConfiguration;
//import org.ecocean.Shepherd;
//import org.ecocean.User;
import org.ecocean.Util;
import org.ecocean.security.Stormpath;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.resource.ResourceException;
import com.stormpath.sdk.directory.CustomData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.google.gson.Gson;


/*
    fires off (via Stormpath) password reset email, based on email address
*/
 public class PasswordReset extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;

   private static final Logger logger = LoggerFactory.getLogger(PasswordReset.class);

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public PasswordReset() {
        super();
    }

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doPost(request, response);
    }

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        Client client = Stormpath.getClient(ServletUtilities.getConfigDir(request));
        if (client == null) {
            out.println("{\"error\": \"could not initiate Stormpath\"}");
            return;
        }

        String email = request.getParameter("email");
        String token = request.getParameter("token");
        String verify = request.getParameter("verify");
        String password = request.getParameter("password");

        if (!Util.isEmpty(email)) {
            sendResetEmail(out, client, email);
        } else if (Util.isEmpty(token)) {  //both token & verify need token
            out.println("{\"error\": \"missing token\"}");
        } else if (verify != null) {
            verifyToken(out, client, token);
        } else if (!Util.isEmpty(password)) {
            setPassword(out, client, token, password);
        } else {
            out.println("{\"error\": \"invalid\"}");
        }
        out.close();
    }

    private void sendResetEmail(PrintWriter out, Client client, String email) {
        if (logger.isDebugEnabled()) logger.debug("sending Stormpath password reset for email=" + email);
        Account acc = null;
        try {
            acc = Stormpath.sendPasswordResetEmail(client, email);
        } catch (ResourceException ex) {
            logger.warn("failed to send Stormpath password reset for email=" + email + ": " + ex.toString());
        }
        if (acc == null) {
            out.println("{\"error\": \"error sending Stormpath reset email\"}");
        } else {
            logger.debug("successfully sent Stormpath password reset for email=" + email);
            out.println("{\"email\": \"" + email + "\", \"success\": true }");
        }
    }

    private void verifyToken(PrintWriter out, Client client, String token) {
        Account acc = null;
        try {
            acc = Stormpath.getApplication(client).verifyPasswordResetToken(token);
        } catch (ResourceException ex) {
            logger.warn("failed to verify password reset token: " + ex.toString());
        }
        if (acc == null) {
            out.println("{\"error\": \"failed to verify password reset token\"}");
        } else {
            logger.debug("successfully verified Stormpath password reset token");
            out.println("{\"token\": \"" + token + "\", \"success\": true }");
        }
    }

    private void setPassword(PrintWriter out, Client client, String token, String password) {
        Account acc = null;
        try {
            acc = Stormpath.getApplication(client).resetPassword(token, password);
        } catch (ResourceException ex) {
            logger.warn("failed to reset password: " + ex.toString());
        }
        if (acc == null) {
            out.println("{\"error\": \"failed to reset password\"}");
        } else {
            CustomData customData = acc.getCustomData();
            customData.remove("unverified");
            customData.put("verified", true);
            customData.save();
            logger.debug("successfully updated Stormpath password for " + acc.getEmail());
            out.println("{\"success\": true }");
        }
    }
}
