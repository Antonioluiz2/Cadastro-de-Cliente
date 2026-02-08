package com.testetecnico.cliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ClienteApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteApiApplication.class, args);
    }
}
