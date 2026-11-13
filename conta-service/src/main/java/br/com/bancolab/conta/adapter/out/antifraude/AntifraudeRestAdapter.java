package br.com.bancolab.conta.adapter.out.antifraude;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.bancolab.conta.application.port.out.AntifraudePort;
import br.com.bancolab.conta.domain.model.Cpf;

/**
 * ADAPTADOR DE SAIDA: implementa a porta AntifraudePort chamando o
 * antifraude-service por REST.
 *
 * ATENCAO: esta implementacao e ingenua de proposito. Nao tem timeout, retry,
 * circuit breaker nem fallback. Se o antifraude ficar lento ou cair, este
 * servico sofre junto. Consertar isso e o Desafio 5.
 */
@Component
class AntifraudeRestAdapter implements AntifraudePort {

    private final RestClient restClient;

    AntifraudeRestAdapter(RestClient.Builder builder, @Value("${antifraude.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public ResultadoAntifraude analisar(Cpf cpf) {
        AnaliseResponse resposta = restClient.get()
                .uri("/analises/{cpf}", cpf.numero())
                .retrieve()
                .body(AnaliseResponse.class);

        return resposta != null && "APROVADO".equals(resposta.resultado())
                ? ResultadoAntifraude.APROVADO
                : ResultadoAntifraude.REPROVADO;
    }

    /** O JSON do outro sistema. Fica preso neste pacote: o dominio nao conhece este formato. */
    record AnaliseResponse(String cpf, String resultado, String motivo) {
    }
}
