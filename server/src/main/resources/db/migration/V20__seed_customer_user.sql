-- seed a development customer account; credentials documented in .env.example
INSERT INTO users (email, password_hash, full_name, role)
VALUES (
    'customer@spacefurni.dev',
    '$2a$12$Ace3GatNvembRc1o2iWpHeY14opiLYDdrShjaE2b677xe2r5VvGaq',
    'SpaceFurni Customer',
    'CUSTOMER'
);
