CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_notification_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders(id),

    CONSTRAINT uk_notification_work_order_type
        UNIQUE (work_order_id, type)
);
