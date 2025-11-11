package com.easybid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easybid.scheduler.EasybidScheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerTestController {

	private final EasybidScheduler easybidScheduler;
	
	/**
     * 스케줄러 수동 실행 (테스트용)
     * 실제 운영 환경에서는 삭제하거나 관리자 권한으로 제한 필요
     * 
     * 사용법: GET /api/scheduler/trigger
     */
	@GetMapping("/trigger")
	public ResponseEntity<String> triggerScheduler() {
		 log.info("🔧 스케줄러 수동 실행 요청");
		 
		 try {
			new Thread(() -> {
				easybidScheduler.manualTrigger();
			}).start();
			
			return ResponseEntity.ok("✅ 스케줄러 실행이 시작되었습니다. 로그를 확인하세요.");
		 } catch (Exception e) {
			 log.error("❌ 스케줄러 수동 실행 실패", e);
		 }
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("❌ 스케줄러 실행 실패: \" + e.getMessage()");
	}
	
}
