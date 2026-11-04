package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "pedido_aluguel")
public class PedidoAluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long versao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automovel_id")
    private Automovel automovel;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "parecer_id")
    private ParecerFinanceiro parecer;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(length = 500)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusPedido status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected PedidoAluguel() {
    }

    public PedidoAluguel(Cliente cliente, Automovel automovel, LocalDate dataInicio, LocalDate dataFim,
                         String observacoes) {
        this.cliente = cliente;
        this.automovel = automovel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.observacoes = observacoes;
        this.status = StatusPedido.EM_ANALISE;
        this.criadoEm = Instant.now();
    }

    public boolean podeSerModificadoPor(Perfil perfil) {
        return status == StatusPedido.EM_ANALISE
                || (perfil == Perfil.AGENTE && status == StatusPedido.APROVADO);
    }

    public void modificar(Perfil perfil, Automovel automovel, LocalDate dataInicio, LocalDate dataFim,
                          String observacoes) {
        if (!podeSerModificadoPor(perfil)) {
            throw new RegraNegocioException("Um pedido com status " + status + " não pode ser modificado.");
        }
        this.automovel = automovel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.observacoes = observacoes;
    }

    public void cancelar() {
        if (status != StatusPedido.EM_ANALISE && status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Um pedido com status " + status + " não pode ser cancelado.");
        }
        status = StatusPedido.CANCELADO;
    }

    public void avaliar(ParecerFinanceiro parecer) {
        if (status != StatusPedido.EM_ANALISE) {
            throw new RegraNegocioException("Apenas pedidos em análise podem ser avaliados.");
        }
        this.parecer = parecer;
        status = parecer.getResultado() == ResultadoParecer.POSITIVO
                ? StatusPedido.APROVADO
                : StatusPedido.REPROVADO;
    }

    public void marcarContratado() {
        if (status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("O contrato só pode ser executado para pedidos com parecer positivo.");
        }
        status = StatusPedido.CONTRATADO;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Automovel getAutomovel() {
        return automovel;
    }

    public ParecerFinanceiro getParecer() {
        return parecer;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
