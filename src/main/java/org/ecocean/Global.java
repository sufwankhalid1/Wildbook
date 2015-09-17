package org.ecocean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private ResourceReader initResources;

    public void setInitResources(final ResourceReader resources) {
        initResources = resources;
    }

    public ResourceReader getInitResources() {
        if (initResources == null) {
            //
            // Purely as a safe-guard. Should not happen.
            //
            initResources = new ResourceReaderImpl();
            logger.warn("Init Resources were null, setting to empty set.");
        }

        return initResources;
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
}