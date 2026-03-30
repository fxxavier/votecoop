package com.github.felipex.votecoop.dto.response;

import java.util.Map;

/**
 * Item de uma tela SELECAO.
 * JSON: { "texto": "...", "url": "...", "body": { ... } }
 */
public record ItemSelecao(String texto, String url, Map<String, Object> body) {

    public ItemSelecao(String texto, String url) {
        this(texto, url, Map.of());
    }
}
