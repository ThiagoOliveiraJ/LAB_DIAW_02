package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.TipoContrato;
import java.math.BigDecimal;
import java.time.Instant;

public record ContratoResponse(Long id, Long pedidoId, TipoContrato tipo, BigDecimal valorTotal, Instant assinadoEm,
                               String agenteNome, String clienteNome, AutomovelResponse automovel, Credito credito) {

    public record Credito(String bancoNome, BigDecimal valor, BigDecimal taxaJurosMensal, Integer parcelas) {
        static Credito de(ContratoCredito c) {
            return c == null ? null
                    : new Credito(c.getBanco().getNome(), c.getValor(), c.getTaxaJurosMensal(), c.getParcelas());
        }
    }

    public static ContratoResponse de(Contrato c) {
        var pedido = c.getPedido();
        return new ContratoResponse(c.getId(), pedido.getId(), c.getTipo(), c.getValorTotal(), c.getAssinadoEm(),
                c.getAgente().getNome(), pedido.getCliente().getNome(), AutomovelResponse.de(pedido.getAutomovel()),
                Credito.de(c.getCredito()));
    }
}
