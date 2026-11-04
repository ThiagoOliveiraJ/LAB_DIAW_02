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
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private PedidoAluguel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id")
    private Agente agente;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "credito_id")
    private ContratoCredito credito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoContrato tipo;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "assinado_em", nullable = false)
    private Instant assinadoEm;

    protected Contrato() {
    }

    public Contrato(PedidoAluguel pedido, Agente agente, TipoContrato tipo, BigDecimal valorTotal,
                    ContratoCredito credito) {
        this.pedido = pedido;
        this.agente = agente;
        this.tipo = tipo;
        this.valorTotal = valorTotal;
        this.credito = credito;
        this.assinadoEm = Instant.now();

        Usuario proprietario = definirProprietario();
        pedido.marcarContratado();
        pedido.getAutomovel().registrarPropriedade(proprietario);
    }

    private Usuario definirProprietario() {
        return switch (tipo) {
            case CLIENTE -> pedido.getCliente();
            case EMPRESA -> {
                if (agente.isBanco()) {
                    throw new RegraNegocioException("Contrato do tipo EMPRESA deve ser executado por uma empresa.");
                }
                yield agente;
            }
            case BANCO -> {
                if (credito == null) {
                    throw new RegraNegocioException("Contrato do tipo BANCO exige um contrato de crédito.");
                }
                yield credito.getBanco();
            }
        };
    }

    public Long getId() {
        return id;
    }

    public PedidoAluguel getPedido() {
        return pedido;
    }

    public Agente getAgente() {
        return agente;
    }

    public ContratoCredito getCredito() {
        return credito;
    }

    public TipoContrato getTipo() {
        return tipo;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public Instant getAssinadoEm() {
        return assinadoEm;
    }
}
