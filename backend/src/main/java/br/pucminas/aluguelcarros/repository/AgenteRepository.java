package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.TipoAgente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenteRepository extends JpaRepository<Agente, Long> {
    boolean existsByCnpj(String cnpj);

    List<Agente> findByTipoOrderByNome(TipoAgente tipo);
}
