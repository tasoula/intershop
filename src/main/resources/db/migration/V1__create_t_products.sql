CREATE TABLE t_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_path VARCHAR(255) UNIQUE,
    price DECIMAL(10,2) NOT NULL CHECK(price >= 0),
    stock_quantity INT NOT NULL CHECK(stock_quantity >= 0)
);