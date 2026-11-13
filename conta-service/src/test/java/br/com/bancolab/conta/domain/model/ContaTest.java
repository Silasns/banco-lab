package br.com.bancolab.conta.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.bancolab.conta.domain.exception.RegraDeNegocioException;

/** Teste de dominio: Java puro, roda em milissegundos, sem Spring e sem banco. */
class ContaTest {

    private static final Cpf CPF = new Cpf("52998224725");
    private static final Instant AGORA = Instant.parse("2026-01-10T12:00:00Z");

    @Test
    void contaNasceAtivaComSaldoZero() {
        Conta conta = Conta.abrir("  Maria Silva ", CPF, AGORA);

        assertThat(conta.getId()).isNotNull();
        assertThat(conta.getTitular()).isEqualTo("Maria Silva");
        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(conta.getStatus()).isEqualTo(StatusConta.ATIVA);
        assertThat(conta.getCriadaEm()).isEqualTo(AGORA);
    }

    @Test
    void naoAbreContaSemTitular() {
        assertThatThrownBy(() -> Conta.abrir(" ", CPF, AGORA))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
