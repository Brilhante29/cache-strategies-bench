package com.portfolio.cachebench;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.portfolio.cachebench")
public class CacheBenchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheBenchApplication.class, args);
    }
}
