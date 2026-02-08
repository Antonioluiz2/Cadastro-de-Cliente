package com.testetecnico.cliente.service;

import com.testetecnico.cliente.domain.dto.ClienteEventDTO;
import com.testetecnico.cliente.domain.dto.ClienteRequestDTO;
import com.testetecnico.cliente.domain.dto.ClienteResponseDTO;
import com.testetecnico.cliente.domain.entity.Cliente;
import com.testetecnico.cliente.domain.entity.StatusCliente;
import com.testetecnico.cliente.exception.ClienteNotFoundException;
import com.testetecnico.cliente.exception.DuplicateClienteException;
import com.testetecnico.cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public ClienteResponseDTO criarCliente(ClienteRequestDTO request) {
        log.info("Criando novo cliente: {}", request.getEmail());

        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateClienteException("Email já cadastrado: " + request.getEmail());
        }

        if (clienteRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateClienteException("CPF já cadastrado: " + request.getCpf());
        }

        Cliente cliente = Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .cpf(request.getCpf())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .status(request.getStatus() != null ? request.getStatus() : StatusCliente.ATIVO)
                .build();

        cliente = clienteRepository.save(cliente);
        log.info("Cliente criado com ID: {}", cliente.getId());

        // Envia evento para Kafka
        ClienteEventDTO evento = criarEvento("CLIENTE_CRIADO", cliente);
        kafkaProducerService.enviarEvento(evento);

        return toResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        log.info("Buscando cliente por ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado: " + id));
        return toResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        log.info("Listando todos os clientes");
        return clienteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarPorStatus(StatusCliente status) {
        log.info("Listando clientes por status: {}", status);
        return clienteRepository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponseDTO atualizarCliente(Long id, ClienteRequestDTO request) {
        log.info("Atualizando cliente ID: {}", id);
        
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado: " + id));

        // Verifica duplicação de email
        if (!cliente.getEmail().equals(request.getEmail()) && 
            clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateClienteException("Email já cadastrado: " + request.getEmail());
        }

        // Verifica duplicação de CPF
        if (!cliente.getCpf().equals(request.getCpf()) && 
            clienteRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateClienteException("CPF já cadastrado: " + request.getCpf());
        }

        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setCpf(request.getCpf());
        cliente.setTelefone(request.getTelefone());
        cliente.setEndereco(request.getEndereco());
        if (request.getStatus() != null) {
            cliente.setStatus(request.getStatus());
        }

        cliente = clienteRepository.save(cliente);
        log.info("Cliente atualizado: {}", id);

        // Envia evento para Kafka
        ClienteEventDTO evento = criarEvento("CLIENTE_ATUALIZADO", cliente);
        kafkaProducerService.enviarEvento(evento);

        return toResponseDTO(cliente);
    }

    @Transactional
    public void deletarCliente(Long id) {
        log.info("Deletando cliente ID: {}", id);
        
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado: " + id));

        clienteRepository.delete(cliente);
        log.info("Cliente deletado: {}", id);

        // Envia evento para Kafka
        ClienteEventDTO evento = criarEvento("CLIENTE_DELETADO", cliente);
        kafkaProducerService.enviarEvento(evento);
    }

    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .cpf(cliente.getCpf())
                .telefone(cliente.getTelefone())
                .endereco(cliente.getEndereco())
                .status(cliente.getStatus())
                .dataCriacao(cliente.getDataCriacao())
                .dataAtualizacao(cliente.getDataAtualizacao())
                .build();
    }

    private ClienteEventDTO criarEvento(String eventType, Cliente cliente) {
        return ClienteEventDTO.builder()
                .eventType(eventType)
                .clienteId(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .cpf(cliente.getCpf())
                .telefone(cliente.getTelefone())
                .endereco(cliente.getEndereco())
                .status(cliente.getStatus().name())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
