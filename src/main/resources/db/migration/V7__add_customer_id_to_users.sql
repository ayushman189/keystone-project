ALTER TABLE users ADD COLUMN customer_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_user_customer FOREIGN KEY (customer_id) REFERENCES customers(id);