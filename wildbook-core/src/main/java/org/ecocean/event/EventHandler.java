package org.ecocean.event;

public interface EventHandler {
    public void trigger(final String type, final Object relatedObj);
}
