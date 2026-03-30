package com.github.felipex.votecoop.dto.request;

import jakarta.validation.constraints.Min;

public record AbrirSessaoRequest(
        @Min(value = 1, message = "A duração mínima da sessão é 1 minuto")
        Integer duracaoMinutos
) {
}
