package com.testetecnico.cliente.service;

import com.testetecnico.cliente.domain.dto.ClienteEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para KafkaProducerService
 * @author Antonio Luiz
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaProducerService - Testes Unitários")
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, ClienteEventDTO> kafkaTemplate;

    @Mock
    private SendResult<String, ClienteEventDTO> sendResult;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private ClienteEventDTO clienteEvent;
    private final String topicName = "cliente-events";

    @BeforeEach
    void setUp() {
        // Injeta o valor do tópico usando ReflectionTestUtils
        ReflectionTestUtils.setField(kafkaProducerService, "clienteEventsTopic", topicName);

        clienteEvent = ClienteEventDTO.builder()
                .eventType("CLIENTE_CRIADO")
                .clienteId(1L)
                .nome("João Silva")
                .email("joao.silva@email.com")
                .cpf("12345678901")
                .telefone("11987654321")
                .endereco("Rua Exemplo, 123")
                .status("ATIVO")
                .timestamp("2026-02-08T10:30:00")
                .build();
    }

    @Test
    @DisplayName("Deve enviar evento para Kafka com sucesso")
    void deveEnviarEventoParaKafkaComSucesso() {
        // Arrange
        CompletableFuture<SendResult<String, ClienteEventDTO>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), any(ClienteEventDTO.class))).thenReturn(future);

        // Act
        kafkaProducerService.enviarEvento(clienteEvent);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ClienteEventDTO> eventoCaptor = ArgumentCaptor.forClass(ClienteEventDTO.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventoCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(topicName);
        assertThat(keyCaptor.getValue()).isEqualTo("1");
        assertThat(eventoCaptor.getValue()).isEqualTo(clienteEvent);
    }

    @Test
    @DisplayName("Deve usar clienteId como chave do evento")
    void deveUsarClienteIdComoChaveDoEvento() {
        // Arrange
        CompletableFuture<SendResult<String, ClienteEventDTO>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), any(ClienteEventDTO.class))).thenReturn(future);

        // Act
        kafkaProducerService.enviarEvento(clienteEvent);

        // Assert
        verify(kafkaTemplate).send(eq(topicName), eq("1"), any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando falha ao enviar evento")
    void deveLancarExcecaoQuandoFalhaAoEnviarEvento() {
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), any(ClienteEventDTO.class)))
                .thenThrow(new RuntimeException("Kafka não disponível"));

        // Act & Assert
        assertThatThrownBy(() -> kafkaProducerService.enviarEvento(clienteEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao enviar evento para Kafka");

        verify(kafkaTemplate).send(anyString(), anyString(), any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve enviar diferentes tipos de eventos")
    void deveEnviarDiferentesTiposDeEventos() {
        // Arrange
        CompletableFuture<SendResult<String, ClienteEventDTO>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), any(ClienteEventDTO.class))).thenReturn(future);

        ClienteEventDTO eventoCriado = clienteEvent.toBuilder().eventType("CLIENTE_CRIADO").build();
        ClienteEventDTO eventoAtualizado = clienteEvent.toBuilder().eventType("CLIENTE_ATUALIZADO").build();
        ClienteEventDTO eventoDeletado = clienteEvent.toBuilder().eventType("CLIENTE_DELETADO").build();

        // Act
        kafkaProducerService.enviarEvento(eventoCriado);
        kafkaProducerService.enviarEvento(eventoAtualizado);
        kafkaProducerService.enviarEvento(eventoDeletado);

        // Assert
        verify(kafkaTemplate, times(3)).send(anyString(), anyString(), any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve enviar eventos com diferentes clienteIds")
    void deveEnviarEventosComDiferentesClienteIds() {
        // Arrange
        CompletableFuture<SendResult<String, ClienteEventDTO>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), any(ClienteEventDTO.class))).thenReturn(future);

        ClienteEventDTO evento1 = clienteEvent.toBuilder().clienteId(1L).build();
        ClienteEventDTO evento2 = clienteEvent.toBuilder().clienteId(2L).build();
        ClienteEventDTO evento3 = clienteEvent.toBuilder().clienteId(3L).build();

        // Act
        kafkaProducerService.enviarEvento(evento1);
        kafkaProducerService.enviarEvento(evento2);
        kafkaProducerService.enviarEvento(evento3);

        // Assert
        verify(kafkaTemplate).send(eq(topicName), eq("1"), any(ClienteEventDTO.class));
        verify(kafkaTemplate).send(eq(topicName), eq("2"), any(ClienteEventDTO.class));
        verify(kafkaTemplate).send(eq(topicName), eq("3"), any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve enviar evento para tópico correto")
    void deveEnviarEventoParaTopicoCorreto() {
        // Arrange
        CompletableFuture<SendResult<String, ClienteEventDTO>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), any(ClienteEventDTO.class))).thenReturn(future);

        // Act
        kafkaProducerService.enviarEvento(clienteEvent);

        // Assert
        verify(kafkaTemplate).send(eq("cliente-events"), anyString(), any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve propagar exceção quando kafkaTemplate lança exceção")
    void devePropagExcecaoQuandoKafkaTemplateLancaExcecao() {
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), any(ClienteEventDTO.class)))
                .thenThrow(new IllegalArgumentException("Argumento inválido"));

        // Act & Assert
        assertThatThrownBy(() -> kafkaProducerService.enviarEvento(clienteEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao enviar evento para Kafka")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
