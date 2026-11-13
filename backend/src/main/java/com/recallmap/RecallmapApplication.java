package com.recallmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RecallmapApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecallmapApplication.class, args);
    }
}
