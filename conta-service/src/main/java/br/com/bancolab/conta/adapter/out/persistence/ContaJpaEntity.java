package br.com.bancolab.conta.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.bancolab.conta.domain.model.StatusConta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representacao da conta NO BANCO. E uma classe diferente da Conta do dominio
 * de proposito: o modelo de negocio nao precisa mudar quando a tabela muda.
 */
@Entity
@Table(name = "conta")
class ContaJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String titular;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected ContaJpaEntity() {
        // exigido pelo JPA
    }

    ContaJpaEntity(UUID id, String titular, String cpf, BigDecimal saldo, StatusConta status, Instant criadaEm) {
        this.id = id;
        this.titular = titular;
        this.cpf = cpf;
        this.saldo = saldo;
        this.status = status;
        this.criadaEm = criadaEm;
    }

    UUID getId() {
        return id;
    }

    String getTitular() {
        return titular;
    }

    String getCpf() {
        return cpf;
    }

    BigDecimal getSaldo() {
        return saldo;
    }

    StatusConta getStatus() {
        return status;
    }

    Instant getCriadaEm() {
        return criadaEm;
    }
}
