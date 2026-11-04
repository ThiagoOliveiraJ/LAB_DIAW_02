package br.pucminas.aluguelcarros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "parecer_financeiro")
public class ParecerFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id")
    private Agente agente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ResultadoParecer resultado;

    @Column(nullable = false, length = 500)
    private String justificativa;

    @Column(name = "emitido_em", nullable = false)
    private Instant emitidoEm;

    protected ParecerFinanceiro() {
    }

    public ParecerFinanceiro(Agente agente, ResultadoParecer resultado, String justificativa) {
        this.agente = agente;
        this.resultado = resultado;
        this.justificativa = justificativa;
        this.emitidoEm = Instant.now();
    }

    public Agente getAgente() {
        return agente;
    }

    public ResultadoParecer getResultado() {
        return resultado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public Instant getEmitidoEm() {
        return emitidoEm;
    }
}
