package com.testetecnico.cliente.controller;

import com.testetecnico.cliente.domain.dto.ClienteRequestDTO;
import com.testetecnico.cliente.domain.dto.ClienteResponseDTO;
import com.testetecnico.cliente.domain.entity.StatusCliente;
import com.testetecnico.cliente.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "API para gerenciamento de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Criar novo cliente", description = "Cria um novo cliente e envia evento para Kafka")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Cliente já existe (email ou CPF duplicado)")
    })
    public ResponseEntity<ClienteResponseDTO> criarCliente(
            @Valid @RequestBody ClienteRequestDTO request) {
        log.info("Recebida requisição POST para criar cliente: {}", request.getEmail());
        ClienteResponseDTO response = clienteService.criarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados de um cliente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> buscarClientePorId(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {
        log.info("Recebida requisição GET para buscar cliente ID: {}", id);
        ClienteResponseDTO response = clienteService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos os clientes ou filtra por status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso")
    })
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes(
            @Parameter(description = "Status do cliente (ATIVO, INATIVO, BLOQUEADO)")
            @RequestParam(required = false) StatusCliente status) {
        log.info("Recebida requisição GET para listar clientes. Status: {}", status);
        
        List<ClienteResponseDTO> response;
        if (status != null) {
            response = clienteService.listarPorStatus(status);
        } else {
            response = clienteService.listarTodos();
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente e envia evento para Kafka")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "409", description = "Email ou CPF já cadastrado para outro cliente")
    })
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO request) {
        log.info("Recebida requisição PUT para atualizar cliente ID: {}", id);
        ClienteResponseDTO response = clienteService.atualizarCliente(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cliente", description = "Remove um cliente e envia evento para Kafka")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Void> deletarCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {
        log.info("Recebida requisição DELETE para cliente ID: {}", id);
        clienteService.deletarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
