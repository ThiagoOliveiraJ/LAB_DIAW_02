package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.TipoContrato;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ContratoRequest(
        @NotNull(message = "Informe o tipo de contrato.") TipoContrato tipo,
        @NotNull(message = "Informe o valor total.")
        @DecimalMin(value = "0.01", message = "O valor deve ser positivo.")
        @Digits(integer = 10, fraction = 2) BigDecimal valorTotal,
        @Valid Credito credito) {

    public record Credito(
            @NotNull(message = "Selecione o banco.") Long bancoId,
            @NotNull(message = "Informe o valor do crédito.")
            @DecimalMin(value = "0.01", message = "O valor deve ser positivo.")
            @Digits(integer = 10, fraction = 2) BigDecimal valor,
            @NotNull(message = "Informe a taxa de juros.")
            @DecimalMin(value = "0.00", message = "A taxa não pode ser negativa.")
            @Digits(integer = 3, fraction = 2) BigDecimal taxaJurosMensal,
            @NotNull(message = "Informe as parcelas.") @Min(value = 1, message = "Informe ao menos 1 parcela.") Integer parcelas) {
    }
}
