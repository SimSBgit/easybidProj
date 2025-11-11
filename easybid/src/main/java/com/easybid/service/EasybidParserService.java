package com.easybid.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.easybid.model.EasybidItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EasybidParserService {

	 /**
     * XML 응답을 파싱하여 EasybidItem 리스트로 변환
     * 날짜 필터링 및 최신 공매번호 필터링 적용
     */
    public Map<Long, EasybidItem> parseXmlToItems(String xmlResponse, LocalDate startDate, LocalDate endDate) {
        Map<Long, EasybidItem> latestItemsMap = new HashMap<>();
        
        try {
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode root = xmlMapper.readTree(xmlResponse);
            JsonNode items = root.path("body").path("items").path("item");
            
            log.info("📦 아이템 노드 수: {}", items.isArray() ? items.size() : 0);
            
            if (items.isArray()) {
                for (JsonNode node : items) {
                    EasybidItem item = parseItemNode(node, startDate, endDate);
                    
                    if (item == null) {
                        continue; // 날짜 필터링 탈락
                    }
                    
                    // 같은 공고번호 중 공매번호가 큰 것만 유지 (최신 공매)
                    Long plnmNo = item.getPlnmNo();
                    Long pbctNo = item.getPbctNo();
                    
                    EasybidItem existing = latestItemsMap.get(plnmNo);
                    if (existing == null || existing.getPbctNo() < pbctNo) {
                        latestItemsMap.put(plnmNo, item);
                        log.debug("🔄 공고번호 {} - 공매번호 {} 업데이트", plnmNo, pbctNo);
                    } else {
                        log.debug("⏭️ 공고번호 {} - 공매번호 {} 스킵 (더 최신 {}가 있음)", plnmNo, pbctNo, existing.getPbctNo());
                    }
                }
            } else {
                log.warn("⚠️ items 노드가 배열이 아닙니다. XML 구조를 확인하세요.");
            }
        } catch (Exception e) {
            log.error("❌ XML 파싱 실패: {}", e.getMessage());
        }
        
        log.info("📊 최신 공매 필터링 후: {}개", latestItemsMap.size());
        return latestItemsMap;
    }
    
    /**
     * 개별 JsonNode를 EasybidItem 객체로 변환
     */
    private EasybidItem parseItemNode(JsonNode node, LocalDate startDate, LocalDate endDate) {
        try {
            EasybidItem item = new EasybidItem();
            
            Long plnmNo = node.path("PLNM_NO").asLong();
            Long pbctNo = node.path("PBCT_NO").asLong();
            
            item.setPlnmNo(plnmNo);
            item.setPbctNo(pbctNo);
            
            // 이미지 URL 추출
            item.setCltrImgFiles(extractImageUrls(node));
            
            // 기본 정보
            item.setPbctCdtnNo(node.path("PBCT_CDTN_NO").asLong());
            item.setCltrNo(node.path("CLTR_NO").asLong());
            item.setCltrHstrNo(node.path("CLTR_HSTR_NO").asLong());
            
            item.setScrnGrpCd(node.path("SCRN_GRP_CD").asText(""));
            item.setCtgrFullNm(node.path("CTGR_FULL_NM").asText(""));
            item.setBidMnmtNo(node.path("BID_MNMT_NO").asText(""));
            
            item.setCltrNm(node.path("CLTR_NM").asText(""));
            item.setCltrMnmtNo(node.path("CLTR_MNMT_NO").asText(""));
            item.setLdnmAdrs(node.path("LDNM_ADRS").asText(""));
            item.setNmrddAdrs(node.path("NMRD_ADRS").asText(""));
            item.setLdnmPnu(node.path("LDNM_PNU").asText(""));
            
            item.setDpslMtdCd(node.path("DPSL_MTD_CD").asText(""));
            item.setDpslMtdNm(node.path("DPSL_MTD_NM").asText(""));
            item.setBidMtdNm(node.path("BID_MTD_NM").asText(""));
            item.setMinBidPrc(node.path("MIN_BID_PRC").asLong());
            item.setApslAsesAvgAmt(node.path("APSL_ASES_AVG_AMT").asLong());
            item.setFeeRate(node.path("FEE_RATE").asText(""));
            
            // 날짜 필터링
            String begnDtm = node.path("PBCT_BEGN_DTM").asText("");
            if (!isValidDateRange(begnDtm, startDate, endDate)) {
                return null; // 필터링 탈락
            }
            item.setPbctBegnDtm(begnDtm);
            
            item.setPbctClsDtm(node.path("PBCT_CLS_DTM").asText(""));
            item.setPbctCltrStatNm(node.path("PBCT_CLTR_STAT_NM").asText(""));
            
            item.setUscbCnt(node.path("USCBD_CNT").asLong());
            item.setIqryCnt(node.path("IQRY_CNT").asLong());
            
            item.setGoodsNm(node.path("GOODS_NM").asText(""));
            
            // 차량 정보
            item.setManf(node.path("MANF").asText(""));
            item.setMdl(node.path("MDL").asText(""));
            item.setNrgt(node.path("NRGT").asText(""));
            item.setGrbx(node.path("GRBX").asText(""));
            item.setEndpc(node.path("ENDPC").asText(""));
            item.setVhclMlge(node.path("VHCL_MLGE").asText(""));
            item.setFuel(node.path("FUEL").asText(""));
            item.setScrtNm(node.path("SCRT_NM").asText(""));
            item.setTpbz(node.path("TPBZ").asText(""));
            item.setItmNm(node.path("ITM_NM").asText(""));
            item.setMmbRgtNm(node.path("MMB_RGT_NM").asText(""));
            
            // 지역 정보
            item.setSido(node.path("SIDO").asText(""));
            item.setSigungu(node.path("SGK").asText(""));
            if (item.getSigungu() == null || item.getSigungu().isEmpty()) {
                item.setSigungu(node.path("SGG").asText(""));
            }
            item.setEmd(node.path("EMD").asText(""));
            item.setCtgrHirkId(node.path("CTGR_HIRK_ID").asText(""));
            item.setCtgrHirkIdMid(node.path("CTGR_HIRK_ID_MID").asText(""));
            
            return item;
            
        } catch (Exception e) {
            log.warn("⚠️ 아이템 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 이미지 URL 추출
     */
    private String extractImageUrls(JsonNode node) {
        String cltrImgFilesRaw = node.path("CLTR_IMG_FILES").asText();
        List<String> imgUrlList = new ArrayList<>();
        
        if (cltrImgFilesRaw != null && !cltrImgFilesRaw.isEmpty()) {
            Pattern pattern = Pattern.compile("https?://[^<>\"]+\\.(jpg|png|jpeg|gif)");
            Matcher matcher = pattern.matcher(cltrImgFilesRaw);
            while (matcher.find()) {
                imgUrlList.add(matcher.group());
            }
        }
        
        if (!imgUrlList.isEmpty()) {
            Long plnmNo = node.path("PLNM_NO").asLong();
            log.debug("🖼️ 이미지 URL {}개 추출됨 (공고번호 {}): {}", 
                    imgUrlList.size(), plnmNo,
                    String.join(",", imgUrlList).substring(0,
                            Math.min(200, String.join(",", imgUrlList).length())));
            return String.join(",", imgUrlList);
        }
        
        return "";
    }
    
    /**
     * 날짜 범위 검증
     */
    private boolean isValidDateRange(String begnDtm, LocalDate startDate, LocalDate endDate) {
        try {
            if (begnDtm == null || begnDtm.length() != 14) {
                log.debug("⏭️ 날짜 형식 불일치로 스킵: {}", begnDtm);
                return false;
            }
            
            DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDate bidStartDate = LocalDateTime.parse(begnDtm, inFmt).toLocalDate();
            
            if (bidStartDate.isBefore(startDate) || bidStartDate.isAfter(endDate)) {
                log.debug("⏭️ 입찰시작일 범위 밖: {}", begnDtm);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.warn("⚠️ pbctBegnDtm 파싱 실패: {}", e.getMessage());
            return false;
        }
    }
}
