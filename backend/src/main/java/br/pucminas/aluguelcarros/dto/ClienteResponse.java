package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Cliente;
import java.util.List;

public record ClienteResponse(Long id, String nome, String login, String rg, String cpf, String endereco,
                              String profissao, List<RendimentoDto> rendimentos) {
    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getLogin(), c.getRg(), c.getCpf(), c.getEndereco(),
                c.getProfissao(), c.getRendimentos().stream().map(RendimentoDto::de).toList());
    }
}
