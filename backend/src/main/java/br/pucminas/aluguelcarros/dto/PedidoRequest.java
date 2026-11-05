package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PedidoRequest(
        @NotNull(message = "Selecione o automóvel.") Long automovelId,
        @NotNull(message = "Informe a data de início.") LocalDate dataInicio,
        @NotNull(message = "Informe a data de fim.") LocalDate dataFim,
        @Size(max = 500, message = "Use no máximo 500 caracteres.") String observacoes) {
}
