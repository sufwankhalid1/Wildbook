package org.ecocean;

import com.samsix.util.io.ResourceReader;
import com.samsix.util.io.ResourceReaderImpl;

public enum Global {
    INST;

    private ResourceReader initResources;

    public void setInitResources(final ResourceReader initResources) {
        this.initResources = initResources;
    }

    public ResourceReader getInitResources() {
        if (initResources != null) {
            this.initResources = new ResourceReaderImpl();
        }

        return initResources;
    }
}