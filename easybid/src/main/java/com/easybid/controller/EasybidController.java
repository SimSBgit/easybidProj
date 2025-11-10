package com.easybid.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.easybid.model.EasybidItem;
import com.easybid.service.EasybidService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/easybid")
public class EasybidController {
	
	private final EasybidService easybidService;
	
//	메인 페이지 렌더링(최초 1페이지 로드)
	@GetMapping
	public String getMain(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
						  @RequestParam(name = "numOfRows", defaultValue = "9") int numOfRows, 
						  Model model) {
		int offset = (pageNo - 1) * numOfRows;
		List<EasybidItem> items = easybidService.getAll(offset, numOfRows);
		int totalCount = easybidService.getTotalCount();
	    int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
		
	    model.addAttribute("totalCount", totalCount);
	    model.addAttribute("items", items);
	    model.addAttribute("currentPage", pageNo);
	    model.addAttribute("totalPages", totalPages);
	    
		return "easybidMain";
	}

//	AJAX로 페이지 데이터만 불러오기
	@GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> getItems(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
    						@RequestParam(name = "numOfRows", defaultValue = "9") int numOfRows) {
		int offset = (pageNo - 1) * numOfRows;
		
		// 1️. 현재 페이지 데이터
        List<EasybidItem> items = easybidService.getAll(offset, numOfRows);
        
        // 2️. 전체 데이터 개수 (총 페이지 계산용)
        int totalCount = easybidService.getTotalCount();
        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("totalPages", totalPages);
        
		return response;
	}
	
//	상세 페이지
	@GetMapping(value = "/items/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String viewItemDetail(@PathVariable("uuid") String uuid, Model model) {
		log.info("요청받은 UUID: {} ", uuid);
	    EasybidItem item = easybidService.findByUuid(uuid);
	    log.info("조회된 item: {}", item);
	    model.addAttribute("item", item);
	    return "details";
	}

}
