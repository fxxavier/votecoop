package com.github.felipex.votecoop.dto.response;

/**
 * Marker interface para os itens da tela FORMULARIO.
 * Cada implementação representa um tipo de campo/elemento da tela.
 */
public sealed interface ItemFormulario
        permits ItemTexto, ItemInputTexto, ItemInputNumero {
}
