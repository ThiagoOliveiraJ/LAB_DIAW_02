package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
public class Cliente extends Usuario {

    public static final int MAX_RENDIMENTOS = 3;

    @Column(nullable = false, unique = true, length = 20)
    private String rg;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false, length = 100)
    private String profissao;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rendimento> rendimentos = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String login, String senhaHash, String nome, String rg, String cpf,
                   String endereco, String profissao) {
        super(login, senhaHash, nome);
        this.rg = rg;
        this.cpf = cpf;
        this.endereco = endereco;
        this.profissao = profissao;
    }

    public void substituirRendimentos(List<Rendimento> novos) {
        if (novos.size() > MAX_RENDIMENTOS) {
            throw new RegraNegocioException("Um cliente pode ter no máximo " + MAX_RENDIMENTOS + " rendimentos.");
        }
        rendimentos.clear();
        novos.forEach(rendimento -> {
            rendimento.setCliente(this);
            rendimentos.add(rendimento);
        });
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.CLIENTE;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public List<Rendimento> getRendimentos() {
        return List.copyOf(rendimentos);
    }
}
