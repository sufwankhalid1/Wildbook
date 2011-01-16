package com.jholmberg;
/**
 * 
 */


//import the Shepherd Project Framework
import org.ecocean.*;
import org.ecocean.servlet.ServletUtilities;

//import basic IO
import java.io.*;
import java.util.*;
import java.net.*;

//import date-time formatter for the custom SPLASH date format
import org.joda.time.DateTime;
import org.joda.time.format.*;

//import jackcess
import com.healthmarketscience.*;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.query.*;
import com.healthmarketscience.jackcess.scsu.*;


/**
 * @author jholmber
 *
 */
public class EncounterLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		String urlToThumbnailJSPPage="http://www.splashcatalog.org/shepherd-alpha1/resetThumbnail2.jsp";
		
		System.out.println("\n\nStarting thumbnail work!");
		
		int numThumbnailsToGenerate=36578;
		String IDKey="";
		for(int q=32166;q<numThumbnailsToGenerate;q++){
			System.out.println(q);
			//ping a URL to thumbnail generator - Tomcat must be up and running
		    try 
		    {
		        
		    	//System.out.println("Trying to render a thumbnail for: "+IDKey+ "as "+thumbnailTheseImages.get(q));
		    	String urlString=urlToThumbnailJSPPage+"?number="+q;
		    	//System.out.println("     "+urlString);
		    	URL url = new URL(urlString);
		    
		        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		        in.close();
		    } 
		    catch (MalformedURLException e) {
		    	
		    	System.out.println("Error trying to render the thumbnail for "+IDKey+".");
		    	e.printStackTrace();
		    	
		    }
		    catch (IOException ioe) {
		    	
		    	System.out.println("Error trying to render the thumbnail for "+IDKey+".");
		    	ioe.printStackTrace();
		    	
		    } 
		    
			
			
		}
		

	}
	
	

}