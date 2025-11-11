package com.easybid.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.easybid.mapper.EasybidMapper;
import com.easybid.model.EasybidItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EasybidService {

	private final EasybidMapper easybidMapper;
	private final EasybidParserService easybidParserService;
	private final EasybidApiService easybidApiService;

	@Value("${openapi.easybid.url}")
	private String baseUrl;

	@Value("${openapi.easybid.serviceKey}")
	private String serviceKey;

    /**
     * 공공데이터 API로부터 데이터를 가져와 DB에 저장
     */
    @Transactional
    public List<EasybidItem> fetchAndSaveItems(int pageNo, int numOfRows) throws Exception {
        
        // 날짜 범위 계산
        DateRange dateRange = calculateDateRange();
        
        // API 호출
        String xmlResponse = easybidApiService.fetchXmlData(pageNo, numOfRows);
        
        // XML 파싱 (날짜 필터링 + 최신 공매 필터링 포함)
        Map<Long, EasybidItem> latestItemsMap = easybidParserService.parseXmlToItems(
                xmlResponse, dateRange.getStartDate(), dateRange.getEndDate());
        
        // DB 저장
        List<EasybidItem> savedItems = saveItemsToDatabase(latestItemsMap);
        
        log.info("✅ DB 저장 완료. 저장된 아이템 수: {}", savedItems.size());
        
        return savedItems;
    }
    
    /**
     * 날짜 범위 계산 (오늘 기준 60일 이전 ~ 30일 이후)
     */
    private DateRange calculateDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(60);
        LocalDate endDate = today.plusDays(30);
        
        return new DateRange(startDate, endDate);
    }
    
    /**
     * 파싱된 아이템들을 DB에 저장 (중복 체크 포함)
     */
    private List<EasybidItem> saveItemsToDatabase(Map<Long, EasybidItem> latestItemsMap) {
        List<EasybidItem> savedItems = new ArrayList<>();
        
        for (EasybidItem item : latestItemsMap.values()) {
            try {
                if (shouldSaveItem(item)) {
                    saveItem(item);
                    savedItems.add(item);
                    log.info("✅ 저장 완료: 공고번호={}, 공매번호={}, 물건명={}", 
                            item.getPlnmNo(), item.getPbctNo(), item.getCltrNm());
                } else {
                    log.info("⏭️ 저장 스킵: 공고번호={}, 공매번호={}", 
                            item.getPlnmNo(), item.getPbctNo());
                }
            } catch (Exception e) {
                log.error("❌ DB 저장 실패: 공고번호={}, 공매번호={}, 오류={}", 
                        item.getPlnmNo(), item.getPbctNo(), e.getMessage());
            }
        }
        
        return savedItems;
    }
    
    /**
     * 아이템 저장 가능 여부 확인 (중복 체크)
     */
    private boolean shouldSaveItem(EasybidItem item) {
        // 정확히 같은 공고번호 + 공매번호가 있는지 확인
        EasybidItem existingInDb = easybidMapper.findByPlnmNoAndPbctNo(
                item.getPlnmNo(), item.getPbctNo());
        
        if (existingInDb != null) {
            log.info("⏭️ 이미 존재: 공고번호={}, 공매번호={}", 
                    item.getPlnmNo(), item.getPbctNo());
            return false;
        }
        
        // 같은 공고번호에 더 최신 공매가 있는지 확인
        EasybidItem existingByPlnm = easybidMapper.findLatestByPlnmNo(item.getPlnmNo());
        if (existingByPlnm != null && existingByPlnm.getPbctNo() >= item.getPbctNo()) {
            log.info("⏭️ 더 최신 공매가 DB에 존재: 공고번호={}, 기존={}, 신규={}", 
                    item.getPlnmNo(), existingByPlnm.getPbctNo(), item.getPbctNo());
            return false;
        }
        
        return true;
    }
    
    /**
     * 날짜 범위를 표현하는 내부 클래스
     */
    @Getter
    @AllArgsConstructor
    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;
    }
    
