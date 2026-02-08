package com.testetecnico.worker.service;

import com.testetecnico.worker.dto.ClienteEventDTO;
import com.testetecnico.worker.dto.ProcessedEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para EventProcessorService
 * @author Antonio Luiz
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventProcessorService - Testes Unitários")
class EventProcessorServiceTest {

    private EventProcessorService eventProcessorService;
    private ClienteEventDTO clienteEvent;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        eventProcessorService = new EventProcessorService();
        
        // Configurar propriedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(eventProcessorService, "processingDelay", 0L);
        ReflectionTestUtils.setField(eventProcessorService, "enableTransformation", true);
        ReflectionTestUtils.setField(eventProcessorService, "saveToFile", true);
        ReflectionTestUtils.setField(eventProcessorService, "outputDirectory", tempDir.toString());

        clienteEvent = ClienteEventDTO.builder()
                .eventType("CLIENTE_CRIADO")
                .clienteId(1L)
                .nome("joão silva")
                .email("JOAO.SILVA@EMAIL.COM")
                .cpf("123.456.789-01")
                .telefone("(11) 98765-4321")
                .endereco("rua exemplo, 123")
                .status("ATIVO")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @Test
    @DisplayName("Deve processar evento com transformação de dados")
    void deveProcessarEventoComTransformacao() throws InterruptedException {
        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getClienteId()).isEqualTo(1L);
        assertThat(result.getEventType()).isEqualTo("CLIENTE_CRIADO");
        assertThat(result.getNomeProcessado()).isEqualTo("JOÃO SILVA"); // Maiúsculo
        assertThat(result.getEmailProcessado()).isEqualTo("joao.silva@email.com"); // Minúsculo
        assertThat(result.getCpfProcessado()).isEqualTo("12345678901"); // Sem formatação
        assertThat(result.getTelefoneProcessado()).isEqualTo("11987654321"); // Sem formatação
        assertThat(result.getEnderecoProcessado()).isEqualTo("Rua Exemplo, 123"); // Title Case
        assertThat(result.getTimestampProcessamento()).isNotNull();
    }

    @Test
    @DisplayName("Deve transformar nome para maiúsculo")
    void deveTransformarNomeParaMaiusculo() throws InterruptedException {
        // Arrange
        clienteEvent.setNome("maria santos");

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getNomeProcessado()).isEqualTo("MARIA SANTOS");
    }

    @Test
    @DisplayName("Deve transformar email para minúsculo")
    void deveTransformarEmailParaMinusculo() throws InterruptedException {
        // Arrange
        clienteEvent.setEmail("CONTATO@EXEMPLO.COM.BR");

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getEmailProcessado()).isEqualTo("contato@exemplo.com.br");
    }

    @Test
    @DisplayName("Deve remover formatação do CPF")
    void deveRemoverFormatacaoDoCpf() throws InterruptedException {
        // Arrange
        clienteEvent.setCpf("123.456.789-01");

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getCpfProcessado()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("Deve remover formatação do telefone")
    void deveRemoverFormatacaoDoTelefone() throws InterruptedException {
        // Arrange
        clienteEvent.setTelefone("(11) 98765-4321");

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getTelefoneProcessado()).isEqualTo("11987654321");
    }

    @Test
    @DisplayName("Deve transformar endereço para Title Case")
    void deveTransformarEnderecoParaTitleCase() throws InterruptedException {
        // Arrange
        clienteEvent.setEndereco("avenida paulista, 1000");

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getEnderecoProcessado()).isEqualTo("Avenida Paulista, 1000");
    }

    @Test
    @DisplayName("Deve processar evento sem transformação quando desabilitado")
    void deveProcessarEventoSemTransformacao() throws InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(eventProcessorService, "enableTransformation", false);

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getNomeProcessado()).isEqualTo("joão silva");
        assertThat(result.getEmailProcessado()).isEqualTo("JOAO.SILVA@EMAIL.COM");
        assertThat(result.getCpfProcessado()).isEqualTo("123.456.789-01");
        assertThat(result.getObservacoes()).contains("sem transformação");
    }

    @Test
    @DisplayName("Deve armazenar evento em memória")
    void deveArmazenarEventoEmMemoria() throws InterruptedException {
        // Act
        eventProcessorService.processarEvento(clienteEvent);

        // Assert
        ProcessedEventDTO stored = eventProcessorService.buscarPorId(1L);
        assertThat(stored).isNotNull();
        assertThat(stored.getClienteId()).isEqualTo(1L);
        assertThat(eventProcessorService.getTotalProcessados()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve sobrescrever evento com mesmo clienteId em memória")
    void deveSobrescreverEventoComMesmoClienteId() throws InterruptedException {
        // Arrange
        ClienteEventDTO primeiroEvento = clienteEvent.toBuilder()
                .eventType("CLIENTE_CRIADO")
                .nome("primeiro nome")
                .build();

        ClienteEventDTO segundoEvento = clienteEvent.toBuilder()
                .eventType("CLIENTE_ATUALIZADO")
                .nome("segundo nome")
                .build();

        // Act
        eventProcessorService.processarEvento(primeiroEvento);
        eventProcessorService.processarEvento(segundoEvento);

        // Assert
        ProcessedEventDTO stored = eventProcessorService.buscarPorId(1L);
        assertThat(stored.getEventType()).isEqualTo("CLIENTE_ATUALIZADO");
        assertThat(stored.getNomeProcessado()).isEqualTo("SEGUNDO NOME");
        assertThat(eventProcessorService.getTotalProcessados()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve armazenar múltiplos eventos com clienteIds diferentes")
    void deveArmazenarMultiplosEventos() throws InterruptedException {
        // Arrange
        ClienteEventDTO evento1 = clienteEvent.toBuilder().clienteId(1L).build();
        ClienteEventDTO evento2 = clienteEvent.toBuilder().clienteId(2L).build();
        ClienteEventDTO evento3 = clienteEvent.toBuilder().clienteId(3L).build();

        // Act
        eventProcessorService.processarEvento(evento1);
        eventProcessorService.processarEvento(evento2);
        eventProcessorService.processarEvento(evento3);

        // Assert
        assertThat(eventProcessorService.getTotalProcessados()).isEqualTo(3);
        assertThat(eventProcessorService.buscarPorId(1L)).isNotNull();
        assertThat(eventProcessorService.buscarPorId(2L)).isNotNull();
        assertThat(eventProcessorService.buscarPorId(3L)).isNotNull();
    }

    @Test
    @DisplayName("Deve listar todos os eventos processados")
    void deveListarTodosOsEventosProcessados() throws InterruptedException {
        // Arrange
        ClienteEventDTO evento1 = clienteEvent.toBuilder().clienteId(1L).build();
        ClienteEventDTO evento2 = clienteEvent.toBuilder().clienteId(2L).build();

        // Act
        eventProcessorService.processarEvento(evento1);
        eventProcessorService.processarEvento(evento2);
        Map<Long, ProcessedEventDTO> todos = eventProcessorService.listarTodos();

        // Assert
        assertThat(todos).hasSize(2);
        assertThat(todos).containsKeys(1L, 2L);
    }

    @Test
    @DisplayName("Deve salvar evento em arquivo quando habilitado")
    void deveSalvarEventoEmArquivo() throws InterruptedException, IOException {
        // Act
        eventProcessorService.processarEvento(clienteEvent);

        // Assert
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotEmpty();
        assertThat(files[0].getName()).contains("cliente_1_CLIENTE_CRIADO");
        assertThat(files[0].getName()).endsWith(".json");

        // Verifica conteúdo do arquivo
        String content = Files.readString(files[0].toPath());
        assertThat(content).contains("\"clienteId\" : 1");
        assertThat(content).contains("\"eventType\" : \"CLIENTE_CRIADO\"");
    }

    @Test
    @DisplayName("Não deve salvar evento em arquivo quando desabilitado")
    void naoDeveSalvarEventoEmArquivoQuandoDesabilitado() throws InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(eventProcessorService, "saveToFile", false);

        // Act
        eventProcessorService.processarEvento(clienteEvent);

        // Assert
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isEmpty();
    }

    @Test
    @DisplayName("Deve processar diferentes tipos de eventos")
    void deveProcessarDiferentesTiposDeEventos() throws InterruptedException {
        // Arrange
        ClienteEventDTO eventoCriado = clienteEvent.toBuilder()
                .eventType("CLIENTE_CRIADO")
                .clienteId(1L)
                .build();
        
        ClienteEventDTO eventoAtualizado = clienteEvent.toBuilder()
                .eventType("CLIENTE_ATUALIZADO")
                .clienteId(2L)
                .build();
        
        ClienteEventDTO eventoDeletado = clienteEvent.toBuilder()
                .eventType("CLIENTE_DELETADO")
                .clienteId(3L)
                .build();

        // Act
        ProcessedEventDTO processado1 = eventProcessorService.processarEvento(eventoCriado);
        ProcessedEventDTO processado2 = eventProcessorService.processarEvento(eventoAtualizado);
        ProcessedEventDTO processado3 = eventProcessorService.processarEvento(eventoDeletado);

        // Assert
        assertThat(processado1.getEventType()).isEqualTo("CLIENTE_CRIADO");
        assertThat(processado2.getEventType()).isEqualTo("CLIENTE_ATUALIZADO");
        assertThat(processado3.getEventType()).isEqualTo("CLIENTE_DELETADO");
    }

    @Test
    @DisplayName("Deve tratar valores nulos nos campos")
    void deveTratarValoresNulosNosCampos() throws InterruptedException {
        // Arrange
        clienteEvent.setNome(null);
        clienteEvent.setEmail(null);
        clienteEvent.setCpf(null);
        clienteEvent.setTelefone(null);
        clienteEvent.setEndereco(null);

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getNomeProcessado()).isNull();
        assertThat(result.getEmailProcessado()).isNull();
        assertThat(result.getCpfProcessado()).isNull();
        assertThat(result.getTelefoneProcessado()).isNull();
        assertThat(result.getEnderecoProcessado()).isNull();
    }

    @Test
    @DisplayName("Deve retornar null ao buscar evento não processado")
    void deveRetornarNullAoBuscarEventoNaoProcessado() {
        // Act
        ProcessedEventDTO result = eventProcessorService.buscarPorId(999L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve criar timestamp de processamento diferente do original")
    void deveCriarTimestampDeProcessamentoDiferenteDoOriginal() throws InterruptedException {
        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getTimestampOriginal()).isNotNull();
        assertThat(result.getTimestampProcessamento()).isNotNull();
        assertThat(result.getTimestampOriginal()).isNotEqualTo(result.getTimestampProcessamento().toString());
    }

    @Test
    @DisplayName("Deve processar endereço vazio como Title Case")
    void deveProcessarEnderecoVazioComoTitleCase() throws InterruptedException {
        // Arrange
        clienteEvent.setEndereco("");

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getEnderecoProcessado()).isEmpty();
    }

    @Test
    @DisplayName("Deve manter status do evento original")
    void deveManterStatusDoEventoOriginal() throws InterruptedException {
        // Arrange
        clienteEvent.setStatus("BLOQUEADO");

        // Act
        ProcessedEventDTO result = eventProcessorService.processarEvento(clienteEvent);

        // Assert
        assertThat(result.getStatus()).isEqualTo("BLOQUEADO");
    }
}
