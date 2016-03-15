package org.ecocean.event.type;

import org.ecocean.event.BaseEvent;
import org.ecocean.media.MediaSubmission;

public class NewSubmissionEvent extends BaseEvent {
    public final static String EVENT_NAME = "new_submission";

    public NewSubmissionEvent(final MediaSubmission submission) {
        super(EVENT_NAME, submission, submission.getId(), null );
    }
}
