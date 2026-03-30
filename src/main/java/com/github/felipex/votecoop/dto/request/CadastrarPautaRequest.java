package com.github.felipex.votecoop.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CadastrarPautaRequest(
        @NotBlank(message = "O título da pauta é obrigatório")
        String titulo,

        String descricao
) {
}
