CREATE TABLE IF NOT EXISTS t_balance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    balance DECIMAL(10,2) NOT NULL
);


