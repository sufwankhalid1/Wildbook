package org.ecocean.event;

import org.ecocean.rest.SimpleIndividual;

public class IndividualIdentified extends BaseEvent {
    public final static String EVENT_NAME = "ind_identified";

    public IndividualIdentified(final SimpleIndividual ind) {
        super(EVENT_NAME, ind, ind.getId(), null);
    }
}
