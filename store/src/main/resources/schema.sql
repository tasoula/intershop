CREATE TABLE IF NOT EXISTS t_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_path VARCHAR(255) UNIQUE,
    price DECIMAL(10,2) NOT NULL CHECK(price >= 0),
    stock_quantity INT NOT NULL CHECK(stock_quantity >= 0)
);

CREATE TABLE IF NOT EXISTS t_users(
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	user_name VARCHAR(255) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_authorities(
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	authority VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS t_users_authorities(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    authority_id UUID,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES t_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_authority FOREIGN KEY (authority_id) REFERENCES t_authorities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_cart_items(
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	user_id UUID NOT NULL,
	product_id UUID NOT NULL,
	quantity INT NOT NULL CHECK(quantity >= 0),
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

	CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES t_users(id) ON DELETE CASCADE,
	CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES t_products(id)
);

CREATE TABLE IF NOT EXISTS t_orders(
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   total_amount NUMERIC(9,2) CHECK(total_amount >= 0),

   CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES t_users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_order_items(
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	order_id UUID NOT NULL,
	product_id UUID NOT NULL,
	quantity INT NOT NULL CHECK(quantity >= 0),
	price_at_time_of_order NUMERIC(9,2) NOT NULL CHECK(price_at_time_of_order >= 0),

	CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES t_orders(id) ON DELETE CASCADE,
	CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES t_products(id)
);