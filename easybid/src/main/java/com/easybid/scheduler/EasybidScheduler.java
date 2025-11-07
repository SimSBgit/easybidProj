//package com.easybid.scheduler;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.easybid.service.EasybidService;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class EasybidScheduler {
//
//	private final EasybidService easybidService;
//	
////	매일 오전 6시 실행
//	@Scheduled(cron = "0 0 6 * * *")
//	public void updateEasybidData() {
//		log.info("🕕 스케줄러 업데이트 진행중 (06:00)...");
//		easybidService.updateEasybidData();
//	}
//}
