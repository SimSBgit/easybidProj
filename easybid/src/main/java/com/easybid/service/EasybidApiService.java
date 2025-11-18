package com.easybid.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EasybidApiService {

//	api 호출용 비즈니스 로직
	private WebClient webClient;
	private final String serviceKey;
	
	@Value("${openapi.easybid.url}")
	private String baseUrl;
	
//	생성자 직접 생성, 기본 생성자는 @RequiredArgsConstructor 사용 가능.
	public EasybidApiService(WebClient.Builder builder,
			@Value("${openapi.easybid.url}") String baseUrl,
			@Value("${openapi.easybid.serviceKey}") String serviceKey) {
		
		this.webClient = builder.baseUrl(baseUrl).build();
		this.serviceKey = serviceKey;
	}
	
//	WebClient를 사용한 비동기+논블로킹 방식 api 호출
	public String fetchEasybidData(int numOfRows, int pageNo) {
		
		return webClient.get().uri(uriBuilder -> uriBuilder
				// .path는 이미 baseUrl에 포함되어 있으므로 추가하지 않습니다.
                // 필요한 경우, 여기에 .path("추가경로")를 넣을 수 있습니다.
				.queryParam("serviceKey", serviceKey)
				.queryParam("numOfRows", numOfRows)
				.queryParam("pageNo", pageNo).build())
				.retrieve() // 응답을 검색(Retrieve)
				.bodyToMono(String.class) // 응답 본문을 String 타입의 Mono(0 또는 1개 데이터)로 받음
				.block(); // 비동기 작업의 완료를 기다리고 결과를 블로킹 방식으로 반환
	}
	
//	RestTemplate를 사용한 동기+블로킹 방식 api 호출
	public String fetchXmlData(int pageNo, int numOfRows) {

		String apiUrl = baseUrl 
				+ "?serviceKey=" + serviceKey 
				+ "&pageNo=" + pageNo 
				+ "&numOfRows=" + numOfRows;

		log.info("요청 URL: " + apiUrl);

		RestTemplate restTemplate = new RestTemplate();
		String xmlResponse = restTemplate.getForObject(apiUrl, String.class);
		
		if (xmlResponse != null && !xmlResponse.isEmpty()) {
			log.info("📄 응답 XML: {}", xmlResponse.substring(0, Math.min(500, xmlResponse.length())));
		} else {
			log.warn("⚠️ 응답 XML이 비어있습니다!");
		}

		return xmlResponse;
	}
	
	
}
