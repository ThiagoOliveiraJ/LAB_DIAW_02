package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Perfil;
import br.pucminas.aluguelcarros.model.TipoAgente;
import br.pucminas.aluguelcarros.model.Usuario;

public record UsuarioResponse(Long id, String nome, String login, Perfil perfil, TipoAgente tipoAgente) {
    public static UsuarioResponse de(Usuario usuario) {
        TipoAgente tipo = usuario instanceof Agente agente ? agente.getTipo() : null;
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getLogin(), usuario.getPerfil(), tipo);
    }
}
