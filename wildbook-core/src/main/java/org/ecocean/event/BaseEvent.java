package org.ecocean.event;
import java.time.OffsetDateTime;

public abstract class BaseEvent {
    private Integer eventid;
    private final String type;
    private final OffsetDateTime timestamp;
    private final Integer relPKId1;
    private final Integer relPKId2;
    private final Object relObj1;
    private final Object relObj2;

    public BaseEvent(final String type,
                     final Object relObj1,
                     final Integer relPKId1,
                     final Object relObj2,
                     final Integer relPKId2) {
        this(null, type, OffsetDateTime.now(), relObj1, relPKId1, relObj2, relPKId2);
    }

    public BaseEvent(final String type,
                     final Object relObj1,
                     final Integer relPKId1) {
        this(type, relObj1, relPKId1, null, null);
    }

    public BaseEvent(final Integer eventId,
                     final String type,
                     final OffsetDateTime timestamp,
                     final Object relObj1,
                     final Integer relPKId1) {
        this(eventId, type, timestamp, relObj1, relPKId1, null, null);
    }

    public BaseEvent(final Integer eventId,
                     final String type,
                     final OffsetDateTime timestamp,
                     final Object relObj1,
                     final Integer relPKId1,
                     final Object relObj2,
                     final Integer relPKId2) {
        this.eventid = eventId;
        this.type = type;
        this.timestamp = timestamp;
        this.relObj1 = relObj1;
        this.relPKId1 = relPKId1;
        this.relObj2 = relObj2;
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

    public Integer getRelPKId1() {
        return relPKId1;
    }

    public Integer getRelPKId2() {
        return relPKId2;
    }

    public Object getRelObj1() {
        return relObj1;
    }

    public Object getRelObj2() {
        return relObj2;
    }
}
