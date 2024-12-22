package com.example.logWatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.example.logWatcher")
public class LogWatcherApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogWatcherApplication.class, args);
	}

}
