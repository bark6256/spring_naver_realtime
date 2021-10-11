package com.cos.naverrealtime.domain;

import java.sql.Timestamp;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Document(collection = "naver_realtime")
public class News {
	@Id
	private String _id;
	
	private String company;
	private String title;
	private String createdAt;
}