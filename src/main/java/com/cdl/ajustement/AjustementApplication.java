package com.cdl.ajustement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AjustementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AjustementApplication.class, args);
    }
}
