package com.github.felipex.votecoop.client;

import com.github.felipex.votecoop.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserCheckClient {

    private static final Logger log = LoggerFactory.getLogger(UserCheckClient.class);
    private static final String ABLE_TO_VOTE = "ABLE_TO_VOTE";

    private final RestClient restClient;

    public UserCheckClient(@Value("${app.usercheck.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void verificarAssociado(String cpf) {
        try {
            UserCheckResponse response = restClient.get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (req, res) -> { throw new AppException(AppException.Tipo.ASSOCIADO_NAO_ENCONTRADO, cpf); })
                    .body(UserCheckResponse.class);

            if (response == null || !ABLE_TO_VOTE.equals(response.status())) {
                throw new AppException(AppException.Tipo.ASSOCIADO_INAPTO, cpf);
            }
        } catch (AppException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("Erro ao contactar serviço de verificação de associados", e);
            throw new AppException(AppException.Tipo.SERVICO_INDISPONIVEL);
        }
    }

    private record UserCheckResponse(String status) {}
}
