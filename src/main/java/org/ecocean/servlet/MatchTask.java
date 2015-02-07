package org.ecocean.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.jdo.Extent;
import javax.jdo.Query;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.match.*;
import org.ecocean.match.method.*;
import org.ecocean.CommonConfiguration;
import org.ecocean.User;
import org.ecocean.Shepherd;


public class MatchTask extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
		Task task = new Task();
		Vector<String> testData = new Vector<String>();
		Vector<String> targetData = new Vector<String>();
		Vector<Method> methods = new Vector<Method>();

		String matchMethodName = request.getParameter("matchMethodName");
    //String context="context0";
    //context=ServletUtilities.getContext(request);
    //Shepherd myShepherd = new Shepherd(context);


		try {
			Object o = Class.forName(matchMethodName).newInstance();
			Method method = (Method) o;
			method.setTestData(testData);
			method.setTargetData(targetData);
			methods.add(method);
		} catch (Exception ex) {
System.out.println("ouch " + ex.toString());
		}

		task.setMethods(methods);

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
        out.println("hello");
    out.close();
  }
  
}
