-- seed a development admin account; credentials documented in .env.example
INSERT INTO users (email, password_hash, full_name, role)
VALUES (
    'admin@spacefurni.dev',
    '$2a$12$4TJ3JD6elbpZ0H0DNyjXQu90PZl5RNX2SQCBuhYdRsMtX7xhpWXCS',
    'SpaceFurni Admin',
    'ADMIN'
);
