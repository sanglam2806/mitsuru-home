-- Reference DDL for PostgreSQL; must match com.mitsuru.admin.beans.Order.
CREATE SEQUENCE IF NOT EXISTS purchase_order_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS purchase_order (
	order_id   BIGINT PRIMARY KEY,
	created_by VARCHAR(100),
	drink_id   BIGINT NOT NULL,
	price      NUMERIC(10, 2) NOT NULL,
	note       VARCHAR(255),
	created_at TIMESTAMP WITH TIME ZONE
);
