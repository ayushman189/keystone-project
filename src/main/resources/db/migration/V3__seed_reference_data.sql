INSERT INTO customers (name)
VALUES
    ('Acme Industries'),
    ('Nova Technologies'),
    ('Prime Facilities');

INSERT INTO users (name, email, password, role)
VALUES
    ('Admin User', 'admin@keystone.com', 'temporary-password', 'ADMIN'),
    ('Manager User', 'manager@keystone.com', 'temporary-password', 'MANAGER'),
    ('Technician User', 'technician@keystone.com', 'temporary-password', 'TECHNICIAN'),
    ('Customer User', 'customer@keystone.com', 'temporary-password', 'CUSTOMER');