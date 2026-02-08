package com.testetecnico.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEventDTO {

    private String eventType;
    private Long clienteId;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String endereco;
    private String status;
    private String timestamp;
}
