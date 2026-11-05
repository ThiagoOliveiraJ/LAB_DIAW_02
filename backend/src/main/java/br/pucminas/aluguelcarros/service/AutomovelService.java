package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AutomovelRequest;
import br.pucminas.aluguelcarros.dto.AutomovelResponse;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AutomovelService {

    private final AutomovelRepository automoveis;
    private final AgenteRepository agentes;
    private final PedidoAluguelRepository pedidos;

    public AutomovelService(AutomovelRepository automoveis, AgenteRepository agentes,
                            PedidoAluguelRepository pedidos) {
        this.automoveis = automoveis;
        this.agentes = agentes;
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar(boolean somenteAlugaveis) {
        return automoveis.findAll(Sort.by("marca", "modelo")).stream()
                .filter(a -> !somenteAlugaveis || a.isAlugavel())
                .map(AutomovelResponse::de)
                .toList();
    }

    public AutomovelResponse cadastrar(AutomovelRequest r, Long agenteId) {
        String placa = normalizarPlaca(r.placa());
        String matricula = r.matricula().trim();
        if (automoveis.existsByPlaca(placa)) {
            throw new RegraNegocioException("Já existe um automóvel com esta placa.");
        }
        if (automoveis.existsByMatricula(matricula)) {
            throw new RegraNegocioException("Já existe um automóvel com esta matrícula.");
        }
        Agente dono = agentes.findById(agenteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agente não encontrado."));
        Automovel automovel = new Automovel(matricula, r.ano(), r.marca().trim(), r.modelo().trim(), placa, dono);
        return AutomovelResponse.de(automoveis.save(automovel));
    }

    public AutomovelResponse atualizar(Long id, AutomovelRequest r, Long agenteId) {
        Automovel automovel = obterDoProprietario(id, agenteId);
        String placa = normalizarPlaca(r.placa());
        String matricula = r.matricula().trim();
        if (automoveis.existsByPlacaAndIdNot(placa, id)) {
            throw new RegraNegocioException("Já existe um automóvel com esta placa.");
        }
        if (automoveis.existsByMatriculaAndIdNot(matricula, id)) {
            throw new RegraNegocioException("Já existe um automóvel com esta matrícula.");
        }
        automovel.atualizar(matricula, r.ano(), r.marca().trim(), r.modelo().trim(), placa);
        return AutomovelResponse.de(automovel);
    }

    public void excluir(Long id, Long agenteId) {
        Automovel automovel = obterDoProprietario(id, agenteId);
        if (pedidos.existsByAutomovelId(id)) {
            throw new RegraNegocioException("Não é possível excluir um automóvel vinculado a pedidos.");
        }
        automoveis.delete(automovel);
    }

    private Automovel obterDoProprietario(Long id, Long agenteId) {
        Automovel automovel = automoveis.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel não encontrado."));
        if (!automovel.getProprietario().getId().equals(agenteId)) {
            throw new AccessDeniedException("Apenas o proprietário pode alterar o automóvel.");
        }
        return automovel;
    }

    private static String normalizarPlaca(String placa) {
        return placa.replace("-", "").toUpperCase(Locale.ROOT);
    }
}
