package com.management.event_management_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SelfPingScheduler {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${links.url}")
	private String pingUrl;

	@Scheduled(fixedRate = 300000) // every 10 minutes
	public void pingSelf() {
		try {
			String response = restTemplate.getForObject(pingUrl, String.class);
			System.out.println("Ping URL : " + pingUrl);
			System.out.println("Self-ping response : " + response);
		} catch (Exception e) {
			System.err.println("Error during self-ping: " + e.getMessage());
		}
	}
}