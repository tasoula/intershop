CREATE TABLE t_order_items(
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	order_id UUID NOT NULL,
	product_id UUID NOT NULL,
	quantity INT NOT NULL CHECK(quantity >= 0),
	price_at_time_of_order NUMERIC(9,2) NOT NULL CHECK(price_at_time_of_order >= 0),

	CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES t_orders(id) ON DELETE CASCADE,
	CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES t_products(id)
);