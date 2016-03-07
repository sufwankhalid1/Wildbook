package org.ecocean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.email.Emailer;
import org.ecocean.event.EventHandler;
import org.ecocean.event.NullEventHandler;
import org.ecocean.location.GeoNamesLocationService;
import org.ecocean.location.LocationService;
import org.ecocean.location.NullLocationService;
import org.ecocean.location.RemoteGeoNames;
import org.ecocean.media.AssetStore;
import org.ecocean.media.AssetStoreFactory;
import org.ecocean.security.DbUserService;
import org.ecocean.security.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.util.UtilException;
import com.samsix.util.io.ResourceReader;
import com.samsix.util.io.ResourceReaderImpl;

public enum Global {
    INST;

    private static Logger logger = LoggerFactory.getLogger(Global.class);

    private final Map<String,ConnectionInfo> connectionInfo = new HashMap<>();
    private ResourceReader appResources;
    private Properties webappClientProps;

    private Emailer emailer;
    private Map<String, Species> species;
    private Map<Integer, CrewRole> crewroles;
    private Map<Integer, VesselType> vesseltypes;
    private List<Species> speciesList;
    private List<CrewRole> crewRoleList;
    private List<VesselType> vesselTypeList;
    private String cust;

    private UserService userService;
    private LocationService locationService;
    private EventHandler eventHandler;

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

    private void initSpecies(final Database db) throws DatabaseException {
        species = new HashMap<>();

        db.getTable("species").select((rs) -> {
            String code = rs.getString("code");
            species.put(code, new Species(code, rs.getString("name")));
        });
        speciesList = new ArrayList<Species>(species.values());
        speciesList.sort((s1, s2) -> {
            return s1.getName().compareTo(s2.getName());
        });
    }

    private void initVesselTypes(final Database db) throws DatabaseException {
        vesseltypes = new HashMap<>();

        db.getTable("vesseltypes").select((rs) -> {
            Integer vesseltypeid = rs.getInt("vesseltypeid");
            vesseltypes.put(vesseltypeid, new VesselType(vesseltypeid, rs.getString("vesseltype")));
        });
        vesselTypeList = new ArrayList<VesselType>(vesseltypes.values());
        vesselTypeList.sort((s1, s2) -> {
            return s1.getVesselType().compareTo(s2.getVesselType());
        });
    }

    private void initCrewRoles(final Database db) throws DatabaseException {
        crewroles = new HashMap<>();

        db.getTable("crewroles").select((rs) -> {
            Integer crewroleid = rs.getInt("crewroleid");
            crewroles.put(crewroleid, new CrewRole(crewroleid, rs.getString("role")));
        });
        crewRoleList = new ArrayList<CrewRole>(crewroles.values());
        crewRoleList.sort((s1, s2) -> {
            return s1.getRole().compareTo(s2.getRole());
        });
    }

    public void init(final Path overridingProps, final Path overridingVars) {
        initResources(overridingProps, overridingVars);
        initDBQueries();
    }