//	콘솔에서 공공데이터 api 바로 출력
	public void fetchAndPrintApi(int pageNo, int numOfRows) {
		try {
			// ✅ 1. API URL 구성
			String apiUrl = baseUrl + "?serviceKey=" + serviceKey + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows;

			log.info("📡 요청 URL: {}", apiUrl);

			// ✅ 2. API 호출
			RestTemplate restTemplate = new RestTemplate();
			String xmlResponse = restTemplate.getForObject(apiUrl, String.class);

			// ✅ 3. XML 응답 확인
			if (xmlResponse == null || xmlResponse.isEmpty()) {
				log.warn("⚠️ 응답 XML이 비어 있습니다!");
				return;
			}

			// ✅ 4. XML 일부 출력 (너무 크면 콘솔 버벅일 수 있으므로 앞부분만)
			log.info("📄 응답 XML (앞부분 미리보기): \n{}", xmlResponse.substring(0, Math.min(1500, xmlResponse.length())));

			// ✅ 5. XML → JSON 변환
			XmlMapper xmlMapper = new XmlMapper();
			JsonNode root = xmlMapper.readTree(xmlResponse);

			JsonNode items = root.path("body").path("items").path("item");

			log.info("📦 item 노드 개수: {}", items.isArray() ? items.size() : 0);

			if (items.isArray()) {
				for (JsonNode node : items) {
					Long plnmNo = node.path("PLNM_NO").asLong();
					Long pbctNo = node.path("PBCT_NO").asLong();
					String cltrNm = node.path("CLTR_NM").asText("");
					String imgInfo = node.path("CLTR_IMG_FILES").toString();

					log.info("📌 공고번호: {}, 공매번호: {}, 물건명: {}", plnmNo, pbctNo, cltrNm);
					log.info("🖼️ 이미지정보: {}", imgInfo);
				}
			}

		} catch (Exception e) {
			log.error("❌ API 호출/파싱 실패: {}", e.getMessage(), e);
		}
	}
	
	public List<EasybidItem> getAll(int offset, int numOfRows) {
		return easybidMapper.findPagedAll(offset, numOfRows);
	}

	public int getTotalCount() {
		return easybidMapper.getTotalCount();
	}

	public EasybidItem findById(Long id) {
		return easybidMapper.getDetails(id);
	}

	public EasybidItem findByUuid(String uuid) {
		return easybidMapper.findUuid(uuid);
	}
	
    /**
     * 아이템 저장
     */
	public void saveItem(EasybidItem item) {
        // UUID가 비어있다면 새로 생성
        if (item.getUuid() == null || item.getUuid().isEmpty()) {
            item.setUuid(UUID.randomUUID().toString());
        }
        easybidMapper.insert(item);
    }

	// 샘플 API URL (임시)
//  private final String apiUrl =
//      "http://openapi.onbid.co.kr/openapi/services/KamcoPblsalThingInquireSvc/getKamcoPbctCltrList"
//      + "?serviceKey=273f45187071c8be25359787b100033ecd7addb7ab2b533878d80dd80dcf4fdb&pageNo=1&numOfRows=5&DPSL_MTD_CD=0001";
	
		// XML → JSON → DB 저장
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

	// 하단의 fetchAndSaveItems() 메서드를 역할 별로 분리했음.
	
	//  XML → DB 저장
