package com.bridgelabz.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bridgelabz.service.MapService;
import com.bridgelabz.service.MapServiceImpl;
import com.bridgelabz.util.RestCallUtility;


@Configuration
public class Config {
	
	@Bean
	public MapService getService() {
		return new MapServiceImpl();
	}
	
	@Bean
	public RestCallUtility getUtil() {
		return new RestCallUtility();
	}
	
}
