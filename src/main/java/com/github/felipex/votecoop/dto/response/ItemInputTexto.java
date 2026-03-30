package com.github.felipex.votecoop.dto.response;

/**
 * Campo de entrada de texto em um FORMULARIO.
 * JSON: { "tipo": "INPUT_TEXTO", "id": "...", "titulo": "...", "valor": "..." }
 */
public record ItemInputTexto(String tipo, String id, String titulo, String valor)
        implements ItemFormulario {

    public ItemInputTexto(String id, String titulo, String valor) {
        this("INPUT_TEXTO", id, titulo, valor);
    }

    public ItemInputTexto(String id, String titulo) {
        this("INPUT_TEXTO", id, titulo, "");
    }
}
