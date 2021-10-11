package com.cos.naverrealtime.batch;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cos.naverrealtime.domain.News;
import com.cos.naverrealtime.domain.NewsRepository;
import com.cos.naverrealtime.util.NaverNewsCraw;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Component
public class NaverNewsCrawBatch {
	
	private final NaverNewsCraw naverNewsCraw;
	private final NewsRepository newsRepository;
	
	@Scheduled(fixedDelay = 1000 * 60 * 5)
	public void NewsCrawAndSave() {
		List<News> newsList = naverNewsCraw.newsCraw5();
		
		newsRepository.saveAll(newsList);
	}
}
