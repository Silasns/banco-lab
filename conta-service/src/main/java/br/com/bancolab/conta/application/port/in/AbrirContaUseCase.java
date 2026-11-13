package br.com.bancolab.conta.application.port.in;

import br.com.bancolab.conta.domain.model.Conta;

/**
 * PORTA DE ENTRADA (driving port): o que o mundo de fora pode pedir para a aplicacao.
 *
 * Quem chama: adaptadores de entrada (hoje o ContaController; amanha pode ser
 * um consumidor de fila ou um job). Quem implementa: a propria aplicacao
 * (AbrirContaService).
 */
public interface AbrirContaUseCase {

    Conta abrir(AbrirContaCommand command);

    record AbrirContaCommand(String titular, String cpf) {
    }
}
