package com.testetecnico.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ClienteWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteWorkerApplication.class, args);
    }
}
