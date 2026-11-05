package br.pucminas.aluguelcarros.dto;

public record LoginResponse(String token, UsuarioResponse usuario) {
}
