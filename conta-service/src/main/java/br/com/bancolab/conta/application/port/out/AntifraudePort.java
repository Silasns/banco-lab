package br.com.bancolab.conta.application.port.out;

import br.com.bancolab.conta.domain.model.Cpf;

/**
 * PORTA DE SAIDA para o sistema de antifraude. A aplicacao nao sabe (nem quer
 * saber) se a resposta vem por REST, fila, gRPC ou de um duble de teste.
 */
public interface AntifraudePort {

    ResultadoAntifraude analisar(Cpf cpf);

    enum ResultadoAntifraude {
        APROVADO,
        REPROVADO
    }
}
