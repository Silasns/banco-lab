package br.com.bancolab.conta.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.com.bancolab.conta.domain.exception.RegraDeNegocioException;

/**
 * O coracao do hexagono. Repare no que NAO existe neste arquivo: nenhuma
 * anotacao de Spring, de JPA ou de JSON. E Java puro, e por isso da para
 * testar as regras de negocio sem subir nada.
 */
public class Conta {

    private final UUID id;
    private final String titular;
    private final Cpf cpf;
    private BigDecimal saldo;
    private StatusConta status;
    private final Instant criadaEm;

    private Conta(UUID id, String titular, Cpf cpf, BigDecimal saldo, StatusConta status, Instant criadaEm) {
        this.id = Objects.requireNonNull(id);
        this.titular = titular;
        this.cpf = Objects.requireNonNull(cpf);
        this.saldo = Objects.requireNonNull(saldo);
        this.status = Objects.requireNonNull(status);
        this.criadaEm = Objects.requireNonNull(criadaEm);
    }

    /** Regra de negocio: toda conta nasce ativa e com saldo zero. */
    public static Conta abrir(String titular, Cpf cpf, Instant agora) {
        if (titular == null || titular.isBlank()) {
            throw new RegraDeNegocioException("O titular da conta e obrigatorio");
        }
        return new Conta(UUID.randomUUID(), titular.trim(), cpf, BigDecimal.ZERO, StatusConta.ATIVA, agora);
    }

    /** Recria uma conta que ja existe (por exemplo, vinda do banco de dados). Nao aplica regras de abertura. */
    public static Conta reconstituir(UUID id, String titular, Cpf cpf, BigDecimal saldo,
                                     StatusConta status, Instant criadaEm) {
        return new Conta(id, titular, cpf, saldo, status, criadaEm);
    }

    public UUID getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public StatusConta getStatus() {
        return status;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
