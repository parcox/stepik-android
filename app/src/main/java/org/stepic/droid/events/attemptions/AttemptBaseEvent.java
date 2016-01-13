package org.stepic.droid.events.attemptions;

import org.stepic.droid.model.Attempt;

public class AttemptBaseEvent {

    long stepId;

    Attempt mAttempt;

    public AttemptBaseEvent(long stepId, Attempt attempt) {

        this.stepId = stepId;
        mAttempt = attempt;
    }

    public Attempt getAttempt() {
        return mAttempt;
    }


    public long getStepId() {
        return stepId;
    }
}
