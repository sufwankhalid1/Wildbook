package org.ecocean.rest;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.ecocean.Global;
import org.ecocean.media.AssetStore;
import org.ecocean.media.AssetStoreFactory;
import org.ecocean.security.Stormpath;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.util.UtilException;
import com.samsix.util.io.ResourceReaderImpl;

//@Configuration
//@EnableAutoConfiguration
//@ComponentScan
@SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
public class RestApplication extends SpringBootServletInitializer {
    private static Logger logger = LoggerFactory.getLogger(RestApplication.class);

    /**
     *  This method should allow you to start up the rest service from a compiled jar rather
     *  than having to make a war and stick it in tomcat.
     */
    public static void main(final String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }

    @Override
    protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JodaModule());

        return application.sources(RestApplication.class);
    }


    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException
    {
        //
        // WARN: DO NOT REMOVE THIS LINE
        //
        super.onStartup(servletContext);

        //
        // Load up resources
        //
        ResourceReaderImpl appResources = new ResourceReaderImpl();

        try {
            appResources.addSource("application");
        } catch (IOException ex) {
            logger.warn("Problem reading from application properties", ex);
        }

        String cust;
        try {
            cust = appResources.getString("cust.name");
        } catch (UtilException ex) {
            logger.warn("Could not read customer name to configure application.", ex);
            cust = null;
        }

        try {
            appResources.addSource("cust/" + cust + "/application");
        } catch (IOException ex) {
            logger.warn("Trouble reading from customer configuration file" , ex);
        }

        //
        // TODO: I could not for the life of me get tomcat (on my mac dev machine anyway)
        // read this file from anywhere despite the fact that I tried to put it in all the
        // places that are supposed to be on the classpath. So instead I had to resort
        // to loading it by direct file input.
        //
//      initResources = new ResourceReaderImpl(servletContext.getContextPath() + "_init");
        try {
            File configFile = new File(new File(System.getProperty("catalina.base"), "conf"),
                                       servletContext.getContextPath() + "_init.properties");
            appResources.addSource(configFile);
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


        //
        // Now set this on the global so that we can use it elsewhere.
        //
        Global.INST.setAppResources(appResources);

        //
        // Finish loading up resources.
        //

        //
        // Initialize other parts of the app.
        //
        try (Database db = Global.INST.getDb()) {
            //
            // Set the static AssetStores map.
            //
            AssetStore.init(AssetStoreFactory.getStores(db));
        } catch (Throwable ex) {
            logger.error("Trouble initializing the app.", ex);
        }


        try {
            ResourceReaderImpl secrets = new ResourceReaderImpl("secrets");
            Stormpath.init(secrets.getString("auth.stormpath.apikey.id"),
                           secrets.getString("auth.stormpath.apikey.secret"),
                           appResources.getString("auth.stormpath.appname", cust));
        } catch (Throwable ex) {
            logger.error("Trouble initializing stormpath.", ex);
        }

        //
        // Uses default location of db/migration on classpath. You will find it in the src/main/resources folder.
        // OutOfOrder = true makes it so that if two developers create SQL in a different order and one somehow
        // gets applied to the database, the other's should too. This should only be an issue on development
        // databases and allows me to get other developers sql patches even if I've applied a newer one myself
        // locally. In production, everything should be fine.
        //
        ConnectionInfo connectionInfo = Global.INST.getConnectionInfo();
        Flyway flyway = new Flyway();
        flyway.setOutOfOrder(true);
        flyway.setSqlMigrationPrefix("");
        flyway.setDataSource(connectionInfo.getUrl(), connectionInfo.getUserName(), connectionInfo.getPassword());
        flyway.migrate();

        //check for and inject a default user 'tomcat' if none exists
        try (Database db = Global.INST.getDb()) {
            String sql = "SELECT count(*) as numusers FROM users";
            RecordSet rs = db.getRecordSet(sql);
            if (rs.next() && rs.getInt("numusers") == 0) {
                User newUser = new User(null, "tomcat", "Tomcat User", "tomcat123");

                logger.info("Creating tomcat user account...");

                UserFactory.saveUser(db, newUser);
                UserFactory.addRole(db, newUser.getUserId(), "context0", "admin");
                UserFactory.addRole(db, newUser.getUserId(), "context0", "destroyer");
                UserFactory.addRole(db, newUser.getUserId(), "context0", "rest");
            }
        } catch (DatabaseException ex) {
           logger.error("Can't create bootstrap user tomcat.", ex);
        }
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        return new ServletRegistrationBean(new MediaUploadServlet(),"/mediaupload");
    }
}
