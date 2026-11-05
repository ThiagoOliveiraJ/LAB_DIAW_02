package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Perfil;

public record AutomovelResponse(Long id, String matricula, Integer ano, String marca, String modelo, String placa,
                                Long proprietarioId, String proprietarioNome, Perfil proprietarioPerfil,
                                boolean alugavel) {
    public static AutomovelResponse de(Automovel a) {
        var dono = a.getProprietario();
        return new AutomovelResponse(a.getId(), a.getMatricula(), a.getAno(), a.getMarca(), a.getModelo(),
                a.getPlaca(), dono.getId(), dono.getNome(), dono.getPerfil(), a.isAlugavel());
    }
}
