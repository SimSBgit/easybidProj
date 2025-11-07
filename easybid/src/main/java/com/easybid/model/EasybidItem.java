package com.easybid.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EasybidItem {
	
	private Long id;
	private String uuid;
    private Long plnmNo;
    private Long pbctNo;
    private Long pbctCdtnNo;
    private Long cltrNo;
    private Long cltrHstrNo;
    private String scrnGrpCd;
    private String ctgrFullNm;
    private String bidMnmtNo;
    private String cltrNm;
    private String cltrMnmtNo;
    private String ldnmAdrs;
    private String nmrddAdrs;
    private String ldnmPnu;
    private String dpslMtdCd;
    private String dpslMtdNm;
    private String bidMtdNm;
    private Long minBidPrc;
    private Long apslAsesAvgAmt;
    private String feeRate;
    private String pbctBegnDtm;
    private String pbctClsDtm;
    private String pbctCltrStatNm;
    private Long uscbCnt;
    private Long iqryCnt;
    private String goodsNm;
    private String manf;
    private String mdl;
    private String nrgt;
    private String grbx;
    private String endpc;
    private String vhclMlge;
    private String fuel;
    private String scrtNm;
    private String tpbz;
    private String itmNm;
    private String mmbRgtNm;
    private String sido;
    private String sigungu;
    private String emd;
    private String ctgrHirkId;
    private String ctgrHirkIdMid;
    private String cltrImgFiles;
    private String jsonData; // 전체 JSON 보관
    
    public String getPbctBegnDtmFormatted() {
        if (pbctBegnDtm == null || pbctBegnDtm.length() != 14) return pbctBegnDtm;
        DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(pbctBegnDtm, inFmt).format(outFmt);
    }
    
    public String getpbctClsDtmFormatted() {
        if (pbctClsDtm == null || pbctClsDtm.length() != 14) return pbctClsDtm;
        DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(pbctClsDtm, inFmt).format(outFmt);
    }

}
