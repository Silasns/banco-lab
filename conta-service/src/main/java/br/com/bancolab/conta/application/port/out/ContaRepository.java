package br.com.bancolab.conta.application.port.out;

import br.com.bancolab.conta.domain.model.Conta;
import br.com.bancolab.conta.domain.model.Cpf;

/**
 * PORTA DE SAIDA (driven port): o que a aplicacao precisa que alguem faca por ela.
 *
 * Quem chama: a aplicacao. Quem implementa: um adaptador de saida
 * (ContaPersistenceAdapter). A interface fica DENTRO do hexagono e fala a
 * lingua do dominio (Conta, Cpf), nunca a lingua do banco (entidade JPA, SQL).
 */
public interface ContaRepository {

    Conta salvar(Conta conta);

    boolean existePorCpf(Cpf cpf);
}
