package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.ClienteAtualizacaoRequest;
import br.pucminas.aluguelcarros.dto.ClienteCadastroRequest;
import br.pucminas.aluguelcarros.dto.ClienteResponse;
import br.pucminas.aluguelcarros.dto.RendimentoDto;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Rendimento;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;
    private final PedidoAluguelRepository pedidos;
    private final PasswordEncoder encoder;

    public ClienteService(ClienteRepository clientes, UsuarioRepository usuarios, PedidoAluguelRepository pedidos,
                          PasswordEncoder encoder) {
        this.clientes = clientes;
        this.usuarios = usuarios;
        this.pedidos = pedidos;
        this.encoder = encoder;
    }

    public ClienteResponse cadastrar(ClienteCadastroRequest r) {
        String login = r.login().trim();
        String cpf = somenteDigitos(r.cpf());
        String rg = r.rg().trim();
        if (usuarios.existsByLogin(login)) {
            throw new RegraNegocioException("Este usuário já está em uso.");
        }
        if (clientes.existsByCpf(cpf)) {
            throw new RegraNegocioException("Já existe um cliente com este CPF.");
        }
        if (clientes.existsByRg(rg)) {
            throw new RegraNegocioException("Já existe um cliente com este RG.");
        }
        Cliente cliente = new Cliente(login, encoder.encode(r.senha()), r.nome().trim(), rg, cpf,
                r.endereco().trim(), r.profissao().trim());
        cliente.substituirRendimentos(paraEntidades(r.rendimentos()));
        return ClienteResponse.de(clientes.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clientes.findAll(Sort.by("nome")).stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id, UsuarioAutenticado solicitante) {
        exigirAcesso(id, solicitante);
        return ClienteResponse.de(obter(id));
    }

    public ClienteResponse atualizar(Long id, ClienteAtualizacaoRequest r, UsuarioAutenticado solicitante) {
        exigirAcesso(id, solicitante);
        Cliente cliente = obter(id);
        String cpf = somenteDigitos(r.cpf());
        String rg = r.rg().trim();
        if (clientes.existsByCpfAndIdNot(cpf, id)) {
            throw new RegraNegocioException("Já existe um cliente com este CPF.");
        }
        if (clientes.existsByRgAndIdNot(rg, id)) {
            throw new RegraNegocioException("Já existe um cliente com este RG.");
        }
        cliente.setNome(r.nome().trim());
        cliente.setRg(rg);
        cliente.setCpf(cpf);
        cliente.setEndereco(r.endereco().trim());
        cliente.setProfissao(r.profissao().trim());
        cliente.substituirRendimentos(paraEntidades(r.rendimentos()));
        if (r.senha() != null && !r.senha().isBlank()) {
            cliente.setSenhaHash(encoder.encode(r.senha()));
        }
        return ClienteResponse.de(cliente);
    }

    public void excluir(Long id, UsuarioAutenticado solicitante) {
        exigirAcesso(id, solicitante);
        Cliente cliente = obter(id);
        if (pedidos.existsByClienteId(id)) {
            throw new RegraNegocioException("Não é possível excluir um cliente que possui pedidos de aluguel.");
        }
        clientes.delete(cliente);
    }

    private void exigirAcesso(Long id, UsuarioAutenticado solicitante) {
        if (!solicitante.isAgente() && !solicitante.id().equals(id)) {
            throw new AccessDeniedException("Acesso restrito aos dados do próprio cliente.");
        }
    }

    private Cliente obter(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
    }

    private static List<Rendimento> paraEntidades(List<RendimentoDto> rendimentos) {
        return rendimentos == null ? List.of() : rendimentos.stream().map(RendimentoDto::paraEntidade).toList();
    }

    private static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
