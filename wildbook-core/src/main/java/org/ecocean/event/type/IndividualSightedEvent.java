package org.ecocean.event.type;

import java.time.OffsetDateTime;

import org.ecocean.event.BaseEvent;
import org.ecocean.rest.SimpleIndividual;

public class IndividualSightedEvent extends BaseEvent {
    public final static String EVENT_NAME = "ind_sighted";

    public IndividualSightedEvent(final SimpleIndividual ind) {
        super(EVENT_NAME, ind, ind.getId());
    }

    public IndividualSightedEvent(final int eventId,
                                  final OffsetDateTime timestamp,
                                  final SimpleIndividual ind) {
        super(eventId, EVENT_NAME, timestamp, ind, ind.getId());
    }

}
