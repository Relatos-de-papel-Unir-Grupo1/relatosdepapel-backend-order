package com.relatosdepapel.orders.event.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreatedEvent {
    private EventHeader header;
    private OrderCreatedEventBody body;
}
