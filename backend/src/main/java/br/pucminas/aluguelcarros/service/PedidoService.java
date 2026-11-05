package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.ParecerRequest;
import br.pucminas.aluguelcarros.dto.PedidoRequest;
import br.pucminas.aluguelcarros.dto.PedidoResponse;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.ParecerFinanceiro;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PedidoService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final long SEM_PEDIDO = -1L;

    private final PedidoAluguelRepository pedidos;
    private final ClienteRepository clientes;
    private final AutomovelRepository automoveis;
    private final AgenteRepository agentes;

    public PedidoService(PedidoAluguelRepository pedidos, ClienteRepository clientes,
                         AutomovelRepository automoveis, AgenteRepository agentes) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.automoveis = automoveis;
        this.agentes = agentes;
    }

    public PedidoResponse criar(PedidoRequest r, Long clienteId) {
        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        if (r.dataInicio().isBefore(hoje())) {
            throw new RegraNegocioException("A data de início não pode estar no passado.");
        }
        Automovel automovel = obterAutomovelAlugavel(r, SEM_PEDIDO);
        PedidoAluguel pedido = new PedidoAluguel(cliente, automovel, r.dataInicio(), r.dataFim(), limpar(r.observacoes()));
        return PedidoResponse.de(pedidos.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioAutenticado solicitante) {
        List<PedidoAluguel> resultado = solicitante.isAgente()
                ? pedidos.findAllByOrderByCriadoEmDesc()
                : pedidos.findByClienteIdOrderByCriadoEmDesc(solicitante.id());
        return resultado.stream().map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id, UsuarioAutenticado solicitante) {
        return PedidoResponse.de(obterVisivel(id, solicitante));
    }

    public PedidoResponse modificar(Long id, PedidoRequest r, UsuarioAutenticado solicitante) {
        PedidoAluguel pedido = obterVisivel(id, solicitante);
        if (!r.dataInicio().equals(pedido.getDataInicio()) && r.dataInicio().isBefore(hoje())) {
            throw new RegraNegocioException("A data de início não pode estar no passado.");
        }
        Automovel automovel = obterAutomovelAlugavel(r, id);
        pedido.modificar(solicitante.perfil(), automovel, r.dataInicio(), r.dataFim(), limpar(r.observacoes()));
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse cancelar(Long id, UsuarioAutenticado solicitante) {
        PedidoAluguel pedido = obterVisivel(id, solicitante);
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse avaliar(Long id, ParecerRequest r, Long agenteId) {
        PedidoAluguel pedido = pedidos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
        Agente agente = agentes.findById(agenteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agente não encontrado."));
        pedido.avaliar(new ParecerFinanceiro(agente, r.resultado(), r.justificativa().trim()));
        return PedidoResponse.de(pedido);
    }

    private PedidoAluguel obterVisivel(Long id, UsuarioAutenticado solicitante) {
        return pedidos.findById(id)
                .filter(p -> solicitante.isAgente() || p.getCliente().getId().equals(solicitante.id()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }

    private Automovel obterAutomovelAlugavel(PedidoRequest r, Long pedidoId) {
        if (r.dataFim().isBefore(r.dataInicio())) {
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início.");
        }
        Automovel automovel = automoveis.findById(r.automovelId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel não encontrado."));
        if (!automovel.isAlugavel()) {
            throw new RegraNegocioException("Este automóvel não está disponível para aluguel.");
        }
        if (pedidos.existeConflito(automovel.getId(), StatusPedido.CONTRATADO, pedidoId, r.dataInicio(), r.dataFim())) {
            throw new RegraNegocioException("O automóvel já está contratado neste período.");
        }
        return automovel;
    }

    private static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    private static String limpar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
