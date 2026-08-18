UPDATE users
SET password = '$2a$10$fWwIoJKTh5M.uZc9mvi9pOdxgKhUDmXzpTNRKBmkjhqNE8vf3b2P'
WHERE email IN (
    'admin@keystone.com',
    'manager@keystone.com',
    'technician@keystone.com',
    'customer@keystone.com'
);