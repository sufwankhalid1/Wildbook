package org.ecocean.event;

public class NullEventHandler implements EventHandler {
    @Override
    public void trigger(final BaseEvent event) {
        // do nothing
    }
}
