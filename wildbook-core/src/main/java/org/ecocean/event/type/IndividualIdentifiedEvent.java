package org.ecocean.event.type;

import java.time.OffsetDateTime;

import org.ecocean.event.BaseEvent;
import org.ecocean.rest.SimpleIndividual;

//
// This is a little bit of a complicated event. First off, an individual could be identified as
// one from an external catalog. If so, than it would be triggered simply when the isIdentified
// check box is checked (though accidental checking of this box could lead to annoying incorrect
// notifications! So we might have to be more careful than that.) Secondly, if it is an individual that
// we had already in *our* catalog than the event should be raised such that anyone following that individual
// before it is merged into the other individual needs to be notified but the notification should point
// to the individual it was merged into (as their individual will be deleted). In addition, I guess
// anyone who was following the individual that survived the merger should be notified that there
// was an additional sighting (or set of sightings?).
//
public class IndividualIdentifiedEvent extends BaseEvent {
    public final static String EVENT_NAME = "ind_identified";

    public IndividualIdentifiedEvent(final SimpleIndividual ind) {
        super(EVENT_NAME, ind, ind.getId());
    }

    public IndividualIdentifiedEvent(final int eventId,
                                     final OffsetDateTime timestamp,
                                     final SimpleIndividual ind) {
        super(eventId, EVENT_NAME, timestamp, ind, ind.getId());
    }
}
