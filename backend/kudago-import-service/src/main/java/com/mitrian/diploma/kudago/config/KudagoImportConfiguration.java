package com.mitrian.diploma.kudago.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(KudagoImportProperties.class)
public class KudagoImportConfiguration {

	@Bean(name = "kudagoRestClient")
	RestClient kudagoRestClient() {
		return RestClient.builder().build();
	}
}
