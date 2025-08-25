CREATE TABLE t_orders(
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   total_amount NUMERIC(9,2) CHECK(total_amount >= 0),

   CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES t_users(id) ON DELETE CASCADE
);