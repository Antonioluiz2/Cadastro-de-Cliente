package com.testetecnico.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEventDTO {

    private String eventType;
    private Long clienteId;
    private String nomeProcessado;
    private String emailProcessado;
    private String cpfProcessado;
    private String telefoneProcessado;
    private String enderecoProcessado;
    private String status;
    private String timestampOriginal;
    private LocalDateTime timestampProcessamento;
    private String observacoes;
}
