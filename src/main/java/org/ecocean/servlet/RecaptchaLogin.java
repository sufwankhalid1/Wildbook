package org.ecocean.servlet;

import java.io.IOException;




import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.Vector;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;


import org.apache.shiro.web.util.WebUtils;
import org.ecocean.*;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;



 public class RecaptchaLogin extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   
   

  /* (non-Java-doc)
   * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    doPost(request, response);
  }   
  
  /* (non-Java-doc)
   * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    
    String remoteAddr = request.getRemoteAddr();
    ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
    reCaptcha.setPrivateKey("6LczL_cSAAAAAIW8b4PWN5jv0TdjcqEEC61E6-ro");

    String challenge = request.getParameter("recaptcha_challenge_field");
    String uresponse = request.getParameter("recaptcha_response_field");
    ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);

    if (     (reCaptchaResponse.isValid())     &&((request.getParameter("email")!=null)&&(!request.getParameter("email").trim().equals("")))          ) {
      
      //this is the correct response
      

      // forward the request and response to the login servlet
      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/LoginUser?username=demodna&password=demodna");
       
      //get the email thread handler
      ThreadPoolExecutor es = MailThreadExecutorService.getExecutorService();

      String context=ServletUtilities.getContext(request);
      Vector e_images = new Vector();
      
      
      StringBuffer new_message=new StringBuffer();
      new_message.append("There has been a new Wildbook DemoDNA login! \n\rEmail address: "+request.getParameter("email"));
      new_message.append("\n\rPotential species: "+request.getParameter("species"));
      new_message.append("\n\rUsage model: "+request.getParameter("usage"));
      //email the new submission address defined in commonConfiguration.properties
      es.execute(new NotificationMailer(CommonConfiguration.getMailHost(context), "do-not-reply@splashcatalog.org", "scott.baker@oregonstate.edu,holmbergius@gmail.com", "New DemoDNA site login!", new_message.toString(), e_images,context));

      es.shutdown();
       
       dispatcher.forward(request, response);   
      
      
    } 
    
    else{
 // forward the request and response to the view
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/login.jsp");
     
     dispatcher.forward(request, response);   
    }
    
 
    
  }             
}