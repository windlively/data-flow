-- create user if not exists root password 'root';
-- alter user root admin true;

-- MOCK数据的库表初始化SQL，数据均为虚构

SET MODE MYSQL;

-- -- 模拟业务库
CREATE SCHEMA business_db;

-- 客户信息表
CREATE TABLE business_db.customer_info
(
    id            INT PRIMARY KEY AUTO_INCREMENT COMMENT '客户id',
    account_name  VARCHAR(64) NOT NULL COMMENT '账户名称',
    register_time DATETIME DEFAULT NOW() COMMENT '注册时间',
    status        TINYINT  DEFAULT 2 COMMENT '账户状态'
);

INSERT INTO business_db.customer_info
VALUES (1, 'relay', '2019-12-2', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (2, 'john', '2019-2-2', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (3, 'lily', '2020-1-21', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (4, 'ming', '2020-1-13', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (5, 'peter', '2019-6-7', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (6, 'alice', '2020-5-8', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (7, 'ken', '2020-2-16', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (8, 'wonder', '2020-8-13', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (9, 'hack', '2020-3-5', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (10, 'yong', '2020-4-9', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (11, 'zing', '2020-6-12', DEFAULT);
INSERT INTO business_db.customer_info
VALUES (12, 'lisa', '2020-5-2', DEFAULT);

-- 书籍信息表
CREATE TABLE business_db.book_info
(
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '书籍id',
    name        VARCHAR(128) NOT NULL COMMENT '书名',
    create_time DATETIME DEFAULT NOW() COMMENT '创建时间',
    price       DECIMAL      NOT NULL COMMENT '价格'
);

INSERT INTO business_db.book_info
VALUES (1, '平凡的世界', '2020-8-16', 28.98);
INSERT INTO business_db.book_info
VALUES (2, '瓦尔登湖', '2020-8-19', 23.28);
INSERT INTO business_db.book_info
VALUES (3, '围城', '2020-8-9', 32.12);
INSERT INTO business_db.book_info
VALUES (4, '红楼梦', '2020-7-5', 29.67);
INSERT INTO business_db.book_info
VALUES (5, '悲惨世界', '2020-8-23', 24.42);
INSERT INTO business_db.book_info
VALUES (6, '苔丝', '2020-8-9', 26.19);
INSERT INTO business_db.book_info
VALUES (7, '战争与和平', '2020-8-3', 22.68);
INSERT INTO business_db.book_info
VALUES (8, '朝花夕拾', '2020-8-21', 24.72);
INSERT INTO business_db.book_info
VALUES (9, '童年', '2020-8-9', 19.99);
INSERT INTO business_db.book_info
VALUES (10, '老人与海', '2020-8-16', 32.12);
INSERT INTO business_db.book_info
VALUES (11, '麦田里的守望者', '2020-8-29', 17.98);
INSERT INTO business_db.book_info
VALUES (12, '百年孤独', '2020-8-12', 28.13);
INSERT INTO business_db.book_info
VALUES (13, '阿Q正传', '2020-8-13', 23.63);
INSERT INTO business_db.book_info
VALUES (14, '巴黎圣母院', '2020-8-16', 26.87);
INSERT INTO business_db.book_info
VALUES (15, '黄金时代', '2020-8-22', 27.98);
INSERT INTO business_db.book_info
VALUES (16, '边城', '2020-8-21', 23.91);
INSERT INTO business_db.book_info
VALUES (17, '简爱', '2020-8-2', 28.53);
INSERT INTO business_db.book_info
VALUES (18, '红与黑', '2020-8-12', 22.72);
INSERT INTO business_db.book_info
VALUES (19, '汤姆索亚历险记', '2020-8-5', 19.62);
INSERT INTO business_db.book_info
VALUES (20, '父与子', '2020-8-18', 28.28);

-- -- 模拟日志记录库
-- CREATE SCHEMA op_log_db;
--
-- -- 购买记录表
-- CREATE TABLE op_log_db.purchase_log
-- (
--     id                    INT PRIMARY KEY AUTO_INCREMENT COMMENT 'id主键',
--     book_id               INT          NOT NULL COMMENT '书籍名称',
--     customer_id           INT          NOT NULL COMMENT '用户id',
--     book_name             VARCHAR(128) NOT NULL COMMENT '书籍名称',
--     customer_account_name VARCHAR(64)  NOT NULL COMMENT '购买时的账户名',
--     count                 INT          NOT NULL COMMENT '数量',
--     should_amount         DECIMAL      NOT NULL COMMENT '应付金额',
--     paid_amount           DECIMAL      NOT NULL COMMENT '实付金额',
--     create_time           DATETIME     NOT NULL COMMENT '创建时间'
-- );
--
-- CREATE SCHEMA origin_db;
--
-- -- 模拟同步库
--
-- CREATE TABLE origin_db.origin_order
-- (
--     id                INT PRIMARY KEY AUTO_INCREMENT,
--     customer_id       INT      NOT NULL,
--     count             INT      NOT NULL,
--     amount            DECIMAL  NOT NULL,
--     pay_time          DATETIME NOT NULL,
--     create_time       DATETIME NOT NULL,
--     outer_create_time DATETIME
-- )