/**
 * 
 */
package com.jholmberg;

//import the Shepherd Project Framework
import org.ecocean.*;


//import basic IO
import java.io.*;
import java.util.*;

import javax.jdo.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Enumeration;

import java.util.TreeMap;


/**
 * @author jholmber
 *
 */
public class DataNucleusMigrator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		//build the source pm
		Properties sourceProperties = new Properties();
		sourceProperties.setProperty("datanucleus.PersistenceManagerFactoryClass","org.datanucleus.PersistenceManagerFactoryImpl");
		sourceProperties.setProperty("datanucleus.ConnectionDriverName","org.apache.derby.jdbc.EmbeddedDriver");
		sourceProperties.setProperty("datanucleus.ConnectionURL","jdbc:derby:/opt/tomcat6/shepherd_derby_database");
		sourceProperties.setProperty("datanucleus.ConnectionUserName","shepherd");
		sourceProperties.setProperty("datanucleus.ConnectionPassword","shepherd");
		sourceProperties.setProperty("datanucleus.autoCreateSchema","true");
		sourceProperties.setProperty("datanucleus.NontransactionalRead","true");
		sourceProperties.setProperty("datanucleus.Multithreaded","true");
		sourceProperties.setProperty("datanucleus.RestoreValues","true");
		sourceProperties.setProperty("datanucleus.storeManagerType","rdbms");
		PersistenceManagerFactory pmfSource = JDOHelper.getPersistenceManagerFactory(sourceProperties);
		PersistenceManager pmSource = pmfSource.getPersistenceManager();
		pmSource.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT, FetchPlan.ALL});
		pmSource.getFetchPlan().setMaxFetchDepth(-1);

		
		//build the second pm
		Properties destProperties = new Properties();
		destProperties.setProperty("datanucleus.PersistenceManagerFactoryClass","org.datanucleus.PersistenceManagerFactoryImpl");
		destProperties.setProperty("datanucleus.ConnectionDriverName","org.postgresql.Driver");
		destProperties.setProperty("datanucleus.ConnectionURL","jdbc:postgresql://localhost:5432/splash");
		destProperties.setProperty("datanucleus.ConnectionUserName","mySplash");
		destProperties.setProperty("datanucleus.ConnectionPassword","mySplash123");
		destProperties.setProperty("datanucleus.autoCreateSchema","true");
		destProperties.setProperty("datanucleus.NontransactionalRead","true");
		destProperties.setProperty("datanucleus.Multithreaded","true");
		destProperties.setProperty("datanucleus.RestoreValues","true");
		destProperties.setProperty("datanucleus.storeManagerType","rdbms");
		PersistenceManagerFactory pmfDest= JDOHelper.getPersistenceManagerFactory(destProperties);
		PersistenceManager pmDestination = pmfDest.getPersistenceManager();
		
		
		//start our transactions
		pmSource.currentTransaction().begin();
		pmDestination.currentTransaction().begin();
		
		
		//iterate, detach, and store
		
			//first keywords
			ArrayList<MarkedIndividual> detachedKeywords=new ArrayList<MarkedIndividual>();
			Extent allKeywords=pmSource.getExtent(MarkedIndividual.class,true);		
			Query acceptedKeywords=pmSource.newQuery(allKeywords);
			//acceptedKeywords.setOrdering("readableName descending");
			Collection c=(Collection)(acceptedKeywords.execute());
			Iterator itKeywords=c.iterator();
			while(itKeywords.hasNext()){
				
				//get the original
				MarkedIndividual kw=(MarkedIndividual)itKeywords.next();
				System.out.println("Processing MarkedIndividual: "+kw.getName());
				
				//detach
				detachedKeywords.add(pmSource.detachCopy(kw));
			}
			
			pmDestination.makePersistentAll(detachedKeywords);
			
			System.out.println("Trying to commit the changes to the destination.");
			pmDestination.currentTransaction().commit();
			pmSource.currentTransaction().commit();
			
			//second MarkedIndividuals
		
		
		//persist
		//pmSource.currentTransaction().commit();
		//pmDestination.currentTransaction().commit();
		
		
		//close
		pmSource.close();
		pmDestination.close();

	}
	


	
	

}