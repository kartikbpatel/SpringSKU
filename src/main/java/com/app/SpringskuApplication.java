package com.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.app.repository.SkuRepository;

@SpringBootApplication
public class SpringskuApplication implements CommandLineRunner {
	
	@Autowired
	SkuRepository skuRepository;
	
    public static void main(String[] args) {
		SpringApplication.run(SpringskuApplication.class, args);
	}

    @Override
    public void run(String... strings) throws Exception {
    }


}
