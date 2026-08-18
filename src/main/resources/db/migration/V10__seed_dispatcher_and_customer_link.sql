-- Add customer_id column to users for customer-user organisation linking
ALTER TABLE users ADD COLUMN customer_id BIGINT;

-- Add DISPATCHER seed user
INSERT INTO users (name, email, password, role, customer_id)
SELECT 'Dispatcher User', 'dispatcher@keystone.com', 'temporary-password', 'DISPATCHER', NULL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'dispatcher@keystone.com');

-- Link customer user to first customer for data isolation
UPDATE users
SET customer_id = (SELECT id FROM customers LIMIT 1)
WHERE email = 'customer@keystone.com' AND customer_id IS NULL;