//	@Transactional
//	public List<EasybidItem> fetchAndSaveItems(int pageNo, int numOfRows) throws Exception {
//
//		// 오늘 날짜 기준
//		LocalDate sevenMonthsAgo = LocalDate.now();
//
//		// 기준일 기준 60일 이전 + 30일 이후
//		LocalDate startDate = sevenMonthsAgo.minusDays(60);
//		LocalDate endDate = sevenMonthsAgo.plusDays(30);
//
////		공공 데이터의 경우 inqStrtDt / inqEndDt을 기간 호출용 파라미터로 사용하는 경우 있음(꼭 기간 파라미터인 것은 아님).
////		&pageNo=1&numOfRows=5&inqStrtDt=20240501&inqEndDt=20240514
//		
//		// API 호출용 포맷
////		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
////		String start = startDate.format(formatter);
////		String end = endDate.format(formatter);
//
//		// api호출 로직은 EasybidApiService으로 분리
////		String apiUrl = baseUrl 
////				+ "?serviceKey=" + serviceKey 
////				+ "&pageNo=" + pageNo 
////				+ "&numOfRows=" + numOfRows;
////
////		log.info("요청 URL: " + apiUrl);
////
////		RestTemplate restTemplate = new RestTemplate();
////		String xmlResponse = restTemplate.getForObject(apiUrl, String.class);
//		
//		// ✅ 응답 XML 일부 출력 (디버깅용)
////		if (xmlResponse != null && !xmlResponse.isEmpty()) {
////				log.info("📄 응답 XML: {}", xmlResponse.substring(0, Math.min(500, xmlResponse.length())));
////		} else {
////				log.warn("⚠️ 응답 XML이 비어있습니다!");
////		}
//		
//		String xmlResponse = easybidApiService.fetchXmlData(pageNo, numOfRows);
//
//		List<EasybidItem> list = new ArrayList<>();
//
//		// 🔹 공고번호별로 최신 공매번호만 저장하기 위한 Map
//		Map<Long, EasybidItem> latestItemsMap = new HashMap<>();
//
//		try {
//
//			XmlMapper xmlMapper = new XmlMapper();
//			JsonNode root = xmlMapper.readTree(xmlResponse);
//			JsonNode items = root.path("body").path("items").path("item");
//
//			log.info("📦 아이템 노드 수: {}", items.isArray() ? items.size() : 0);
//
//			if (items.isArray()) {
//				for (JsonNode node : items) {
//					EasybidItem item = new EasybidItem();
//
//					Long plnmNo = node.path("PLNM_NO").asLong();
//					Long pbctNo = node.path("PBCT_NO").asLong();
//
//					item.setPlnmNo(plnmNo);
//					item.setPbctNo(pbctNo);
//
//					// ✅ 공공데이터 XML에서 이미지 URL 문자열 추출
//					String cltrImgFilesRaw = node.path("CLTR_IMG_FILES").asText();
//
//					// 예: <CLTR_IMG_FILES>...</CLTR_IMG_FILES> 여러 개가 들어있을 수 있음
//					List<String> imgUrlList = new ArrayList<>();
//
//					if (cltrImgFilesRaw != null && !cltrImgFilesRaw.isEmpty()) {
//						// 태그 안의 URL만 추출 (정규식 사용)
//						Pattern pattern = Pattern.compile("https?://[^<>\"]+\\.(jpg|png|jpeg|gif)");
//						Matcher matcher = pattern.matcher(cltrImgFilesRaw);
//						while (matcher.find()) {
//							imgUrlList.add(matcher.group());
//						}
//					}
//
//					// ✅ ","로 구분해서 DB에 저장 (DB VARCHAR 컬럼)
//					if (!imgUrlList.isEmpty()) {
//						item.setCltrImgFiles(String.join(",", imgUrlList));
//						log.debug("🖼️ 이미지 URL {}개 추출됨 (공고번호 {}): {}", imgUrlList.size(), plnmNo,
//								String.join(",", imgUrlList).substring(0,
//										Math.min(200, String.join(",", imgUrlList).length())));
//
//					} else {
//						item.setCltrImgFiles("");
//					}
//
//					item.setPbctCdtnNo(node.path("PBCT_CDTN_NO").asLong());
//					item.setCltrNo(node.path("CLTR_NO").asLong());
//					item.setCltrHstrNo(node.path("CLTR_HSTR_NO").asLong());
//
//					item.setScrnGrpCd(node.path("SCRN_GRP_CD").asText(""));
//					item.setCtgrFullNm(node.path("CTGR_FULL_NM").asText(""));
//					item.setBidMnmtNo(node.path("BID_MNMT_NO").asText(""));
//
//					item.setCltrNm(node.path("CLTR_NM").asText(""));
//					item.setCltrMnmtNo(node.path("CLTR_MNMT_NO").asText(""));
//					item.setLdnmAdrs(node.path("LDNM_ADRS").asText(""));
//					item.setNmrddAdrs(node.path("NMRD_ADRS").asText(""));
//					item.setLdnmPnu(node.path("LDNM_PNU").asText(""));
//
//					item.setDpslMtdCd(node.path("DPSL_MTD_CD").asText(""));
//					item.setDpslMtdNm(node.path("DPSL_MTD_NM").asText(""));
//					item.setBidMtdNm(node.path("BID_MTD_NM").asText(""));
//					item.setMinBidPrc(node.path("MIN_BID_PRC").asLong());
//					item.setApslAsesAvgAmt(node.path("APSL_ASES_AVG_AMT").asLong());
//					item.setFeeRate(node.path("FEE_RATE").asText(""));
//
//					item.setPbctBegnDtm(node.path("PBCT_BEGN_DTM").asText(""));
//					item.setPbctClsDtm(node.path("PBCT_CLS_DTM").asText(""));
//					item.setPbctCltrStatNm(node.path("PBCT_CLTR_STAT_NM").asText(""));
//
//					item.setUscbCnt(node.path("USCBD_CNT").asLong());
//					item.setIqryCnt(node.path("IQRY_CNT").asLong());
//
//					item.setGoodsNm(node.path("GOODS_NM").asText(""));
//
//					item.setManf(node.path("MANF").asText(""));
//					item.setMdl(node.path("MDL").asText(""));
//					item.setNrgt(node.path("NRGT").asText(""));
//					item.setGrbx(node.path("GRBX").asText(""));
//					item.setEndpc(node.path("ENDPC").asText(""));
//					item.setVhclMlge(node.path("VHCL_MLGE").asText(""));
//					item.setFuel(node.path("FUEL").asText(""));
//					item.setScrtNm(node.path("SCRT_NM").asText(""));
//					item.setTpbz(node.path("TPBZ").asText(""));
//					item.setItmNm(node.path("ITM_NM").asText(""));
//					item.setMmbRgtNm(node.path("MMB_RGT_NM").asText(""));
//
//					item.setSido(node.path("SIDO").asText(""));
//					item.setSigungu(node.path("SGK").asText(""));
//					if (item.getSigungu() == null || item.getSigungu().isEmpty()) {
//						item.setSigungu(node.path("SGG").asText(""));
//					}
//					item.setEmd(node.path("EMD").asText(""));
//					item.setCtgrHirkId(node.path("CTGR_HIRK_ID").asText(""));
//					item.setCtgrHirkIdMid(node.path("CTGR_HIRK_ID_MID").asText(""));
//
//			        // 🔹 날짜 필터링 시작
//			        try {
//			            String begnDtm = node.path("PBCT_BEGN_DTM").asText("");
//			            if (begnDtm == null || begnDtm.length() != 14) {
//			                log.debug("⏭️ 날짜 형식 불일치로 스킵: {}", begnDtm);
//			                continue; // 필터링 탈락
//			            }
//
//			            DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//			            LocalDate bidStartDate = LocalDateTime.parse(begnDtm, inFmt).toLocalDate();
//
//			            // ✅ 오늘 기준 60일 이전 / 30일 이후 범위 검사
//			            if (bidStartDate.isBefore(startDate) || bidStartDate.isAfter(endDate)) {
//			                log.debug("⏭️ 입찰시작일 범위 밖: {}", begnDtm);
//			                continue;
//			            }
//
//			            item.setPbctBegnDtm(begnDtm);
//
//			        } catch (Exception e) {
//			            log.warn("⚠️ pbctBegnDtm 파싱 실패: {}", e.getMessage());
//			            continue;
//			        }
//			        // 🔹 날짜 필터링 끝
//			        
//					// 🔹 같은 공고번호 중 공매번호가 큰 것만 유지 (최신 공매)
//					EasybidItem existing = latestItemsMap.get(plnmNo);
//					if (existing == null || existing.getPbctNo() < pbctNo) {
//						latestItemsMap.put(plnmNo, item);
//						log.debug("🔄 공고번호 {} - 공매번호 {} 업데이트", plnmNo, pbctNo);
//					} else {
//						log.debug("⏭️ 공고번호 {} - 공매번호 {} 스킵 (더 최신 {}가 있음)", plnmNo, pbctNo, existing.getPbctNo());
//					}
//				}
//
//				// 🔹 DB 중복 확인 + 저장
//				for (EasybidItem item : latestItemsMap.values()) {
//					try {
//						EasybidItem existingInDb = easybidMapper.findByPlnmNoAndPbctNo(item.getPlnmNo(),
//								item.getPbctNo());
//
//						// ✅ 공고번호만 같은 다른 공매 중복 방지
//						EasybidItem existingByPlnm = easybidMapper.findLatestByPlnmNo(item.getPlnmNo());
//						if (existingByPlnm != null && existingByPlnm.getPbctNo() >= item.getPbctNo()) {
//							log.info("⏭️ 더 최신 공매가 DB에 존재: 공고번호={}, 기존={}, 신규={}", item.getPlnmNo(),
//									existingByPlnm.getPbctNo(), item.getPbctNo());
//							continue;
//						}
//
//						if (existingInDb == null) {
//							saveItem(item); // easybidMapper.insert(item) 대신
//							list.add(item);
//							log.info("✅ 저장 완료: 공고번호={}, 공매번호={}, 물건명={}", item.getPlnmNo(), item.getPbctNo(),
//									item.getCltrNm());
//						} else {
//							log.info("⏭️ 이미 존재: 공고번호={}, 공매번호={}", item.getPlnmNo(), item.getPbctNo());
//						}
//
//					} catch (Exception e) {
//						log.error("❌ DB 저장 실패: 공고번호={}, 공매번호={}, 오류={}", item.getPlnmNo(), item.getPbctNo(),
//								e.getMessage());
//					}
//				}
//			} else {
//				log.warn("⚠️ items 노드가 배열이 아닙니다. XML 구조를 확인하세요.");
//			}
//		} catch (Exception e) {
//			log.error("❌ XML 파싱 실패: {}", e.getMessage());
//		}
//
//		log.info("✅ DB 저장 완료. 저장된 아이템 수: {}", list.size());
//		log.info("📊 최신 공매 필터링 후: {}개", latestItemsMap.size());
//
//		return list;
//	}

