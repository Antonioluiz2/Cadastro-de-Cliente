package com.testetecnico.worker.consumer;

import com.testetecnico.worker.dto.ClienteEventDTO;
import com.testetecnico.worker.dto.ProcessedEventDTO;
import com.testetecnico.worker.service.EventProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventConsumer {

    private final EventProcessorService eventProcessorService;

    @KafkaListener(
        topics = "${app.kafka.topic.cliente-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirEvento(
            @Payload ClienteEventDTO evento,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Mensagem recebida - Partition: {}, Offset: {}, EventType: {}, ClienteId: {}", 
                partition, offset, evento.getEventType(), evento.getClienteId());

            // Processa o evento
            ProcessedEventDTO processedEvent = eventProcessorService.processarEvento(evento);

            // Log do resultado
            log.info("Evento processado com sucesso: {}", processedEvent);

            // Confirma o processamento da mensagem
            acknowledgment.acknowledge();
            
            log.info("Mensagem confirmada (acknowledged) - Partition: {}, Offset: {}", partition, offset);

        } catch (Exception e) {
            log.error("Erro ao processar evento: {} - ClienteId: {}", 
                evento.getEventType(), evento.getClienteId(), e);
            
            // Aqui você pode implementar lógica de retry ou dead letter queue
            // Por enquanto, vamos confirmar mesmo com erro para não travar o processamento
            acknowledgment.acknowledge();
        }
    }
}
