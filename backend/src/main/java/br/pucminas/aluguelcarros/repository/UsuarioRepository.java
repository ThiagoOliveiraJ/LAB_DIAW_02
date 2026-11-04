package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);
}
