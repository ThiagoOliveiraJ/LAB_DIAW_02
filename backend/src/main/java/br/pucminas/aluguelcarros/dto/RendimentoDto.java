package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Rendimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RendimentoDto(
        @NotBlank(message = "Informe a entidade empregadora.") @Size(max = 150) String entidadeEmpregadora,
        @NotNull(message = "Informe o valor.")
        @DecimalMin(value = "0.01", message = "O rendimento deve ser positivo.")
        @Digits(integer = 10, fraction = 2) BigDecimal valor) {

    public static RendimentoDto de(Rendimento rendimento) {
        return new RendimentoDto(rendimento.getEntidadeEmpregadora(), rendimento.getValor());
    }

    public Rendimento paraEntidade() {
        return new Rendimento(entidadeEmpregadora.trim(), valor);
    }
}
