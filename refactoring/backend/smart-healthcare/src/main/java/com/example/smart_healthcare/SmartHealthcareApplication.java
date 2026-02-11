package com.example.smart_healthcare;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
public class SmartHealthcareApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHealthcareApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		log.info("🚀 백엔드 로드 완료");
		log.info("📡 API 서버가 정상적으로 시작되었습니다.");
		log.info("🔗 접속 URL: http://localhost:8080");
	}

}
