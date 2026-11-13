package br.com.bancolab.conta.adapter.out.persistence;

import org.springframework.stereotype.Component;

import br.com.bancolab.conta.application.port.out.ContaRepository;
import br.com.bancolab.conta.domain.model.Conta;
import br.com.bancolab.conta.domain.model.Cpf;

/**
 * ADAPTADOR DE SAIDA: implementa a porta ContaRepository usando JPA.
 * Trocar H2 por SQL Server, ou JPA por MongoDB, muda apenas este pacote.
 */
@Component
class ContaPersistenceAdapter implements ContaRepository {

    private final ContaJpaRepository jpaRepository;

    ContaPersistenceAdapter(ContaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conta salvar(Conta conta) {
        ContaJpaEntity salva = jpaRepository.save(paraEntidade(conta));
        return paraDominio(salva);
    }

    @Override
    public boolean existePorCpf(Cpf cpf) {
        return jpaRepository.existsByCpf(cpf.numero());
    }

    private static ContaJpaEntity paraEntidade(Conta conta) {
        return new ContaJpaEntity(
                conta.getId(),
                conta.getTitular(),
                conta.getCpf().numero(),
                conta.getSaldo(),
                conta.getStatus(),
                conta.getCriadaEm());
    }

    private static Conta paraDominio(ContaJpaEntity entidade) {
        return Conta.reconstituir(
                entidade.getId(),
                entidade.getTitular(),
                new Cpf(entidade.getCpf()),
                entidade.getSaldo(),
                entidade.getStatus(),
                entidade.getCriadaEm());
    }
}
