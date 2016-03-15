package org.ecocean.event;

public class DefaultEventHandler extends EventHandler {
    @Override
    public void trigger(final BaseEvent event) {
        notifyListeners(event);
    }
}
