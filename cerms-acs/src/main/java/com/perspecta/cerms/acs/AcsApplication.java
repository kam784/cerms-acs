package com.perspecta.cerms.acs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@PropertySource(value = "classpath:application.yml")
@SpringBootApplication
public class AcsApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder
				.sources(AcsApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(AcsApplication.class, args);

	}
}
