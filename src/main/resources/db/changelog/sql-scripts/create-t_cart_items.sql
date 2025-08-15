CREATE TABLE t_cart_items(
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	user_id UUID NOT NULL,
	product_id UUID NOT NULL,
	quantity INT NOT NULL CHECK(quantity >= 0),

	CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES t_users(id) ON DELETE CASCADE,
	CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES t_products(id)
);