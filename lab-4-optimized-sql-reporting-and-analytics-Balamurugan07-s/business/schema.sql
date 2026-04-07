-- ============================================================
-- Business Intelligence Schema + Sample Data
-- ============================================================

CREATE TABLE IF NOT EXISTS customers (
    customer_id   INT PRIMARY KEY,
    customer_name VARCHAR(100),
    region        VARCHAR(50),
    segment       VARCHAR(50)   -- 'Retail', 'Wholesale', 'Online'
);

CREATE TABLE IF NOT EXISTS products (
    product_id   INT PRIMARY KEY,
    product_name VARCHAR(100),
    category     VARCHAR(50),
    unit_price   DECIMAL(10,2)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id    INT PRIMARY KEY,
    customer_id INT REFERENCES customers(customer_id),
    order_date  DATE,
    status      VARCHAR(20)   -- 'Completed', 'Cancelled', 'Pending'
);

CREATE TABLE IF NOT EXISTS order_items (
    item_id    INT PRIMARY KEY,
    order_id   INT REFERENCES orders(order_id),
    product_id INT REFERENCES products(product_id),
    quantity   INT,
    unit_price DECIMAL(10,2)
);

-- ---- Sample Data ----
INSERT INTO customers VALUES
(1,'Alice Johnson','North','Retail'),
(2,'Bob Smith','South','Wholesale'),
(3,'Carol White','East','Online'),
(4,'David Brown','West','Retail'),
(5,'Eva Green','North','Online');

INSERT INTO products VALUES
(1,'Laptop Pro','Electronics',1200.00),
(2,'Wireless Mouse','Electronics',25.00),
(3,'Office Chair','Furniture',350.00),
(4,'Standing Desk','Furniture',600.00),
(5,'Notebook Pack','Stationery',15.00),
(6,'Monitor 27"','Electronics',450.00);

INSERT INTO orders VALUES
(101,1,'2024-01-10','Completed'),
(102,2,'2024-01-15','Completed'),
(103,3,'2024-02-05','Completed'),
(104,1,'2024-02-20','Completed'),
(105,4,'2024-03-01','Cancelled'),
(106,5,'2024-03-15','Completed'),
(107,2,'2024-04-10','Completed'),
(108,3,'2024-04-22','Completed'),
(109,1,'2024-05-05','Completed'),
(110,5,'2024-05-18','Completed');

INSERT INTO order_items VALUES
(1,101,1,1,1200.00),(2,101,2,2,25.00),
(3,102,3,4,350.00),(4,102,4,1,600.00),
(5,103,6,2,450.00),(6,103,2,3,25.00),
(7,104,1,1,1200.00),(8,104,5,10,15.00),
(9,105,3,2,350.00),
(10,106,1,2,1200.00),(11,106,6,1,450.00),
(12,107,4,2,600.00),(13,107,2,5,25.00),
(14,108,1,1,1200.00),(15,108,3,1,350.00),
(16,109,6,3,450.00),(17,109,5,20,15.00),
(18,110,1,1,1200.00),(19,110,4,1,600.00);