    private void initSSL(final ResourceReaderImpl resources ) {
        String keyBase = "network.ssl.certification";
        String ssl = resources.getString(keyBase + ".type", null);
        if (StringUtils.isBlank(ssl)) {
            return;
        }

        if (ssl.equals("keystore")) {
            try {
                System.setProperty("javax.net.ssl.keyStore", resources.getString(keyBase + ".keystore.file"));
                System.setProperty("javax.net.ssl.keyStorePassword", resources.getString(keyBase + ".keystore.password"));
            } catch (UtilException ex) {
                logger.error("Trouble setting keystore certification.", ex);
            }
        } else if (ssl.equals("trust")) {
            try {

                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        @Override
                        public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                        }
                        @Override
                        public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                        }
                    }
                };

                // Install the all-trusting trust manager
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    @Override
                    public boolean verify(final String hostname, final SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            } catch (Throwable ex) {
                logger.error("Trouble setting up trusted connections.", ex);
            }
        }
    }

    public void initResources(final Path overridingProps, final Path overridingVars) {
        Map<String, String> overridingPropVars = null;
        if (overridingVars != null) {
            if (Files.exists(overridingVars)) {
                Properties props = new Properties();
                try {
                    props.load(new FileInputStream(overridingVars.toFile()));

                    overridingPropVars = new HashMap<String, String>();
                    for (String key : props.stringPropertyNames()) {
                        overridingPropVars.put(key, props.getProperty(key));
                    }
                } catch (FileNotFoundException ex) {
                    logger.error("Can't read [" + overridingVars.toFile() + "]");
                } catch (IOException ex) {
                    logger.error("Can't read [" + overridingVars.toFile() + "]");
                }
            }
        }

        //
        // Load up resources and set default variables for database connections.
        // These variables can be overridden by passing them into overridingPropVars. In addition,
        // you can override the entire property in your overridingProps file for things like the
        // Database.Primary.Url which defaults to...
        //
        //    Database.Primary.Url = jdbc:postgresql://${wildbook.db.server}/${wildbook.db.name}
        //
        // ... but you might, for instance, want to change the whole property in which case you
        // can put the new value of the property in the overridingProps file you pass in here.
        //
        // To connect to a remote database that you don't want to expose directly to the internet
        // you can use an ssh tunnel by running (and leaving open) in a shell the following e.g.
        //
        //    ssh -L5433:localhost:5432 <remote-server>
        //
        // And then you would override the port variable to be 5433 and localhost calls to 5432 would
        // be piped through 5433 to the remote server.
        //
        ResourceReaderImpl resources = new ResourceReaderImpl();
        resources.addVariable("wildbook.db.server", "localhost");
        resources.addVariable("wildbook.db.name", "wildbook");
        resources.addVariable("wildbook.db.user", "shepherd");
        resources.addVariable("wildbook.db.password", "shepherd");
        resources.addVariable("wildbook.db.port", "5432");
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
            if (Files.exists(overridingProps)) {
                try {
                    resources.addSource(overridingProps.toFile());
                } catch (Throwable ex) {
                    //
                    // This is really just here to preserve the old way. If you just add the file above
                    // and add the config.dir property in there then we don't need this anymore. Just
                    // have it here until we can transition our servers and dev machines. As soon as we need
                    // anything else in the <webapp>_init.properties file we might as well get rid of this
                    // else statement as we will have the prop file by then anyway.
                    //
                    logger.error("Can't read init property file.", ex);
                }
            } else {
                logger.warn("File [" + overridingProps + "] does not exist.");
            }
        }

        appResources = resources;

        initSSL(resources);
    }

    /*
     * This has to be separated out from the main init() method because after that is called
     * we use flyway to update the db that these calls might depend on. So these have to be done
     * after that.
     */
    public void initDBQueries() {

        //
        // Initialize other parts of the app.
        //
        try (Database db = Global.INST.getDb()) {
            //
            // Set the static AssetStores map.
            //
            AssetStore.init(AssetStoreFactory.getStores(db));

            initSpecies(db);
            initVesselTypes(db);
            initCrewRoles(db);

        } catch (Throwable ex) {
            logger.error("Trouble initializing the app.", ex);
        }
    }

    public void refreshSpecies() throws DatabaseException {
        try (Database db = Global.INST.getDb()) {
            initSpecies(db);
        }
    }

    public void refreshCrew() throws DatabaseException {
        try (Database db = Global.INST.getDb()) {
            initCrewRoles(db);
        }
    }

    public void refreshVesselTypes() throws DatabaseException {
        try (Database db = Global.INST.getDb()) {
            initVesselTypes(db);
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

    public UserService getUserService() {
        if (userService == null) {
            //
            // TODO: Check for overriding property to use a different UserService
            // if you don't want to use the default wildbook database variant.
            //
            userService = new DbUserService(getConnectionInfo());
        }

        return userService;
    }


    public EventHandler getEventHandler() {
        if (eventHandler == null) {
            try {
                eventHandler = (EventHandler) appResources.getObject("services.event.handler", "org.ecocean.event.NullEventHandler");
            } catch (UtilException ex) {
                logger.error("Trouble creating EventHandler", ex);
                eventHandler = new NullEventHandler();
            }
        }

        return eventHandler;
    }

    public LocationService getLocationService() {
        if (locationService == null) {
            String service = appResources.getString("services.location", null);
            if ("geonames".equals(service)) {
                locationService = new GeoNamesLocationService(getConnectionInfo("geonames"));
            } else if ("remotegeonames".equals(service)) {
                locationService = new RemoteGeoNames(getAppResources().getString("services.location.url", null));
            } else {
                locationService = new NullLocationService();
            }
        }

        return locationService;
    }

    public ConnectionInfo getConnectionInfo() {
      return getConnectionInfo(ConnectionInfo.DBTYPE_PRIMARY);
    }

    public Database getDb() {
        return new Database(getConnectionInfo());
    }

    public ConnectionInfo getConnectionInfo(final String connectionName) {
      if (connectionInfo.get(connectionName) == null) {
          connectionInfo.put(connectionName, ConnectionInfo.valueOf(getAppResources(), connectionName));
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
        if (species == null) {
            return null;
        }
        return species.get(code);
    }

    public List<CrewRole> getCrewRoles() {
        return crewRoleList;
    }

    public CrewRole getCrewRoles(final Integer crewroleid) {
        if (crewroles == null) {
            return null;
        }
        return crewroles.get(crewroleid);
    }

    public List<VesselType> getVesselTypes() {
        return vesselTypeList;
    }

    public VesselType getVesselTypes(final Integer vesseltypeid) {
        if (vesseltypes == null) {
            return null;
        }
        return vesseltypes.get(vesseltypeid);
    }

    public Properties getWebappClientProps() {
        if (webappClientProps == null) {
            webappClientProps = new Properties();

            loadWebappProps("/webappclient.properties");
            if (cust != null) {
                //
                // Load any overriding props for the cust.
                //
                loadWebappProps("/cust/" + cust + "/webappclient.properties");
            }
        }

        return webappClientProps;
    }

    public String getCust() {
        return cust;
    }
}