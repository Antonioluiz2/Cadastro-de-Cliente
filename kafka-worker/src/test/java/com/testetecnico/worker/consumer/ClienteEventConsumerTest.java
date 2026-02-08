package com.testetecnico.worker.consumer;

import com.testetecnico.worker.dto.ClienteEventDTO;
import com.testetecnico.worker.dto.ProcessedEventDTO;
import com.testetecnico.worker.service.EventProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ClienteEventConsumer
 * @author Antonio Luiz
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteEventConsumer - Testes Unitários")
class ClienteEventConsumerTest {

    @Mock
    private EventProcessorService eventProcessorService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private ClienteEventConsumer clienteEventConsumer;

    private ClienteEventDTO clienteEvent;
    private ProcessedEventDTO processedEvent;

    @BeforeEach
    void setUp() {
        clienteEvent = ClienteEventDTO.builder()
                .eventType("CLIENTE_CRIADO")
                .clienteId(1L)
                .nome("João Silva")
                .email("joao.silva@email.com")
                .cpf("12345678901")
                .telefone("11987654321")
                .endereco("Rua Exemplo, 123")
                .status("ATIVO")
                .timestamp(LocalDateTime.now().toString())
                .build();

        processedEvent = ProcessedEventDTO.builder()
                .eventType("CLIENTE_CRIADO")
                .clienteId(1L)
                .nomeProcessado("JOÃO SILVA")
                .emailProcessado("joao.silva@email.com")
                .cpfProcessado("12345678901")
                .telefoneProcessado("11987654321")
                .enderecoProcessado("Rua Exemplo, 123")
                .status("ATIVO")
                .timestampOriginal(LocalDateTime.now().toString())
                .timestampProcessamento(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve consumir e processar evento com sucesso")
    void deveConsumirEProcessarEventoComSucesso() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);
        doNothing().when(acknowledgment).acknowledge();

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(clienteEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento CLIENTE_CRIADO")
    void deveProcessarEventoClienteCriado() throws InterruptedException {
        // Arrange
        clienteEvent.setEventType("CLIENTE_CRIADO");
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(clienteEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento CLIENTE_ATUALIZADO")
    void deveProcessarEventoClienteAtualizado() throws InterruptedException {
        // Arrange
        clienteEvent.setEventType("CLIENTE_ATUALIZADO");
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(clienteEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento CLIENTE_DELETADO")
    void deveProcessarEventoClienteDeletado() throws InterruptedException {
        // Arrange
        clienteEvent.setEventType("CLIENTE_DELETADO");
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(clienteEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar eventos de diferentes partições")
    void deveProcessarEventosDeDiferentesParticoes() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);
        clienteEventConsumer.consumirEvento(clienteEvent, 1, 101L, acknowledgment);
        clienteEventConsumer.consumirEvento(clienteEvent, 2, 102L, acknowledgment);

        // Assert
        verify(eventProcessorService, times(3)).processarEvento(clienteEvent);
        verify(acknowledgment, times(3)).acknowledge();
    }

    @Test
    @DisplayName("Deve processar eventos com diferentes offsets")
    void deveProcessarEventosComDiferentesOffsets() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 101L, acknowledgment);
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 102L, acknowledgment);

        // Assert
        verify(eventProcessorService, times(3)).processarEvento(clienteEvent);
        verify(acknowledgment, times(3)).acknowledge();
    }

    @Test
    @DisplayName("Deve confirmar (acknowledge) mensagem após processamento bem-sucedido")
    void deveConfirmarMensagemAposProcessamentoBemSucedido() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve confirmar mensagem mesmo quando ocorre erro no processamento")
    void deveConfirmarMensagemMesmoQuandoOcorreErro() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenThrow(new RuntimeException("Erro no processamento"));

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(clienteEvent);
        verify(acknowledgment).acknowledge(); // Deve confirmar mesmo com erro
    }

    @Test
    @DisplayName("Deve continuar processando após erro em um evento")
    void deveContinuarProcessandoAposErroEmUmEvento() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenThrow(new RuntimeException("Erro"))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 101L, acknowledgment);

        // Assert
        verify(eventProcessorService, times(2)).processarEvento(clienteEvent);
        verify(acknowledgment, times(2)).acknowledge();
    }

