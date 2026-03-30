package com.github.felipex.votecoop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String baseUrl,
        Sessao sessao,
        Usercheck usercheck
) {
    public record Sessao(int duracaoPadraoMinutos) {}
    public record Usercheck(String baseUrl) {}
}
