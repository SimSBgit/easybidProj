package com.easybid;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.easybid.mapper")
@SpringBootApplication
public class EasybidApplication /*implements CommandLineRunner*/ {

	public static void main(String[] args) {
		SpringApplication.run(EasybidApplication.class, args);
	}

//	콘솔에서 호출한 api 데이터 출력
	
//	@Override
//    public void run(String... args) {
//        String url = "http://openapi.onbid.co.kr/openapi/services/KamcoPblsalThingInquireSvc/getKamcoPbctCltrList"
//                   + "?serviceKey=273f45187071c8be25359787b100033ecd7addb7ab2b533878d80dd80dcf4fdb"
//                   + "&pageNo=1&numOfRows=5";
//
//        RestTemplate restTemplate = new RestTemplate();
//        String response = restTemplate.getForObject(url, String.class);
//
//        System.out.println("📡 API Response: ");
//        System.out.println(response);
//    }
	
}
