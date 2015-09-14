package org.ecocean;

import com.samsix.util.io.ResourceReader;

public enum Global {
    INST;

    private ResourceReader initResources;

    public void setInitResources(final ResourceReader initResources) {
        if (initResources == null) {
            this.initResources = initResources;
        }
    }

    public ResourceReader getInitResources() {
        return initResources;
    }
}