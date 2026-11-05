package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.TipoAgente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record AgenteRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 150) String nome,
        @NotBlank(message = "Informe o usuário.") @Size(min = 3, max = 60, message = "O usuário deve ter de 3 a 60 caracteres.") String login,
        @NotBlank(message = "Informe a senha.") @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres.") String senha,
        @NotNull(message = "Informe o tipo.") TipoAgente tipo,
        @NotBlank(message = "Informe o CNPJ.") @CNPJ(message = "CNPJ inválido.") String cnpj) {
}
