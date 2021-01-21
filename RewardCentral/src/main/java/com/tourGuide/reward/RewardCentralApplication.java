package com.tourGuide.reward;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import rewardCentral.RewardCentral;

@SpringBootApplication
public class RewardCentralApplication {

	public static void main(String[] args) {
		SpringApplication.run(RewardCentralApplication.class, args);
	}

	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}

}
