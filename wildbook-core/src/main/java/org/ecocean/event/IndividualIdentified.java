package org.ecocean.event;

import java.time.OffsetDateTime;

import org.ecocean.rest.SimpleIndividual;

public class IndividualIdentified extends BaseEvent {
    public final static String EVENT_NAME = "ind_identified";

    public IndividualIdentified(final SimpleIndividual ind) {
        super(EVENT_NAME, ind, ind.getId());
    }

    public IndividualIdentified(final int eventId,
                                final OffsetDateTime timestamp,
                                final SimpleIndividual ind) {
        super(eventId, EVENT_NAME, timestamp, ind, ind.getId());
    }
}
