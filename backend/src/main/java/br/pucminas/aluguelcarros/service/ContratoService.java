package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.ContratoRequest;
import br.pucminas.aluguelcarros.dto.ContratoResponse;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.ContratoRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContratoService {

    private final ContratoRepository contratos;
    private final PedidoAluguelRepository pedidos;
    private final AgenteRepository agentes;
    private final AutomovelRepository automoveis;

    public ContratoService(ContratoRepository contratos, PedidoAluguelRepository pedidos, AgenteRepository agentes,
                           AutomovelRepository automoveis) {
        this.contratos = contratos;
        this.pedidos = pedidos;
        this.agentes = agentes;
        this.automoveis = automoveis;
    }

    public ContratoResponse executar(Long pedidoId, ContratoRequest r, Long agenteId) {
        PedidoAluguel pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
        Agente agente = obterAgente(agenteId);

        Automovel automovel = automoveis.findByIdParaAtualizacao(pedido.getAutomovel().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel não encontrado."));
        if (!automovel.isAlugavel()) {
            throw new RegraNegocioException("Este automóvel não está mais disponível para aluguel.");
        }
        if (pedidos.existeConflito(automovel.getId(), StatusPedido.CONTRATADO, pedidoId,
                pedido.getDataInicio(), pedido.getDataFim())) {
            throw new RegraNegocioException("O automóvel já está contratado neste período.");
        }

        ContratoCredito credito = r.credito() == null ? null : new ContratoCredito(
                obterAgente(r.credito().bancoId()), r.credito().valor(), r.credito().taxaJurosMensal(),
                r.credito().parcelas());
        Contrato contrato = new Contrato(pedido, agente, r.tipo(), r.valorTotal(), credito);
        return ContratoResponse.de(contratos.save(contrato));
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> listar(UsuarioAutenticado solicitante) {
        List<Contrato> resultado = solicitante.isAgente()
                ? contratos.findAllByOrderByAssinadoEmDesc()
                : contratos.findByPedidoClienteIdOrderByAssinadoEmDesc(solicitante.id());
        return resultado.stream().map(ContratoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscarPorPedido(Long pedidoId, UsuarioAutenticado solicitante) {
        return contratos.findByPedidoId(pedidoId)
                .filter(c -> solicitante.isAgente() || c.getPedido().getCliente().getId().equals(solicitante.id()))
                .map(ContratoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato não encontrado."));
    }

    private Agente obterAgente(Long id) {
        return agentes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Agente não encontrado."));
    }
}
