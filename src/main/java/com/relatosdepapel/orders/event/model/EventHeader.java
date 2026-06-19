package com.relatosdepapel.orders.event.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventHeader {
    private String eventId;
    private String version;
    private String eventType;
    private LocalDateTime timestamp;
}
