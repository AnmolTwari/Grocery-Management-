package com.shopmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShopManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopManagerApplication.class, args);
	}

	@Bean(destroyMethod = "shutdown")
	public ExecutorService queryExecutor() {
		return Executors.newFixedThreadPool(6);
	}

}
