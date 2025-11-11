package com.easybid.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.easybid.model.EasybidItem;
import com.easybid.service.EasybidService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EasybidScheduler {

	private final EasybidService easybidService;
	
	@Value("${scheduler.easybid.enabled:true}")
    private boolean schedulerEnabled;
    
    @Value("${scheduler.easybid.max-pages:5}")
    private int maxPages;
    
    @Value("${scheduler.easybid.rows-per-page:200}")
    private int rowsPerPage;

    /**
     * 매일 새벽 03시에 공공데이터 API로부터 데이터를 가져와 DB에 저장
     * cron 표현식: 초 분 시 일 월 요일
     * "0 0 3 * * *" = 매일 03시 00분 00초
     */
    @Scheduled(cron = "${scheduler.easybid.cron:0 0 3 * * *}")
    public void scheduledFetchAndSave() {
        
        if (!schedulerEnabled) {
            log.info("ℹ️ 스케줄러가 비활성화되어 있습니다.");
            return;
        }
        log.info("🕒 [스케줄러 시작] 공공데이터 자동 수집 시작 - {}", LocalDateTime.now());
        
        try {
            int totalSaved = 0;
            int pageNo = 1;
            
            // 여러 페이지를 순회하며 데이터 수집
            for (int i = 0; i < maxPages; i++) {
                log.info("📄 페이지 {} 처리 중...", pageNo);
                
                List<EasybidItem> savedItems = easybidService.fetchAndSaveItems(pageNo, rowsPerPage);
                totalSaved += savedItems.size();
                
                log.info("✅ 페이지 {} 완료: {}개 저장", pageNo, savedItems.size());
                
                // 저장된 아이템이 없으면 더 이상 데이터가 없다고 판단
                if (savedItems.isEmpty()) {
                    log.info("ℹ️ 더 이상 저장할 데이터가 없습니다. 스케줄러 종료.");
                    break;
                }
                
                pageNo++;
                
                // API 호출 부하 방지를 위한 딜레이 (1초)
                Thread.sleep(1000);
            }
            
            log.info("✅ [스케줄러 완료] 총 {}개 아이템 저장 완료 - {}", 
                    totalSaved, LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("❌ [스케줄러 실패] 공공데이터 자동 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 수동 테스트용 메서드 (필요시 Controller에서 호출)
     * 실제 운영 환경에서는 제거하거나 주석 처리 권장
     */
    public void manualTrigger() {
        log.info("🔧 [수동 실행] 스케줄러 수동 트리거");
        scheduledFetchAndSave();
    }
}
