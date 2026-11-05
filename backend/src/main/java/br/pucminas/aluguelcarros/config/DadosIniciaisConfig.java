package br.pucminas.aluguelcarros.config;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.TipoAgente;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DadosIniciaisConfig {

    @Bean
    ApplicationRunner carregarDadosIniciais(@Value("${app.seed.senha}") String senha,
                                            UsuarioRepository usuarios, AgenteRepository agentes,
                                            AutomovelRepository automoveis, PasswordEncoder encoder,
                                            TransactionTemplate transacao) {
        return args -> {
            if (senha.length() < 8) {
                throw new IllegalStateException("APP_SEED_SENHA deve ter ao menos 8 caracteres.");
            }
            if (usuarios.existsByLogin("locadora")) {
                return;
            }
            transacao.executeWithoutResult(status -> {
                String hash = encoder.encode(senha);
                Agente locadora = agentes.save(new Agente("locadora", hash, "Locadora Horizonte",
                        TipoAgente.EMPRESA, "11222333000181"));
                agentes.save(new Agente("banco", hash, "Banco Aliança", TipoAgente.BANCO, "11444777000161"));
                automoveis.save(new Automovel("MAT-0001", 2024, "Volkswagen", "Polo", "RIO2A18", locadora));
                automoveis.save(new Automovel("MAT-0002", 2023, "Fiat", "Pulse", "BHZ4C27", locadora));
                automoveis.save(new Automovel("MAT-0003", 2025, "Toyota", "Corolla", "SPA1B23", locadora));
            });
        };
    }
}
