package com.testetecnico.cliente.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testetecnico.cliente.domain.dto.ClienteRequestDTO;
import com.testetecnico.cliente.domain.dto.ClienteResponseDTO;
import com.testetecnico.cliente.domain.entity.StatusCliente;
import com.testetecnico.cliente.exception.ClienteNotFoundException;
import com.testetecnico.cliente.exception.DuplicateClienteException;
import com.testetecnico.cliente.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para ClienteController
 * @author Antonio Luiz
 */
@WebMvcTest(ClienteController.class)
@DisplayName("ClienteController - Testes de Integração")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    private ClienteRequestDTO clienteRequest;
    private ClienteResponseDTO clienteResponse;

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

        clienteResponse = ClienteResponseDTO.builder()
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
    @DisplayName("POST /api/v1/clientes - Deve criar cliente com sucesso")
    void deveCriarClienteComSucesso() throws Exception {
        // Arrange
        when(clienteService.criarCliente(any(ClienteRequestDTO.class))).thenReturn(clienteResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("João Silva")))
                .andExpect(jsonPath("$.email", is("joao.silva@email.com")))
                .andExpect(jsonPath("$.cpf", is("12345678901")))
                .andExpect(jsonPath("$.status", is("ATIVO")));

        verify(clienteService).criarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/clientes - Deve retornar 409 quando email duplicado")
    void deveRetornar409QuandoEmailDuplicado() throws Exception {
        // Arrange
        when(clienteService.criarCliente(any(ClienteRequestDTO.class)))
                .thenThrow(new DuplicateClienteException("Email já cadastrado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isConflict());

        verify(clienteService).criarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/clientes - Deve retornar 400 quando dados inválidos")
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        // Arrange
        clienteRequest.setNome(""); // Nome vazio é inválido
        clienteRequest.setEmail("email-invalido"); // Email inválido

        // Act & Assert
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).criarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} - Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorIdComSucesso() throws Exception {
        // Arrange
        when(clienteService.buscarPorId(1L)).thenReturn(clienteResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("João Silva")))
                .andExpect(jsonPath("$.email", is("joao.silva@email.com")));

        verify(clienteService).buscarPorId(1L);
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} - Deve retornar 404 quando cliente não existe")
    void deveRetornar404QuandoClienteNaoExiste() throws Exception {
        // Arrange
        when(clienteService.buscarPorId(999L))
                .thenThrow(new ClienteNotFoundException("Cliente não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/clientes/999"))
                .andExpect(status().isNotFound());

        verify(clienteService).buscarPorId(999L);
    }

    @Test
    @DisplayName("GET /api/v1/clientes - Deve listar todos os clientes")
    void deveListarTodosOsClientes() throws Exception {
        // Arrange
        ClienteResponseDTO cliente2 = ClienteResponseDTO.builder()
                .id(2L)
                .nome("Maria Santos")
                .email("maria@email.com")
                .cpf("98765432100")
                .status(StatusCliente.ATIVO)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        List<ClienteResponseDTO> clientes = Arrays.asList(clienteResponse, cliente2);
        when(clienteService.listarTodos()).thenReturn(clientes);

        // Act & Assert
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", is("João Silva")))
                .andExpect(jsonPath("$[1].nome", is("Maria Santos")));

        verify(clienteService).listarTodos();
    }

    @Test
    @DisplayName("GET /api/v1/clientes?status=ATIVO - Deve listar clientes por status")
    void deveListarClientesPorStatus() throws Exception {
        // Arrange
        when(clienteService.listarPorStatus(StatusCliente.ATIVO))
                .thenReturn(Collections.singletonList(clienteResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/clientes")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ATIVO")));

        verify(clienteService).listarPorStatus(StatusCliente.ATIVO);
        verify(clienteService, never()).listarTodos();
    }

    @Test
    @DisplayName("GET /api/v1/clientes - Deve retornar lista vazia quando não há clientes")
    void deveRetornarListaVaziaQuandoNaoHaClientes() throws Exception {
        // Arrange
        when(clienteService.listarTodos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(clienteService).listarTodos();
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} - Deve atualizar cliente com sucesso")
    void deveAtualizarClienteComSucesso() throws Exception {
        // Arrange
        ClienteRequestDTO updateRequest = ClienteRequestDTO.builder()
                .nome("João Silva Atualizado")
                .email("joao.silva@email.com")
                .cpf("12345678901")
                .telefone("11999999999")
                .endereco("Rua Nova, 456")
                .status(StatusCliente.ATIVO)
                .build();

        ClienteResponseDTO updatedResponse = clienteResponse.toBuilder()
                .nome("João Silva Atualizado")
                .telefone("11999999999")
                .endereco("Rua Nova, 456")
                .build();

        when(clienteService.atualizarCliente(eq(1L), any(ClienteRequestDTO.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("João Silva Atualizado")))
                .andExpect(jsonPath("$.telefone", is("11999999999")))
                .andExpect(jsonPath("$.endereco", is("Rua Nova, 456")));

        verify(clienteService).atualizarCliente(eq(1L), any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} - Deve retornar 404 ao atualizar cliente inexistente")
    void deveRetornar404AoAtualizarClienteInexistente() throws Exception {
        // Arrange
        when(clienteService.atualizarCliente(eq(999L), any(ClienteRequestDTO.class)))
                .thenThrow(new ClienteNotFoundException("Cliente não encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/clientes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isNotFound());

        verify(clienteService).atualizarCliente(eq(999L), any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} - Deve retornar 409 ao atualizar com email duplicado")
    void deveRetornar409AoAtualizarComEmailDuplicado() throws Exception {
        // Arrange
        when(clienteService.atualizarCliente(eq(1L), any(ClienteRequestDTO.class)))
                .thenThrow(new DuplicateClienteException("Email já cadastrado"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isConflict());

        verify(clienteService).atualizarCliente(eq(1L), any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} - Deve deletar cliente com sucesso")
    void deveDeletarClienteComSucesso() throws Exception {
        // Arrange
        doNothing().when(clienteService).deletarCliente(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService).deletarCliente(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} - Deve retornar 404 ao deletar cliente inexistente")
    void deveRetornar404AoDeletarClienteInexistente() throws Exception {
        // Arrange
        doThrow(new ClienteNotFoundException("Cliente não encontrado"))
                .when(clienteService).deletarCliente(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/clientes/999"))
                .andExpect(status().isNotFound());

        verify(clienteService).deletarCliente(999L);
    }

    @Test
    @DisplayName("POST /api/v1/clientes - Deve validar nome obrigatório")
    void deveValidarNomeObrigatorio() throws Exception {
        // Arrange
        clienteRequest.setNome(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).criarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/clientes - Deve validar email obrigatório")
    void deveValidarEmailObrigatorio() throws Exception {
        // Arrange
        clienteRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).criarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/clientes - Deve validar formato de email")
    void deveValidarFormatoDeEmail() throws Exception {
        // Arrange
        clienteRequest.setEmail("email-invalido");

        // Act & Assert
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).criarCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/clientes - Deve validar CPF obrigatório")
    void deveValidarCpfObrigatorio() throws Exception {
        // Arrange
        clienteRequest.setCpf(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).criarCliente(any(ClienteRequestDTO.class));
    }
}
