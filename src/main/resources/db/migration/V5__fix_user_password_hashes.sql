UPDATE users
SET password = '$2a$10$oAAkm/flT.Qt4Cajwv86j.dfpYzU4az8CNEefg5we1CyXRoTPPpBW'
WHERE email IN (
    'admin@keystone.com',
    'manager@keystone.com',
    'technician@keystone.com',
    'customer@keystone.com'
);