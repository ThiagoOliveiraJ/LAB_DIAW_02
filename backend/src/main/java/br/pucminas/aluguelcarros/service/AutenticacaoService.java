package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.LoginRequest;
import br.pucminas.aluguelcarros.dto.LoginResponse;
import br.pucminas.aluguelcarros.dto.UsuarioResponse;
import br.pucminas.aluguelcarros.exception.CredenciaisInvalidasException;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AutenticacaoService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    private final String hashFicticio;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokens = tokens;
        this.hashFicticio = encoder.encode("usuario-inexistente");
    }

    public LoginResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarios.findByLogin(request.login().trim()).orElse(null);
        String hash = usuario == null ? hashFicticio : usuario.getSenhaHash();
        if (!encoder.matches(request.senha(), hash) || usuario == null) {
            throw new CredenciaisInvalidasException();
        }
        return new LoginResponse(tokens.gerar(usuario), UsuarioResponse.de(usuario));
    }

    public UsuarioResponse buscarAtual(Long id) {
        return usuarios.findById(id).map(UsuarioResponse::de).orElseThrow(CredenciaisInvalidasException::new);
    }
}