    @Test
    @DisplayName("Deve processar eventos com diferentes clienteIds")
    void deveProcessarEventosComDiferentesClienteIds() throws InterruptedException {
        // Arrange
        ClienteEventDTO evento1 = clienteEvent.toBuilder().clienteId(1L).build();
        ClienteEventDTO evento2 = clienteEvent.toBuilder().clienteId(2L).build();
        ClienteEventDTO evento3 = clienteEvent.toBuilder().clienteId(3L).build();

        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(evento1, 0, 100L, acknowledgment);
        clienteEventConsumer.consumirEvento(evento2, 0, 101L, acknowledgment);
        clienteEventConsumer.consumirEvento(evento3, 0, 102L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(evento1);
        verify(eventProcessorService).processarEvento(evento2);
        verify(eventProcessorService).processarEvento(evento3);
        verify(acknowledgment, times(3)).acknowledge();
    }

    @Test
    @DisplayName("Deve lidar com InterruptedException durante processamento")
    void deveLidarComInterruptedExceptionDuranteProcessamento() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenThrow(new InterruptedException("Thread interrompida"));

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(clienteEvent);
        verify(acknowledgment).acknowledge(); // Deve confirmar mesmo com erro
    }

    @Test
    @DisplayName("Deve processar evento com todos os campos preenchidos")
    void deveProcessarEventoComTodosCamposPreenchidos() throws InterruptedException {
        // Arrange
        ClienteEventDTO eventoCompleto = ClienteEventDTO.builder()
                .eventType("CLIENTE_CRIADO")
                .clienteId(100L)
                .nome("Maria Santos Silva")
                .email("maria.santos@empresa.com.br")
                .cpf("98765432100")
                .telefone("21987654321")
                .endereco("Avenida Brasil, 1000, Centro, Rio de Janeiro")
                .status("ATIVO")
                .timestamp(LocalDateTime.now().toString())
                .build();

        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(eventoCompleto, 0, 200L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(eventoCompleto);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento com campos opcionais nulos")
    void deveProcessarEventoComCamposOpcionaisNulos() throws InterruptedException {
        // Arrange
        clienteEvent.setTelefone(null);
        clienteEvent.setEndereco(null);

        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        // Act
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(clienteEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Não deve propagar exceção para o Kafka quando ocorre erro")
    void naoDevePropagExcecaoParaOKafkaQuandoOcorreErro() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenThrow(new RuntimeException("Erro grave no processamento"));

        // Act - não deve lançar exceção
        clienteEventConsumer.consumirEvento(clienteEvent, 0, 100L, acknowledgment);

        // Assert
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar múltiplos eventos em sequência")
    void deveProcessarMultiplosEventosEmSequencia() throws InterruptedException {
        // Arrange
        when(eventProcessorService.processarEvento(any(ClienteEventDTO.class)))
                .thenReturn(processedEvent);

        ClienteEventDTO evento1 = clienteEvent.toBuilder().eventType("CLIENTE_CRIADO").clienteId(1L).build();
        ClienteEventDTO evento2 = clienteEvent.toBuilder().eventType("CLIENTE_ATUALIZADO").clienteId(1L).build();
        ClienteEventDTO evento3 = clienteEvent.toBuilder().eventType("CLIENTE_DELETADO").clienteId(1L).build();

        // Act
        clienteEventConsumer.consumirEvento(evento1, 0, 100L, acknowledgment);
        clienteEventConsumer.consumirEvento(evento2, 0, 101L, acknowledgment);
        clienteEventConsumer.consumirEvento(evento3, 0, 102L, acknowledgment);

        // Assert
        verify(eventProcessorService).processarEvento(evento1);
        verify(eventProcessorService).processarEvento(evento2);
        verify(eventProcessorService).processarEvento(evento3);
        verify(acknowledgment, times(3)).acknowledge();
    }
}
