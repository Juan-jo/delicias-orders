package org.delicias.outbox.domain;

import lombok.Getter;

@Getter
public enum OutboxEventType {
    CREATE_ORDER,
    ORDER_STATUS_CHANGED
}
