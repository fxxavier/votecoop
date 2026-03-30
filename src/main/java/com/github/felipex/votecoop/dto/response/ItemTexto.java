package com.github.felipex.votecoop.dto.response;

/**
 * Item de texto simples (somente leitura) em um FORMULARIO.
 * JSON: { "tipo": "TEXTO", "texto": "..." }
 */
public record ItemTexto(String tipo, String texto) implements ItemFormulario {

    public ItemTexto(String texto) {
        this("TEXTO", texto);
    }
}
