package org.ecocean.event;

public class NullEventHandler implements EventHandler {
    @Override
    public void trigger(final String type, final Object relatedObj) {
        // do nothing
    }
}
