package com.github.felipex.votecoop.dto.response;

/**
 * Campo de entrada numérica em um FORMULARIO.
 * JSON: { "tipo": "INPUT_NUMERO", "id": "...", "titulo": "...", "valor": 0 }
 */
public record ItemInputNumero(String tipo, String id, String titulo, Number valor)
        implements ItemFormulario {

    public ItemInputNumero(String id, String titulo, Number valor) {
        this("INPUT_NUMERO", id, titulo, valor);
    }
}
