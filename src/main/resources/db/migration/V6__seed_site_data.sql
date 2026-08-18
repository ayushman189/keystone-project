INSERT INTO sites (name, address, customer_id)
VALUES
    ('Acme Industries - Main Office', '123 Industrial Blvd', (SELECT id FROM customers WHERE name = 'Acme Industries')),
    ('Acme Industries - Warehouse', '456 Storage Ave', (SELECT id FROM customers WHERE name = 'Acme Industries')),
    ('Nova Technologies - HQ', '789 Tech Park', (SELECT id FROM customers WHERE name = 'Nova Technologies')),
    ('Prime Facilities - Building A', '321 Facility Rd', (SELECT id FROM customers WHERE name = 'Prime Facilities'));
