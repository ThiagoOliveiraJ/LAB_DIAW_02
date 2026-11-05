package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.ParecerFinanceiro;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.ResultadoParecer;
import br.pucminas.aluguelcarros.model.StatusPedido;
import java.time.Instant;
import java.time.LocalDate;

public record PedidoResponse(Long id, StatusPedido status, LocalDate dataInicio, LocalDate dataFim,
                             String observacoes, Instant criadoEm, Long clienteId, String clienteNome,
                             AutomovelResponse automovel, Parecer parecer) {

    public record Parecer(ResultadoParecer resultado, String justificativa, String agenteNome, Instant emitidoEm) {
        static Parecer de(ParecerFinanceiro p) {
            return p == null ? null
                    : new Parecer(p.getResultado(), p.getJustificativa(), p.getAgente().getNome(), p.getEmitidoEm());
        }
    }

    public static PedidoResponse de(PedidoAluguel p) {
        return new PedidoResponse(p.getId(), p.getStatus(), p.getDataInicio(), p.getDataFim(), p.getObservacoes(),
                p.getCriadoEm(), p.getCliente().getId(), p.getCliente().getNome(),
                AutomovelResponse.de(p.getAutomovel()), Parecer.de(p.getParecer()));
    }
}
