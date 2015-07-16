package org.ecocean.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.ecocean.CommonConfiguration;
import org.ecocean.Shepherd;
import org.ecocean.User;
import org.ecocean.security.Stormpath;

import com.google.gson.Gson;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.resource.ResourceException;


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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String url = "/login.jsp";

		System.out.println("Starting LoginUser servlet...");

		//see /login.jsp for these form fields
		String username = request.getParameter("username").trim();
		String password = request.getParameter("password").trim();

    boolean wantJson = (request.getParameter("json") != null);


		String salt="";
		String context="context0";
    //context=ServletUtilities.getContext(request);
		Shepherd myShepherd=new Shepherd(context);
		myShepherd.beginDBTransaction();
    User loggedInUser = null;

		try{
		  if(myShepherd.getUser(username)!=null){
		    User user=myShepherd.getUser(username);
        loggedInUser = user;
		    salt=user.getSalt();
		    if(request.getParameter("acceptUserAgreement")!=null){
		      System.out.println("Trying to set acceptance for UserAgreement!");
		      user.setAcceptedUserAgreement(true);
		      myShepherd.commitDBTransaction();
		    }
		    else{
		      myShepherd.rollbackDBTransaction();
		    }

		  }
		  else{
		    myShepherd.rollbackDBTransaction();
		  }
		}
		catch(Exception e){
		  myShepherd.rollbackDBTransaction();
		}

		myShepherd.closeDBTransaction();
    String hashedPassword=ServletUtilities.hashAndSaltPassword(password, salt);


    //we *first* try Stormpath, and see what we get
    String propPath = request.getSession().getServletContext().getRealPath("/") + "/WEB-INF/classes/bundles/stormpathApiKey.properties";
    Client client = Stormpath.getClient(propPath);
    myShepherd = new Shepherd(context);

    if (client != null) {
System.out.println("checking Stormpath for login!");
        Account acc = null;
        try {
	          acc = Stormpath.loginAccount(client, username, password);
        } catch (ResourceException ex) {
	          System.out.println("failed to authenticate user '" + username + "' via Stormpath; falling back to Wildbook User: " + ex.toString());
        }
        if (acc != null) {
            User u = myShepherd.getUserByEmailAddress(acc.getEmail());
            loggedInUser = u;
            if (u == null) {
                //TODO we should probably have some kinda rules here: like stormpath user is a certain group etc
                System.out.println("successful authentication via Stormpath, but no Wildbook user for email " + acc.getEmail() + ". creating one!");
                try {
                    u = new User(acc);
                    myShepherd.getPM().makePersistent(u);
                    loggedInUser = u;
                } catch (Exception ex) {
                    System.out.println("trouble creating Wildbook user from Stormpath: " + ex.toString());
                }
            }
            if (u != null) {
                //hackily log them into wb!
                username = u.getUsername();
                hashedPassword = u.getPassword();
            }
        }
    }

	    //create a UsernamePasswordToken using the
		//username and password provided by the user
		UsernamePasswordToken token = new UsernamePasswordToken(username, hashedPassword);

		boolean redirectUser=false;

		try {

			//get the user (aka subject) associated with
			//this request.

			Subject subject = SecurityUtils.getSubject();

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

			subject.login(token);

		   myShepherd.beginDBTransaction();
		    if(myShepherd.getUser(username)!=null){
		      User user=myShepherd.getUser(username);
		      if((CommonConfiguration.getProperty("showUserAgreement",context)!=null)&&(CommonConfiguration.getProperty("userAgreementURL",context)!=null)&&(CommonConfiguration.getProperty("showUserAgreement",context).equals("true"))&&(!user.getAcceptedUserAgreement())){
		        subject.logout();
		        redirectUser=true;
		        //redirect to the user agreement

		      }
		      else{
		        user.setLastLogin((new Date()).getTime());
		        url = "/welcome.jsp";}

		    }

		    myShepherd.commitDBTransaction();
        myShepherd.closeDBTransaction();

        if(redirectUser && !wantJson){url=CommonConfiguration.getProperty("userAgreementURL",context);}



			//clear the information stored in the token

			token.clear();



		}
		catch (UnknownAccountException ex) {
			//username provided was not found
			ex.printStackTrace();
			request.setAttribute("error", ex.getMessage() );
			myShepherd.rollbackDBTransaction();
			myShepherd.closeDBTransaction();

		}
		catch (IncorrectCredentialsException ex) {
			//password provided did not match password found in database
			//for the username provided
		  myShepherd.rollbackDBTransaction();
		  myShepherd.closeDBTransaction();
			ex.printStackTrace();
			request.setAttribute("error", ex.getMessage());
		}

		catch (Exception ex) {

			ex.printStackTrace();

			request.setAttribute("error", "Login NOT SUCCESSFUL - cause not known!");

		}


	     // forward the request and response to the view
        //RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);

        //dispatcher.forward(request, response);

		//WebUtils.redirectToSavedRequest(request, response, url);

    // forward the request and response to the view
		if(redirectUser && !wantJson){
		  //RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		  //dispatcher.forward(request, response);

		  // forward the request and response to the view
		  RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);

		  dispatcher.forward(request, response);

		}

    if (wantJson) {
        HashMap rtn = new HashMap();
        rtn.put("username", username);
        Object err = request.getAttribute("error");
        if (err != null) {
            rtn.put("error", err);
            rtn.put("success", false);
        } else {
            rtn.put("fullName", loggedInUser.getFullName());
            rtn.put("success", true);
        }
        if (redirectUser) rtn.put("needsTerms", true);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = new Gson().toJson(rtn);
        out.println(json);
    } else {
        WebUtils.redirectToSavedRequest(request, response, url);
    }




	}
}
