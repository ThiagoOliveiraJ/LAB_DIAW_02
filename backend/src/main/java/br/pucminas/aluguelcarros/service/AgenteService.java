package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AgenteRequest;
import br.pucminas.aluguelcarros.dto.AgenteResponse;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.TipoAgente;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AgenteService {

    private final AgenteRepository agentes;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public AgenteService(AgenteRepository agentes, UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.agentes = agentes;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public AgenteResponse cadastrar(AgenteRequest r) {
        String login = r.login().trim();
        String cnpj = r.cnpj().replaceAll("\\D", "");
        if (usuarios.existsByLogin(login)) {
            throw new RegraNegocioException("Este usuário já está em uso.");
        }
        if (agentes.existsByCnpj(cnpj)) {
            throw new RegraNegocioException("Já existe um agente com este CNPJ.");
        }
        Agente agente = new Agente(login, encoder.encode(r.senha()), r.nome().trim(), r.tipo(), cnpj);
        return AgenteResponse.de(agentes.save(agente));
    }

    @Transactional(readOnly = true)
    public List<AgenteResponse> listar(TipoAgente tipo) {
        List<Agente> resultado = tipo == null ? agentes.findAll(Sort.by("nome")) : agentes.findByTipoOrderByNome(tipo);
        return resultado.stream().map(AgenteResponse::de).toList();
    }
}
