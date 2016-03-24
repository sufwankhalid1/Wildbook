package org.ecocean.event.type;

import java.time.OffsetDateTime;

import org.ecocean.encounter.SimpleEncounter;
import org.ecocean.event.BaseEvent;

public class IndividualSightedEvent extends BaseEvent {
    public final static String EVENT_NAME = "ind_sighted";

    public IndividualSightedEvent(final SimpleEncounter enc) {
        super(EVENT_NAME, enc, enc.getId());
    }

    public IndividualSightedEvent(final int eventId,
                                  final OffsetDateTime timestamp,
                                  final SimpleEncounter enc) {
        super(eventId, EVENT_NAME, timestamp, enc, enc.getId());
    }
}
