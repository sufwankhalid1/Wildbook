package org.ecocean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ecocean.email.Emailer;
import org.ecocean.media.AssetStore;
import org.ecocean.media.AssetStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.util.UtilException;
import com.samsix.util.io.ResourceReader;
import com.samsix.util.io.ResourceReaderImpl;

public enum Global {
    INST;

    private static Logger logger = LoggerFactory.getLogger(Global.class);

    private Map<String,ConnectionInfo> connectionInfo = new HashMap<>();
    private ResourceReader appResources;
    private Properties webappClientProps = new Properties();

    private Emailer emailer;
    private Map<String, Species> species = new HashMap<>();
    private List<Species> speciesList;
    private String cust;

    private void loadWebappProps(final String file) {
        try (InputStream input = Global.class.getResourceAsStream(file)) {
            if (input == null) {
                logger.warn("No file [" + file + "] found.");
            } else {
                webappClientProps.load(input);
            }
        } catch (Throwable ex) {
            logger.error("Trouble reading [" + file + "]", ex);
        }
    }

    public void init(final File overridingProps, final Map<String, String> overridingPropVars) {
        //
        // Load up resources
        //
        ResourceReaderImpl resources = new ResourceReaderImpl();
        resources.addVariable("wildbook.db.server", "localhost");
        resources.addVariable("wildbook.db.name", "wildbook");
        if (overridingPropVars != null) {
            resources.addVariables(overridingPropVars);
        }

        try {
            resources.addSource("wildbook");
        } catch (IOException ex) {
            logger.warn("Problem reading from application properties", ex);
        }

        try {
            cust = resources.getString("cust.name");
        } catch (UtilException ex) {
            logger.warn("Could not read customer name to configure application.", ex);
            cust = null;
        }

        try {
            resources.addSource("cust/" + cust + "/wildbook");
        } catch (IOException ex) {
            logger.warn("Trouble reading from customer configuration file" , ex);
        }

        if (overridingProps != null) {
            try {
                resources.addSource(overridingProps);
            } catch (Throwable ex) {
                //
                // This is really just here to preserve the old way. If you just add the file above
                // and add the config.dir property in there then we don't need this anymore. Just
                // have it here until we can transition our servers and dev machines. As soon as we need
                // anything else in the <webapp>_init.properties file we might as well get rid of this
                // else statement as we will have the prop file by then anyway.
                //
                logger.warn("Can't read init property file, building simple props from init params.", ex);
    //            Properties props = new Properties();
    //            props.put("config.dir", servletContext.getInitParameter("config.dir"));
    //
    //            initResources.addSource(props);
            }
        }

        appResources = resources;

        loadWebappProps("/webappclient.properties");
        if (cust != null) {
            //
            // Load any overriding props for the cust.
            //
            loadWebappProps("/cust/" + cust + "/webappclient.properties");
        }

        //
        // Initialize other parts of the app.
        //
        try (Database db = Global.INST.getDb()) {
            //
            // Set the static AssetStores map.
            //
            AssetStore.init(AssetStoreFactory.getStores(db));

            db.getTable("species").select((rs) -> {
                String code = rs.getString("code");
                species.put(code, new Species(code, rs.getString("name")));
            });
            speciesList = new ArrayList<Species>(species.values());
            speciesList.sort((s1, s2) -> {
                return s1.getName().compareTo(s2.getName());
            });
        } catch (Throwable ex) {
            logger.error("Trouble initializing the app.", ex);
        }
    }

    public ResourceReader getAppResources() {
        if (appResources == null) {
            //
            // Purely as a safe-guard. Should not happen.
            //
            appResources = new ResourceReaderImpl();
            logger.warn("App Resources were null, setting to empty set.");
        }

        return appResources;
    }


    public ConnectionInfo getConnectionInfo() {
      return getConnectionInfo(ConnectionInfo.DBTYPE_PRIMARY);
    }

    public Database getDb() {
        return new Database(getConnectionInfo());
    }

    public ConnectionInfo getConnectionInfo(final String connectionName) {
      if (connectionInfo.get(connectionName) == null) {
          connectionInfo.put(connectionName, ConnectionInfo.valueOf(appResources, connectionName));
      }

      return connectionInfo.get(connectionName);
    }

    public Emailer getEmailer() {
        if (emailer == null) {
            String host = getAppResources().getString("email.host", "localhost");
            String username = getAppResources().getString("email.username", null);
            String password = getAppResources().getString("email.password", null);

            emailer = new Emailer(host, username, password);
        }

        return emailer;
    }


    public boolean isDevEnv() {
        return appResources.getBoolean("environment.development", false);
    }

    public List<Species> getSpecies() {
        return speciesList;
    }

    public Species getSpecies(final String code) {
        return species.get(code);
    }

    public Properties getWebappClientProps() {
        return webappClientProps;
    }

    public String getCust() {
        return cust;
    }
}