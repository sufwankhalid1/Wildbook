package org.ecocean.event;
import java.time.OffsetDateTime;

public abstract class BaseEvent {
    private Integer eventid;
    private final String type;
    private final OffsetDateTime timestamp = OffsetDateTime.now();
    private final Integer relPKId1;
    private final Integer relPKId2;
    private final Object relObj;

    public BaseEvent(final String type, final Object relObj, final Integer relPKId1, final Integer relPKId2) {
        this.type = type;
        this.relObj = relObj;
        this.relPKId1 = relPKId1;
        this.relPKId2 = relPKId2;
    }

    public Integer getEventId() {
        return eventid;
    }

    public void setEventId(final Integer id) {
        eventid = id;
    }

    public String getType() {
        return type;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public int getRelPKId1() {
        return relPKId1;
    }

    public int getRelPKId2() {
        return relPKId2;
    }

    public Object getRelObj() {
        return relObj;
    }
}
