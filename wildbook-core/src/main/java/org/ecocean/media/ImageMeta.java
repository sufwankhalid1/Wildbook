package org.ecocean.media;

import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ImageMeta {
    private LocalDateTime timestamp;
    private Double latitude;
    private Double longitude;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        return builder.append("timestamp", timestamp)
        .append("latitude", latitude)
        .append("longitude", longitude).toString();
    }
}
