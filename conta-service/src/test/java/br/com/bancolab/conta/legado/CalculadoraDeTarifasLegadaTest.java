package br.com.bancolab.conta.legado;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Testes de caracterizacao: descrevem o comportamento ATUAL do legado.
 * No Desafio 3 eles sao sua rede de seguranca: as mesmas regras precisam
 * continuar valendo no codigo novo.
 */
class CalculadoraDeTarifasLegadaTest {

    private final CalculadoraDeTarifasLegada calculadora = new CalculadoraDeTarifasLegada();

    @ParameterizedTest(name = "{0} {1} de {2} (vip={3}) -> {4}")
    @CsvSource({
            "CORRENTE, TED,   100.00,  false, CORRENTE;TED;100.00;8.50;108.50",
            "CORRENTE, TED,   100.00,  true,  CORRENTE;TED;100.00;4.25;104.25",
            "CORRENTE, SAQUE, 50.00,   false, CORRENTE;SAQUE;50.00;2.50;52.50",
            "CORRENTE, PIX,   100.00,  false, CORRENTE;PIX;100.00;0.00;100.00",
            "POUPANCA, TED,   100.00,  false, POUPANCA;TED;100.00;5.00;105.00",
            "POUPANCA, SAQUE, 50.00,   false, POUPANCA;SAQUE;50.00;0.00;50.00",
            "SALARIO,  PIX,   200.00,  false, SALARIO;PIX;200.00;2.00;202.00",
            "SALARIO,  PIX,   5000.00, false, SALARIO;PIX;5000.00;10.00;5010.00",
            "SALARIO,  PIX,   5000.00, true,  SALARIO;PIX;5000.00;5.00;5005.00",
    })
    void calculaTarifa(String tipo, String operacao, BigDecimal valor, boolean vip, String esperado) {
        assertThat(calculadora.processar(tipo, operacao, valor, vip)).isEqualTo(esperado);
    }

    @Test
    void contaSalarioNaoFazTed() {
        assertThatThrownBy(() -> calculadora.processar("SALARIO", "TED", new BigDecimal("100.00"), false))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
