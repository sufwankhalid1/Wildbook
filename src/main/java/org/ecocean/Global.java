package org.ecocean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.util.io.ResourceReader;
import com.samsix.util.io.ResourceReaderImpl;

public enum Global {
    INST;

    private static Logger logger = LoggerFactory.getLogger(Global.class);

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
}