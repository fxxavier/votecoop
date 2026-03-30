package com.github.felipex.votecoop.dto.response;

import java.util.List;

/**
 * Tela do tipo SELECAO para o app mobile.
 * Exibe uma lista de opções clicáveis.
 */
public record TelaSelecao(
        String tipo,
        String titulo,
        List<ItemSelecao> itens
) {
    public TelaSelecao(String titulo, List<ItemSelecao> itens) {
        this("SELECAO", titulo, itens);
    }
}
