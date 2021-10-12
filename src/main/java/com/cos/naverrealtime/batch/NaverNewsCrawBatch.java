package com.cos.naverrealtime.batch;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cos.naverrealtime.domain.News;
import com.cos.naverrealtime.domain.NewsRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;


@RequiredArgsConstructor
@Component
public class NaverNewsCrawBatch {
	
	//8Byte
	long aidNum = 278000;
	private final NewsRepository newsRepository;
	
	// cron = 초 분 시 일 월 주
	@Scheduled(cron = "0 39 12 * * *", zone="Asia/Seoul")
	public void naverCraw() {
		int successCount = 0;
		int errorCount = 0;
		int crawCount = 0;
		
		System.out.println("배치 프로그램 시작");
		List<News> naverNewsList = new ArrayList<>(); 
		
		while(true) {
			// https://news.naver.com/main/read.naver?mode=LSD&mid=shm&sid1=103&oid=437&aid=0000277493
			String aid = String.format("%010d", aidNum);
			String url = "https://news.naver.com/main/read.naver?mode=LSD&mid=shm&sid1=103&oid=437&aid=" + aid;
			
			try {
				Document doc = Jsoup.connect(url).get();
				
				String company = doc.selectFirst(".press_logo img").attr("alt");
				String title = doc.selectFirst("#articleTitle").text();
				String createdAtStr = doc.selectFirst(".t11").text();
				
				LocalDate today = LocalDate.now();
				LocalDate yesterday = today.minusDays(1);
				
				createdAtStr = createdAtStr.substring(0,10);
				createdAtStr = createdAtStr.replace(".", "-");
				
				if(today.toString().equals(createdAtStr)) {
					break;
				}
				
				if(yesterday.toString().equals(createdAtStr)) { // List 컬렉션에 모았다가 DB에 save 하기
					naverNewsList.add(News.builder()
							.company(company)
							.title(title)
							.createdAt(Timestamp.valueOf( LocalDateTime.now().minusDays(1).plusHours(9) ))
							.build()
					);
					successCount++;
				}
			} catch (Exception e) {
				
				System.out.println("크롤링 오류 - 없는 기사 aid : " + aidNum);
				errorCount++;
			}
			aidNum++;
		} // while 끝
		System.out.println("배치 프로그램 종료 ===================");
		System.out.println("성공 횟수 : " + successCount);
		System.out.println("실패 횟수 : " + errorCount);
		System.out.println("크롤링 횟수 : " + crawCount);
		System.out.println("크롤링 끝내기");
		
		System.out.println("컬렉션에 담은 크기 : " + naverNewsList.size());
		//﻿ -- 배치 프로그램에서 DB에 저장할수 없는 이유
		//배치스래드는 동기 서버다. 동기서버이기 때문에 약속,어음을 받을수 없다.
		//때문에 DB(비동기)에 저장등을 할수 없다(프로토콜이 다르다).
		// newsRepository.saveAll(naverNewsList); > X
		
		Flux.fromIterable(naverNewsList)
			.flatMap(newsRepository::save)
			.subscribe();
		// 비동기 서버로 명령을 넘겨준다.
		// flatMap() > 매개변수에 함수를 적어야 한다. :: 를 사용하여 함수를 집어넣을수 있다.
		// subscribe() > 스프링 웹플럭스 서버에 스레드를 구독
		
	}
}
