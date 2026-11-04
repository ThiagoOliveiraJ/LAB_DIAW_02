package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.StatusPedido;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoAluguelRepository extends JpaRepository<PedidoAluguel, Long> {
    List<PedidoAluguel> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

    List<PedidoAluguel> findAllByOrderByCriadoEmDesc();

    boolean existsByClienteId(Long clienteId);

    boolean existsByAutomovelId(Long automovelId);

    @Query("""
            select count(p) > 0 from PedidoAluguel p
            where p.automovel.id = :automovelId
              and p.status = :status
              and p.id <> :ignorarPedidoId
              and p.dataInicio <= :fim
              and p.dataFim >= :inicio
            """)
    boolean existeConflito(@Param("automovelId") Long automovelId,
                           @Param("status") StatusPedido status,
                           @Param("ignorarPedidoId") Long ignorarPedidoId,
                           @Param("inicio") LocalDate inicio,
                           @Param("fim") LocalDate fim);
}
