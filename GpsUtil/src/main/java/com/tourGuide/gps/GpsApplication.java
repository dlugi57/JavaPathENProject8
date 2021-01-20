package com.tourGuide.gps;

import gpsUtil.GpsUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Locale;

@SpringBootApplication
public class GpsApplication {
	public static void main(String[] args) {
		Locale.setDefault(Locale.US);

		SpringApplication.run(GpsApplication.class, args);
	}

	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}

}
