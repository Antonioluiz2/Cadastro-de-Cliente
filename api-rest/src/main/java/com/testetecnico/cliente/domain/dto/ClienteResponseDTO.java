package com.testetecnico.cliente.domain.dto;

import com.testetecnico.cliente.domain.entity.StatusCliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String endereco;
    private StatusCliente status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
