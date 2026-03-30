package com.github.felipex.votecoop.dto.response;

import java.util.List;

/**
 * Tela do tipo FORMULARIO para o app mobile.
 * Exibe campos de entrada e até dois botões de ação.
 */
public record TelaFormulario(
        String tipo,
        String titulo,
        List<ItemFormulario> itens,
        BotaoAcao botaoOk,
        BotaoAcao botaoCancelar
) {
    public TelaFormulario(String titulo, List<ItemFormulario> itens, BotaoAcao botaoOk, BotaoAcao botaoCancelar) {
        this("FORMULARIO", titulo, itens, botaoOk, botaoCancelar);
    }

    public TelaFormulario(String titulo, List<ItemFormulario> itens, BotaoAcao botaoOk) {
        this("FORMULARIO", titulo, itens, botaoOk, null);
    }
}