//    콘솔에서 DB API 출력
//    public void printApiResponse(int pageNo, int numOfRows) {
//        try {
//            // ✅ 1. URL 생성
//            String apiUrl = baseUrl
//                    + "?serviceKey=" + serviceKey
//                    + "&pageNo=" + pageNo
//                    + "&numOfRows=" + numOfRows;
//
//            log.info("📡 요청 URL: {}", apiUrl);
//
//            // ✅ 2. API 호출
//            RestTemplate restTemplate = new RestTemplate();
//            String xmlResponse = restTemplate.getForObject(apiUrl, String.class);
//
//            // ✅ 3. 응답 XML 콘솔 출력
//            if (xmlResponse != null && !xmlResponse.isEmpty()) {
//                log.info("📄 전체 응답 XML (앞부분 미리보기): \n{}", 
//                        xmlResponse.substring(0, Math.min(1500, xmlResponse.length())));
//            } else {
//                log.warn("⚠️ 응답 XML이 비어있습니다!");
//            }
//
//            // ✅ 4. (선택) 특정 노드 확인
//            XmlMapper xmlMapper = new XmlMapper();
//            JsonNode root = xmlMapper.readTree(xmlResponse);
//            JsonNode items = root.path("body").path("items").path("item");
//
//            log.info("📦 item 노드 개수: {}", items.isArray() ? items.size() : 0);
//
//            if (items.isArray()) {
//                for (JsonNode node : items) {
//                    JsonNode imgNode = node.path("CLTR_IMG_FILES");
//                    log.info("🖼️ 이미지 노드 데이터: {}", imgNode.toString());
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("❌ API 호출/파싱 실패: {}", e.getMessage());
//        }
//    }

//	public List<EasybidItem> getAll() {
//		return easybidMapper.findAll();
//	}

}
