package br.pucminas.aluguelcarros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteCadastroRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 150) String nome,
        @NotBlank(message = "Informe o usuário.") @Size(min = 3, max = 60, message = "O usuário deve ter de 3 a 60 caracteres.") String login,
        @NotBlank(message = "Informe a senha.") @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres.") String senha,
        @NotBlank(message = "Informe o RG.") @Size(max = 20) String rg,
        @NotBlank(message = "Informe o CPF.") @CPF(message = "CPF inválido.") String cpf,
        @NotBlank(message = "Informe o endereço.") @Size(max = 255) String endereco,
        @NotBlank(message = "Informe a profissão.") @Size(max = 100) String profissao,
        @Size(max = 3, message = "Informe no máximo 3 rendimentos.") List<@Valid RendimentoDto> rendimentos) {
}
