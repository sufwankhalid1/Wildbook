package org.ecocean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ecocean.email.Emailer;
import org.ecocean.media.AssetStore;
import org.ecocean.media.AssetStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.util.io.ResourceReader;
import com.samsix.util.io.ResourceReaderImpl;

public enum Global {
    INST;

    private static Logger logger = LoggerFactory.getLogger(Global.class);

    private Map<String,ConnectionInfo> connectionInfo = new HashMap<>();
    private ResourceReader appResources;

    private Emailer emailer;
    private List<Species> species = new ArrayList<Species>();

    public void init(final ResourceReader resources) {
        appResources = resources;

        //
        // Initialize other parts of the app.
        //
        try (Database db = Global.INST.getDb()) {
            //
            // Set the static AssetStores map.
            //
            AssetStore.init(AssetStoreFactory.getStores(db));

            db.getTable("species").select((rs) -> {
                species.add(new Species(rs.getString("code"), rs.getString("name")));
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
          try {
              ResourceReader reader = new ResourceReaderImpl("bundles/s6db.properties");
              connectionInfo.put(connectionName, ConnectionInfo.valueOf(reader, connectionName));
          } catch (IOException ex) {
              logger.warn("Can't find properties [bundles/s6db]", ex);
          }
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
        return species;
    }
}