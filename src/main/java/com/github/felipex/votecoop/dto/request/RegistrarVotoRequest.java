package com.github.felipex.votecoop.dto.request;

import com.github.felipex.votecoop.domain.OpcaoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarVotoRequest(
        @NotBlank(message = "O ID do associado é obrigatório")
        String associadoId,

        @NotNull(message = "A opção de voto é obrigatória (SIM ou NAO)")
        OpcaoVoto opcao
) {
}
