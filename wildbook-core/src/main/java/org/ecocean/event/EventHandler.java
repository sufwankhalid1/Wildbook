package org.ecocean.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EventHandler {
    private final Map<String, List<EventListener>> mapListeners = new HashMap<>();

    private List<EventListener> getListeners(final String eventType) {
        List<EventListener> listeners = mapListeners.get(eventType);
        if (listeners == null) {
            listeners = new ArrayList<>();
            mapListeners.put(eventType, listeners);
        }
        return listeners;
    }

    public void registerListener(final String eventType, final EventListener listener) {
        getListeners(eventType).add(listener);
    }

    protected void notifyListeners(final BaseEvent event) {
        for (EventListener listener : getListeners(event.getType())) {
            listener.eventOccurred(event);
        }
    }

    public abstract void trigger(final BaseEvent event);
}