package br.pucminas.aluguelcarros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "agente")
public class Agente extends Usuario {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoAgente tipo;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    protected Agente() {
    }

    public Agente(String login, String senhaHash, String nome, TipoAgente tipo, String cnpj) {
        super(login, senhaHash, nome);
        this.tipo = tipo;
        this.cnpj = cnpj;
    }

    public boolean isBanco() {
        return tipo == TipoAgente.BANCO;
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.AGENTE;
    }

    public TipoAgente getTipo() {
        return tipo;
    }

    public String getCnpj() {
        return cnpj;
    }
}
