package com.easybid.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easybid.model.EasybidItem;
import com.easybid.service.EasybidService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/easybid")
public class EasybidRestController {

	private final EasybidService easybidService;

//	콘솔에서 공공데이터 api 곧바로 출력
	  @GetMapping("/test")
	    public String testApi(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
	    		@RequestParam(name = "numOfRows", defaultValue = "5") int numOfRows) {
	        easybidService.fetchAndPrintApi(pageNo, numOfRows);
	        return "✅ 콘솔에서 API 응답 내용을 확인하세요.";
	    }
	
//	@GetMapping("/fetch")
//	public List<EasybidItem> fetchAndSave() throws Exception {
//		return easybidService.fetchAndSaveItems();
//	}
	
//    @GetMapping(value = "/fetch", 
//    		produces = MediaType.APPLICATION_JSON_VALUE)
//    public List<EasybidItem> fetchAndSave(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
//    	@RequestParam(name = "numOfRows", defaultValue = "5") int numOfRows) throws Exception {
//        return easybidService.fetchAndSaveItems(pageNo, numOfRows);
//    }

	@GetMapping(value = "/fetchAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<EasybidItem> fetchAllPages() throws Exception {
	    int totalPages = 5; // 5페이지 * 20행 = 100개
	    int rowsPerPage = 20;

	    List<EasybidItem> allItems = new ArrayList<>();

	    for (int page = 1; page <= totalPages; page++) {
	        log.info("📡 페이지 {} 호출 중...", page);
	        List<EasybidItem> pageItems = easybidService.fetchAndSaveItems(page, rowsPerPage);
	        allItems.addAll(pageItems);

	        Thread.sleep(500); // 0.5초 대기 (공공데이터 서버 부하 방지)
	    }

	    log.info("✅ 총 저장된 데이터 수: {}", allItems.size());
	    return allItems;
	}
//
	// DB 데이터 100개 조회 
	@GetMapping(value = "/items",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public List<EasybidItem> getItems() {
		return easybidService.getAll();
	}
	
//	콘솔에서 DB API 출력
//	@GetMapping("/printApi")
//    public String printApi(@RequestParam(defaultValue = "1") int pageNo,
//                           @RequestParam(defaultValue = "10") int numOfRows) {
//        easybidService.printApiResponse(pageNo, numOfRows);
//        return "✅ 콘솔에서 API 응답을 확인하세요!";
//    }
}
