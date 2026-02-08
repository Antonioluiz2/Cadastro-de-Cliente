package com.testetecnico.cliente.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.cliente-events}")
    private String clienteEventsTopic;

    @Bean
    public NewTopic clienteEventsTopic() {
        return TopicBuilder.name(clienteEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
