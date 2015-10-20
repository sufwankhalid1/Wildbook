package org.ecocean.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.ecocean.CommonConfiguration;
import org.ecocean.rest.UserController;
import org.ecocean.security.UserFactory;
import org.ecocean.security.UserToken;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.Database;


/**
 * Uses JSecurity to authenticate a user
 * If user can be authenticated successfully
 * forwards user to /secure/index.jsp
 *
 * If user cannot be authenticated then forwards
 * user to the /login.jsp which will display
 * an error message
 *
 */
 public class LoginUser extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;

   private static final Logger logger = LoggerFactory.getLogger(LoginUser.class);

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public LoginUser() {
        super();
    }


    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        doPost(request, response);
    }

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting LoginUser servlet...");
        }

        //see /login.jsp for these form fields
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password").trim();

        UserToken userToken = null;
        try {
            userToken = UserController.getUserToken(request, username, password);

            //
            // If user is null, bail back to our login page.
            //
            if (userToken == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Did not find user.");
                }
                request.setAttribute("error", "No user found with username/email [" + username + "]" );
                WebUtils.redirectToSavedRequest(request, response, "/login.jsp");
                return;
            }

            //The use of IniShiroFilter specified in web.xml
            //caused JSecurity to create the DefaultWebSecurityManager object
            //see: http://jsecurity.org/api/org/jsecurity/web/DefaultWebSecurityManager.html
            //This security manager is the default for web-based applications
            //The SecurityUtils was provided that security manager automatically
            //The configuration specified in web.xml caused
            //a JdbcRealm object to be provided to the SecurityManager
            //so when the login method is called that JdbcRealm
            //object will be used
            //This application uses all the other defaults
            //For example the default authentication query string is
            //"select password from users where username = ?"
            //since the database this application uses (securityDB)
            //has a users table and that table has a column named username
            //and a column named password, the default authentication query
            //string will work
            //The call to login will cause the following to occur
            //Shiro will query the database for a password associated with the
            //provided username (which is stored in token).  If a password is found
            //and matches the password
            //provided by the user (also stored in the token), a new Subject will be created that is
            //authenticated.  This subject will be bound to the session for the
            //user who made this request
            //see:  http://shiro.apache.org/static/current/apidocs/org/apache/shiro/authc/Authenticator.html
            //for a list of potential Exceptions that might be generated if
            //authentication fails (e.g. incorrect password, no username found)

            //
            // get the user (aka subject) associated with this request.
            //
            if (logger.isDebugEnabled()) {
                LogBuilder.get().appendVar("userid", userToken.getUser().getUserId())
                    .appendVar("token.username", userToken.getToken().getUsername()).debug(logger);
            }

            Subject subject = SecurityUtils.getSubject();
            subject.login(userToken.getToken());

            if (request.getParameter("acceptUserAgreement") != null) {
                userToken.getUser().setAcceptedUserAgreement(true);
            }

            String context = ServletUtilities.getContext(request);

            if (UserController.notAcceptedTerms(context, userToken.getUser())) {
                subject.logout();

                // redirect to the user agreement
                // forward the request and response to the view
                String url = CommonConfiguration.getProperty("userAgreementURL",context);
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);

                dispatcher.forward(request, response);
            } else {
                userToken.getUser().setLastLogin((new Date()).getTime());

                try (Database db = ServletUtils.getDb(request)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Saving user...");
                    }
                    UserFactory.saveUser(db, userToken.getUser());
                    WebUtils.redirectToSavedRequest(request, response, "/welcome.jsp");
                }
            }
        } catch (Exception ex) {
            logger.error("Can't login.", ex);
            request.setAttribute("error", ex.getMessage() );
            WebUtils.redirectToSavedRequest(request, response, "/login.jsp");
        } finally {
            if (userToken != null) {
                //clear the information stored in the token
                userToken.clear();
            }
        }
    }
}
