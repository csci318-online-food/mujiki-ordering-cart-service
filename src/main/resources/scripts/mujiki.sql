CREATE TABLE cart (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    restaurant_id UUID,
    total_price DOUBLE PRECISION DEFAULT 0.0,
    create_at TIMESTAMP,
    modify_at TIMESTAMP,
    modify_by VARCHAR(64),
    create_by VARCHAR(64)
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID,
    order_id UUID,
    restaurant_id UUID,
    item_id UUID,
    quantity INT,
    price DOUBLE PRECISION,
    create_at TIMESTAMP,
    modify_at TIMESTAMP,
    modify_by VARCHAR(64),
    create_by VARCHAR(64)
);