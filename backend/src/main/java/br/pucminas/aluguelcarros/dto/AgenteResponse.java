package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.TipoAgente;

public record AgenteResponse(Long id, String nome, String login, TipoAgente tipo, String cnpj) {
    public static AgenteResponse de(Agente a) {
        return new AgenteResponse(a.getId(), a.getNome(), a.getLogin(), a.getTipo(), a.getCnpj());
    }
}
