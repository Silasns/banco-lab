package br.com.bancolab.conta.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.bancolab.conta.domain.model.Conta;

/** Formato do JSON de saida. O dominio nunca e serializado direto. */
public record ContaResponse(UUID id, String titular, String cpf, BigDecimal saldo, String status, Instant criadaEm) {

    static ContaResponse de(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getTitular(),
                conta.getCpf().numero(),
                conta.getSaldo(),
                conta.getStatus().name(),
                conta.getCriadaEm());
    }
}
