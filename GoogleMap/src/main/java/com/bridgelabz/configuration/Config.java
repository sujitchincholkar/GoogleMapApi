package com.bridgelabz.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bridgelabz.service.GoogleMapService;
import com.bridgelabz.util.Utility;

@Configuration
public class Config {
	
	@Bean
	public GoogleMapService getService() {
		return new GoogleMapService();
	}
	

	@Bean
	public Utility getUtil() {
		return new Utility();
	}
}
