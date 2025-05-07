package com.bytesfield.schedula;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.bytesfield.schedula.repositories")
@EntityScan(basePackages = "com.bytesfield.schedula.models.entities")
@EnableScheduling
public class SchedulaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulaApplication.class, args);
    }

}
