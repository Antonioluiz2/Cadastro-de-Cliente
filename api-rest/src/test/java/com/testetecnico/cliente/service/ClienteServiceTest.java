package com.testetecnico.cliente.service;

import com.testetecnico.cliente.domain.dto.ClienteEventDTO;
import com.testetecnico.cliente.domain.dto.ClienteRequestDTO;
import com.testetecnico.cliente.domain.dto.ClienteResponseDTO;
import com.testetecnico.cliente.domain.entity.Cliente;
import com.testetecnico.cliente.domain.entity.StatusCliente;
import com.testetecnico.cliente.exception.ClienteNotFoundException;
import com.testetecnico.cliente.exception.DuplicateClienteException;
import com.testetecnico.cliente.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ClienteService
 * @author Antonio Luiz
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService - Testes Unitários")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDTO clienteRequest;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteRequest = ClienteRequestDTO.builder()
                .nome("João Silva")
                .email("joao.silva@email.com")
                .cpf("12345678901")
                .telefone("11987654321")
                .endereco("Rua Exemplo, 123")
                .status(StatusCliente.ATIVO)
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao.silva@email.com")
                .cpf("12345678901")
                .telefone("11987654321")
                .endereco("Rua Exemplo, 123")
                .status(StatusCliente.ATIVO)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void deveCriarClienteComSucesso() {
        // Arrange
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        doNothing().when(kafkaProducerService).enviarEvento(any(ClienteEventDTO.class));

        // Act
        ClienteResponseDTO response = clienteService.criarCliente(clienteRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getEmail()).isEqualTo("joao.silva@email.com");
        assertThat(response.getStatus()).isEqualTo(StatusCliente.ATIVO);

        verify(clienteRepository).existsByEmail("joao.silva@email.com");
        verify(clienteRepository).existsByCpf("12345678901");
        verify(clienteRepository).save(any(Cliente.class));
        verify(kafkaProducerService).enviarEvento(any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve criar cliente com status padrão ATIVO quando não informado")
    void deveCriarClienteComStatusPadraoAtivo() {
        // Arrange
        clienteRequest.setStatus(null);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        ClienteResponseDTO response = clienteService.criarCliente(clienteRequest);

        // Assert
        assertThat(response.getStatus()).isEqualTo(StatusCliente.ATIVO);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com email duplicado")
    void deveLancarExcecaoAoCriarClienteComEmailDuplicado() {
        // Arrange
        when(clienteRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> clienteService.criarCliente(clienteRequest))
                .isInstanceOf(DuplicateClienteException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(clienteRepository).existsByEmail("joao.silva@email.com");
        verify(clienteRepository, never()).existsByCpf(anyString());
        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(kafkaProducerService, never()).enviarEvento(any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com CPF duplicado")
    void deveLancarExcecaoAoCriarClienteComCpfDuplicado() {
        // Arrange
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> clienteService.criarCliente(clienteRequest))
                .isInstanceOf(DuplicateClienteException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(clienteRepository).existsByEmail(anyString());
        verify(clienteRepository).existsByCpf("12345678901");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve enviar evento CLIENTE_CRIADO para Kafka ao criar cliente")
    void deveEnviarEventoClienteCriadoParaKafka() {
        // Arrange
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ArgumentCaptor<ClienteEventDTO> eventoCaptor = ArgumentCaptor.forClass(ClienteEventDTO.class);

        // Act
        clienteService.criarCliente(clienteRequest);

        // Assert
        verify(kafkaProducerService).enviarEvento(eventoCaptor.capture());
        ClienteEventDTO evento = eventoCaptor.getValue();

        assertThat(evento.getEventType()).isEqualTo("CLIENTE_CRIADO");
        assertThat(evento.getClienteId()).isEqualTo(1L);
        assertThat(evento.getNome()).isEqualTo("João Silva");
        assertThat(evento.getEmail()).isEqualTo("joao.silva@email.com");
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorIdComSucesso() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act
        ClienteResponseDTO response = clienteService.buscarPorId(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("João Silva");
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar cliente inexistente")
    void deveLancarExcecaoAoBuscarClienteInexistente() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> clienteService.buscarPorId(999L))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");

        verify(clienteRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosOsClientes() {
        // Arrange
        Cliente cliente2 = Cliente.builder()
                .id(2L)
                .nome("Maria Santos")
                .email("maria@email.com")
                .cpf("98765432100")
                .status(StatusCliente.ATIVO)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente, cliente2));

        // Act
        List<ClienteResponseDTO> response = clienteService.listarTodos();

        // Assert
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getNome()).isEqualTo("João Silva");
        assertThat(response.get(1).getNome()).isEqualTo("Maria Santos");
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar clientes por status")
    void deveListarClientesPorStatus() {
        // Arrange
        when(clienteRepository.findByStatus(StatusCliente.ATIVO))
                .thenReturn(Arrays.asList(cliente));

        // Act
        List<ClienteResponseDTO> response = clienteService.listarPorStatus(StatusCliente.ATIVO);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getStatus()).isEqualTo(StatusCliente.ATIVO);
        verify(clienteRepository).findByStatus(StatusCliente.ATIVO);
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void deveAtualizarClienteComSucesso() {
        // Arrange
        ClienteRequestDTO updateRequest = ClienteRequestDTO.builder()
                .nome("João Silva Atualizado")
                .email("joao.silva@email.com")
                .cpf("12345678901")
                .telefone("11999999999")
                .endereco("Rua Nova, 456")
                .status(StatusCliente.ATIVO)
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        ClienteResponseDTO response = clienteService.atualizarCliente(1L, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
        verify(kafkaProducerService).enviarEvento(any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente inexistente")
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> clienteService.atualizarCliente(999L, clienteRequest))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");

        verify(clienteRepository).findById(999L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar email para um já existente")
    void deveLancarExcecaoAoAtualizarEmailParaJaExistente() {
        // Arrange
        clienteRequest.setEmail("outro@email.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail("outro@email.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> clienteService.atualizarCliente(1L, clienteRequest))
                .isInstanceOf(DuplicateClienteException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByEmail("outro@email.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar CPF para um já existente")
    void deveLancarExcecaoAoAtualizarCpfParaJaExistente() {
        // Arrange
        clienteRequest.setCpf("99999999999");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCpf("99999999999")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> clienteService.atualizarCliente(1L, clienteRequest))
                .isInstanceOf(DuplicateClienteException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByCpf("99999999999");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve enviar evento CLIENTE_ATUALIZADO ao atualizar")
    void deveEnviarEventoClienteAtualizadoAoAtualizar() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ArgumentCaptor<ClienteEventDTO> eventoCaptor = ArgumentCaptor.forClass(ClienteEventDTO.class);

        // Act
        clienteService.atualizarCliente(1L, clienteRequest);

        // Assert
        verify(kafkaProducerService).enviarEvento(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getEventType()).isEqualTo("CLIENTE_ATUALIZADO");
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void deveDeletarClienteComSucesso() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(any(Cliente.class));

        // Act
        clienteService.deletarCliente(1L);

        // Assert
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).delete(cliente);
        verify(kafkaProducerService).enviarEvento(any(ClienteEventDTO.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cliente inexistente")
    void deveLancarExcecaoAoDeletarClienteInexistente() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> clienteService.deletarCliente(999L))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");

        verify(clienteRepository).findById(999L);
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve enviar evento CLIENTE_DELETADO ao deletar")
    void deveEnviarEventoClienteDeletadoAoDeletar() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        ArgumentCaptor<ClienteEventDTO> eventoCaptor = ArgumentCaptor.forClass(ClienteEventDTO.class);

        // Act
        clienteService.deletarCliente(1L);

        // Assert
        verify(kafkaProducerService).enviarEvento(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getEventType()).isEqualTo("CLIENTE_DELETADO");
    }
}
