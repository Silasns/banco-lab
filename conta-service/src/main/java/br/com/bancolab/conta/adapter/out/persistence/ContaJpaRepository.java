package br.com.bancolab.conta.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio do Spring Data. E um detalhe do adaptador: ninguem fora deste pacote o enxerga. */
interface ContaJpaRepository extends JpaRepository<ContaJpaEntity, UUID> {

    boolean existsByCpf(String cpf);
}
