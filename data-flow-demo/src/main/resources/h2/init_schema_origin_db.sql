
-- 模拟同步库

CREATE SCHEMA origin_db;

CREATE TABLE origin_db.origin_order(
    id INT PRIMARY KEY AUTO_INCREMENT ,
    customer_id INT NOT NULL ,
    book_id INT NOT NULL ,
    count INT NOT NULL ,
    amount DECIMAL NOT NULL ,
    pay_time DATETIME NOT NULL,
    create_time DATETIME NOT NULL,
    outer_create_time DATETIME
)