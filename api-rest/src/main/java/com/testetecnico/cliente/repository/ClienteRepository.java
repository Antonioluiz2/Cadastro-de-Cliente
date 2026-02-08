package com.testetecnico.cliente.repository;

import com.testetecnico.cliente.domain.entity.Cliente;
import com.testetecnico.cliente.domain.entity.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByCpf(String cpf);
    
    List<Cliente> findByStatus(StatusCliente status);
    
    boolean existsByEmail(String email);
    
    boolean existsByCpf(String cpf);
}
