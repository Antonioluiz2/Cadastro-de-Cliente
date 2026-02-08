package com.testetecnico.cliente.service;

import com.testetecnico.cliente.domain.dto.ClienteEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, ClienteEventDTO> kafkaTemplate;

    @Value("${app.kafka.topic.cliente-events}")
    private String clienteEventsTopic;

    public void enviarEvento(ClienteEventDTO evento) {
        try {
            log.info("Enviando evento para Kafka: {}", evento);
            
            CompletableFuture<SendResult<String, ClienteEventDTO>> future = 
                kafkaTemplate.send(clienteEventsTopic, evento.getClienteId().toString(), evento);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Evento enviado com sucesso: {} com offset: {}", 
                        evento.getEventType(), 
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Erro ao enviar evento: {}", evento.getEventType(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erro ao enviar evento para Kafka", e);
            throw new RuntimeException("Falha ao enviar evento para Kafka", e);
        }
    }
}
