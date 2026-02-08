package com.testetecnico.worker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.testetecnico.worker.dto.ClienteEventDTO;
import com.testetecnico.worker.dto.ProcessedEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EventProcessorService {

    @Value("${app.worker.processing.delay-ms:1000}")
    private long processingDelay;

    @Value("${app.worker.processing.enable-transformation:true}")
    private boolean enableTransformation;

    @Value("${app.worker.processing.save-to-file:true}")
    private boolean saveToFile;

    @Value("${app.worker.processing.output-directory:./data/processed}")
    private String outputDirectory;

    private final Map<Long, ProcessedEventDTO> memoryStorage = new HashMap<>();
    private final ObjectMapper objectMapper;

    public EventProcessorService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public ProcessedEventDTO processarEvento(ClienteEventDTO evento) throws InterruptedException {
        log.info("Iniciando processamento do evento: {} para cliente ID: {}", 
            evento.getEventType(), evento.getClienteId());

        // Simula processamento com delay
        Thread.sleep(processingDelay);

        // Realiza transformação dos dados
        ProcessedEventDTO processedEvent = transformarEvento(evento);

        // Armazena em memória
        armazenarEmMemoria(processedEvent);

        // Salva em arquivo se habilitado
        if (saveToFile) {
            salvarEmArquivo(processedEvent);
        }

        log.info("Evento processado com sucesso: {} para cliente ID: {}", 
            evento.getEventType(), evento.getClienteId());

        return processedEvent;
    }

    private ProcessedEventDTO transformarEvento(ClienteEventDTO evento) {
        ProcessedEventDTO processed = ProcessedEventDTO.builder()
                .eventType(evento.getEventType())
                .clienteId(evento.getClienteId())
                .timestampOriginal(evento.getTimestamp())
                .timestampProcessamento(LocalDateTime.now())
                .status(evento.getStatus())
                .build();

        if (enableTransformation) {
            // Transforma nome para maiúsculo
            processed.setNomeProcessado(evento.getNome() != null ? 
                evento.getNome().toUpperCase() : null);
            
            // Transforma email para minúsculo
            processed.setEmailProcessado(evento.getEmail() != null ? 
                evento.getEmail().toLowerCase() : null);
            
            // Formata CPF (remove caracteres especiais)
            processed.setCpfProcessado(evento.getCpf() != null ? 
                evento.getCpf().replaceAll("[^0-9]", "") : null);
            
            // Formata telefone (remove caracteres especiais)
            processed.setTelefoneProcessado(evento.getTelefone() != null ? 
                evento.getTelefone().replaceAll("[^0-9]", "") : null);
            
            // Transforma endereço para título
            processed.setEnderecoProcessado(evento.getEndereco() != null ? 
                toTitleCase(evento.getEndereco()) : null);

            processed.setObservacoes("Dados transformados: nome em maiúsculo, email em minúsculo, CPF e telefone sem formatação");
        } else {
            processed.setNomeProcessado(evento.getNome());
            processed.setEmailProcessado(evento.getEmail());
            processed.setCpfProcessado(evento.getCpf());
            processed.setTelefoneProcessado(evento.getTelefone());
            processed.setEnderecoProcessado(evento.getEndereco());
            processed.setObservacoes("Dados mantidos sem transformação");
        }

        return processed;
    }

    private void armazenarEmMemoria(ProcessedEventDTO evento) {
        memoryStorage.put(evento.getClienteId(), evento);
        log.debug("Evento armazenado em memória. Total de registros: {}", memoryStorage.size());
    }

    private void salvarEmArquivo(ProcessedEventDTO evento) {
        try {
            // Cria diretório se não existir
            Path directory = Paths.get(outputDirectory);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // Nome do arquivo com timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("cliente_%d_%s_%s.json", 
                evento.getClienteId(), evento.getEventType(), timestamp);
            
            File file = new File(directory.toFile(), fileName);

            // Escreve JSON no arquivo
            try (FileWriter writer = new FileWriter(file)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, evento);
            }

            log.info("Evento salvo em arquivo: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Erro ao salvar evento em arquivo", e);
        }
    }

    public ProcessedEventDTO buscarPorId(Long clienteId) {
        return memoryStorage.get(clienteId);
    }

    public Map<Long, ProcessedEventDTO> listarTodos() {
        return new HashMap<>(memoryStorage);
    }

    public int getTotalProcessados() {
        return memoryStorage.size();
    }

    private String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder titleCase = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return titleCase.toString().trim();
    }
}
