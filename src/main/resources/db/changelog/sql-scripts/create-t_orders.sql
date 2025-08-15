CREATE TABLE t_orders(
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID NOT NULL,
   delivery_address VARCHAR(255),
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   total_amount NUMERIC(9,2) CHECK(total_amount >= 0),
   status VARCHAR(50),

   CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES t_users(id) ON DELETE CASCADE
);