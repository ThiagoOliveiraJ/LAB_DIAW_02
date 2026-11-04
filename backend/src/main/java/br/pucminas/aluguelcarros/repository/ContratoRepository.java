package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Contrato;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    Optional<Contrato> findByPedidoId(Long pedidoId);

    List<Contrato> findByPedidoClienteIdOrderByAssinadoEmDesc(Long clienteId);

    List<Contrato> findAllByOrderByAssinadoEmDesc();
}
