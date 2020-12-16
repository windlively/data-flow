CREATE SCHEMA op_log_db;
-- 购买记录表
CREATE TABLE op_log_db.purchase_log
(
    id                    INT PRIMARY KEY AUTO_INCREMENT COMMENT 'id主键',
    book_id               INT          NOT NULL COMMENT '书籍名称',
    customer_id           INT          NOT NULL COMMENT '用户id',
    book_name             VARCHAR(128) NOT NULL COMMENT '书籍名称',
    customer_account_name VARCHAR(64)  NOT NULL COMMENT '购买时的账户名',
    count                 INT          NOT NULL COMMENT '数量',
    should_amount         DECIMAL      NOT NULL COMMENT '应付金额',
    paid_amount           DECIMAL      NOT NULL COMMENT '实付金额',
    create_time           DATETIME     NOT NULL COMMENT '创建时间'
);