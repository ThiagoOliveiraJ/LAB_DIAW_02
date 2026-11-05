package br.pucminas.aluguelcarros.security;

import br.pucminas.aluguelcarros.model.Perfil;
import org.springframework.security.oauth2.jwt.Jwt;

public record UsuarioAutenticado(Long id, Perfil perfil) {

    public static UsuarioAutenticado de(Jwt jwt) {
        return new UsuarioAutenticado(Long.valueOf(jwt.getSubject()), Perfil.valueOf(jwt.getClaimAsString("perfil")));
    }

    public boolean isAgente() {
        return perfil == Perfil.AGENTE;
    }
}
