SHOW DATABASES;

CREATE DATABASE IF NOT EXISTS easybiddb;
USE easybiddb;

CREATE TABLE auction_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(100) NOT NULL,
    plnm_no BIGINT NULL,
    pbct_no BIGINT NULL,
    pbct_cdtn_no BIGINT NULL,
    cltr_no BIGINT NULL,
    cltr_hstr_no BIGINT NULL,
    scrn_grp_cd VARCHAR(20) NULL,
    ctgr_full_nm VARCHAR(255) NULL,
    bid_mnmt_no VARCHAR(100) NULL,
    cltr_nm VARCHAR(1000) NULL,
    cltr_mnmt_no VARCHAR(100) NULL,
    ldnm_adrs VARCHAR(1000) NULL,
    nmrdd_adrs VARCHAR(1000) NULL,
    ldnm_pnu VARCHAR(100) NULL,
    dpsl_mtd_cd VARCHAR(20) NULL,
    dpsl_mtd_nm VARCHAR(100) NULL,
    bid_mtd_nm VARCHAR(200) NULL,
    min_bid_prc BIGINT NULL,
    apsl_ases_avg_amt BIGINT NULL,
    fee_rate VARCHAR(50) NULL,
    pbct_begn_dtm VARCHAR(14) NULL,
    pbct_cls_dtm VARCHAR(14) NULL,
    pbct_cltr_stat_nm VARCHAR(100) NULL,
    uscbd_cnt BIGINT NULL,
    iqry_cnt BIGINT NULL,
    goods_nm TEXT NULL,
    manf VARCHAR(200) NULL,
    mdl VARCHAR(200) NULL,
    nrgt VARCHAR(200) NULL,
    grbx VARCHAR(200) NULL,
    endpc VARCHAR(200) NULL,
    vhcl_mlge VARCHAR(200) NULL,
    fuel VARCHAR(200) NULL,
    scrt_nm VARCHAR(200) NULL,
    tpbz VARCHAR(200) NULL,
    itm_nm VARCHAR(200) NULL,
    mmb_rgt_nm VARCHAR(200) NULL,
    sido VARCHAR(100) NULL,
    sigungu VARCHAR(100) NULL,
    emd VARCHAR(100) NULL,
    ctgr_hirk_id VARCHAR(50) NULL,
    ctgr_hirk_id_mid VARCHAR(50) NULL,
    cltr_img_files TEXT NULL,
    json_data LONGTEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_id PRIMARY KEY (id),
    CONSTRAINT uq_uuid UNIQUE (UUID),
    CONSTRAINT uq_plnm_pbct UNIQUE (plnm_no, pbct_no)
);

SELECT * FROM auction_item;

DELETE FROM auction_item;

DROP TABLE auction_item;

SELECT * FROM auction_item WHERE id = 1;

