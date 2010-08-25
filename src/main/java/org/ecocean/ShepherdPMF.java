package org.ecocean;

import javax.jdo.*;

import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;


public class ShepherdPMF {

  private static PersistenceManagerFactory pmf;

  public synchronized static PersistenceManagerFactory getPMF() {
    //public static PersistenceManagerFactory getPMF(String dbLocation) {
    try {
      if (pmf == null) {

        Properties dnProperties = new Properties();


        dnProperties.setProperty("datanucleus.PersistenceManagerFactoryClass", "org.datanucleus.jdo.JDOPersistenceManagerFactory");
        dnProperties.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.jdo.JDOPersistenceManagerFactory");

        //class setup
        System.out.println("loading properties");
        Properties props = new Properties();
        try {
          props.load(ShepherdPMF.class.getResourceAsStream("/bundles/en/commonConfiguration.properties"));
        }
        catch (IOException ioe) {
          ioe.printStackTrace();
        }

        Enumeration<Object> propsNames = props.keys();
        while (propsNames.hasMoreElements()) {
          String name = (String) propsNames.nextElement();
          if (name.startsWith("datanucleus")) {
            dnProperties.setProperty(name, props.getProperty(name).trim());
          }
        }
        System.out.println("properties are " + dnProperties.toString());
        pmf = JDOHelper.getPersistenceManagerFactory(dnProperties);


      }
      return pmf;
    }
    catch (JDOException jdo) {
      jdo.printStackTrace();
      System.out.println("I couldn't instantiate a PMF.");
      return null;
    }
  }

}
