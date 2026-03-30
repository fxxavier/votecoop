package com.github.felipex.votecoop.dto.response;

import java.util.Map;

/**
 * Botão de ação de um FORMULARIO.
 * JSON: { "texto": "...", "url": "...", "body": { ... } }
 */
public record BotaoAcao(String texto, String url, Map<String, Object> body) {

    public BotaoAcao(String texto, String url) {
        this(texto, url, Map.of());
    }
}
