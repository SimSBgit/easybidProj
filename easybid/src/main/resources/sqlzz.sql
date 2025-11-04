SHOW DATABASES;

CREATE DATABASE IF NOT EXISTS easybiddb;
USE easybiddb;

CREATE TABLE auction_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cltr_no BIGINT NOT NULL,
    cltr_nm VARCHAR(500),
    apsl_ases_avg_amt BIGINT,
    min_bid_prc BIGINT,
    pbct_cls_dtm VARCHAR(20)
);

DROP TABLE auction_item;

SELECT * FROM auction_item;





