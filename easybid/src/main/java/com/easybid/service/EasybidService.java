package com.easybid.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.easybid.mapper.EasybidMapper;
import com.easybid.model.EasybidItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EasybidService {
//	
//    private final EasybidMapper easybidMapper;
//
//    // 샘플 API URL (임시)
//    private final String apiUrl =
//        "http://openapi.onbid.co.kr/openapi/services/KamcoPblsalThingInquireSvc/getKamcoPbctCltrList"
//        + "?serviceKey=273f45187071c8be25359787b100033ecd7addb7ab2b533878d80dd80dcf4fdb&pageNo=1&numOfRows=5&DPSL_MTD_CD=0001";
//
//    // XML → JSON → DB 저장
//    public List<EasybidItem> fetchAndSaveItems() throws Exception {
//        RestTemplate restTemplate = new RestTemplate();
//        String xmlResponse = restTemplate.getForObject(apiUrl, String.class);
//
//        XmlMapper xmlMapper = new XmlMapper();
//        JsonNode root = xmlMapper.readTree(xmlResponse);
//        JsonNode items = root.path("body").path("items").path("item");
//
//        List<EasybidItem> list = new ArrayList<>();
//
//        if (items.isArray()) {
//            for (JsonNode node : items) {
//                EasybidItem item = new EasybidItem();
//                item.setCltrNo(node.path("cltrNo").asLong());
//                item.setCltrNm(node.path("cltrNm").asText(""));
//                item.setApslAsesAvgAmt(node.path("apslAsesAvgAmt").asLong());
//                item.setMinBidPrc(node.path("minBidPrc").asLong());
//                item.setPbctClsDtm(node.path("pbctClsDtm").asText(""));
//
//                easybidMapper.insert(item);
//                list.add(item);
//            }
//        }
//
//        return list;
//    }
//
//    public List<EasybidItem> getAll() {
//        return easybidMapper.findAll();
//    }

    private final EasybidMapper easybidMapper;

    // 샘플 API URL (임시)
    @Value("${openapi.easybid.url}")
    private String baseUrl;

    @Value("${openapi.easybid.serviceKey}")
    private String serviceKey;
    
    // XML → DB 저장
    @Transactional
    public List<EasybidItem> fetchAndSaveItems(int pageNo, int numOfRows) throws Exception {
    	
    	String apiUrl = baseUrl
                + "?serviceKey=" + serviceKey
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows;

        log.info("요청 URL: " + apiUrl);
    	
        RestTemplate restTemplate = new RestTemplate();
        String xmlResponse = restTemplate.getForObject(apiUrl, String.class);

     // ✅ 응답 XML 일부 출력 (디버깅용)
        if (xmlResponse != null && !xmlResponse.isEmpty()) {
        	log.info("📄 응답 XML: {}", xmlResponse.substring(0, Math.min(500, xmlResponse.length())));
        } else {
            log.warn("⚠️ 응답 XML이 비어있습니다!");
        }
        
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode root = xmlMapper.readTree(xmlResponse);
        JsonNode items = root.path("body").path("items").path("item");

        log.info("📦 아이템 노드 수: {}", items.isArray() ? items.size() : 0);
       
        List<EasybidItem> list = new ArrayList<>();

        // 🔹 공고번호별로 최신 공매번호만 저장하기 위한 Map
        Map<Long, EasybidItem> latestItemsMap = new HashMap<>();
        
        if (items.isArray()) {
            for (JsonNode node : items) {
                EasybidItem item = new EasybidItem();

                Long plnmNo = node.path("PLNM_NO").asLong();
                Long pbctNo = node.path("PBCT_NO").asLong();
                
                item.setPlnmNo(plnmNo);
                item.setPbctNo(pbctNo);
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

                item.setPbctBegnDtm(node.path("PBCT_BEGN_DTM").asText(""));
                item.setPbctClsDtm(node.path("PBCT_CLS_DTM").asText(""));
                item.setPbctCltrStatNm(node.path("PBCT_CLTR_STAT_NM").asText(""));

                item.setUscbCnt(node.path("USCBD_CNT").asLong());
                item.setIqryCnt(node.path("IQRY_CNT").asLong());

                item.setGoodsNm(node.path("GOODS_NM").asText(""));

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

                item.setSido(node.path("SIDO").asText(""));
                item.setSigungu(node.path("SGK").asText(""));
                if (item.getSigungu() == null || item.getSigungu().isEmpty()) {
                    item.setSigungu(node.path("SGG").asText(""));
                }
                item.setEmd(node.path("EMD").asText(""));
                item.setCtgrHirkId(node.path("CTGR_HIRK_ID").asText(""));
                item.setCtgrHirkIdMid(node.path("CTGR_HIRK_ID_MID").asText(""));
                
                // 🔹 같은 공고번호 중 공매번호가 큰 것만 유지 (최신 공매)
                EasybidItem existing = latestItemsMap.get(plnmNo);
                if (existing == null || existing.getPbctNo() < pbctNo) {
                    latestItemsMap.put(plnmNo, item);
                    log.debug("🔄 공고번호 {} - 공매번호 {} 업데이트", plnmNo, pbctNo);
                } else {
                    log.debug("⏭️ 공고번호 {} - 공매번호 {} 스킵 (더 최신 {}가 있음)", plnmNo, pbctNo, existing.getPbctNo());
                }
            }
            
            // 🔹 최신 공매만 DB에 저장 (중복 방지)
            for (EasybidItem item : latestItemsMap.values()) {
                try {
                    // DB에 이미 존재하는지 확인
                    EasybidItem existingInDb = easybidMapper.findByPlnmNoAndPbctNo(
                        item.getPlnmNo(), 
                        item.getPbctNo()
                    );
                    
                    if (existingInDb == null) {
                        easybidMapper.insert(item);
                        list.add(item);
                        log.info("✅ 새로운 물건 저장: 공고번호={}, 공매번호={}, 물건명={}", 
                            item.getPlnmNo(), item.getPbctNo(), item.getCltrNm());
                    } else {
                        log.info("⏭️ 이미 존재하는 물건 스킵: 공고번호={}, 공매번호={}", 
                            item.getPlnmNo(), item.getPbctNo());
                    }
                } catch (Exception e) {
                    log.error("❌ 저장 실패: 공고번호={}, 공매번호={}, 오류={}", 
                        item.getPlnmNo(), item.getPbctNo(), e.getMessage());
                }
            }
        } else {
            log.warn("⚠️ items 노드가 배열이 아닙니다. XML 구조를 확인하세요.");
        }
        
        log.info("✅ DB 저장 완료. 저장된 아이템 수: {}", list.size());
        log.info("📊 API에서 받은 전체: {}개, 최신 공매로 필터링: {}개, 실제 저장: {}개", 
            items.isArray() ? items.size() : 0, 
            latestItemsMap.size(), 
            list.size());
        
        return list;
    }

    public List<EasybidItem> getAll() {
        return easybidMapper.findAll();
    }
}
