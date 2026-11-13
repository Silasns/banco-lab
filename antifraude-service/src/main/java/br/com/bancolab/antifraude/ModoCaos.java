package br.com.bancolab.antifraude;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Guarda a configuracao de falhas injetadas de proposito neste servico.
 * Por padrao nao ha caos: sem latencia e sem erros.
 */
@Component
public class ModoCaos {

    public record Configuracao(long latenciaMs, double taxaErro) {

        public Configuracao {
            if (latenciaMs < 0) {
                throw new IllegalArgumentException("latenciaMs nao pode ser negativa");
            }
            if (taxaErro < 0 || taxaErro > 1) {
                throw new IllegalArgumentException("taxaErro deve estar entre 0 e 1");
            }
        }
    }

    private final AtomicReference<Configuracao> atual = new AtomicReference<>(new Configuracao(0, 0));

    public Configuracao atual() {
        return atual.get();
    }

    public void configurar(Configuracao nova) {
        atual.set(nova);
    }

    /** Atrasa e/ou derruba a requisicao conforme a configuracao atual. */
    public void aplicar() {
        Configuracao config = atual.get();

        if (config.latenciaMs() > 0) {
            try {
                Thread.sleep(config.latenciaMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (ThreadLocalRandom.current().nextDouble() < config.taxaErro()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Falha injetada pelo modo caos");
        }
    }
}
