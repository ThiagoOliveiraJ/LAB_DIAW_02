package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.ResultadoParecer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParecerRequest(
        @NotNull(message = "Informe o resultado.") ResultadoParecer resultado,
        @NotBlank(message = "Informe a justificativa.") @Size(max = 500) String justificativa) {
}
