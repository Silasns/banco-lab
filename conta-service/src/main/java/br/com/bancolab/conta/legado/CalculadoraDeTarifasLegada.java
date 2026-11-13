package br.com.bancolab.conta.legado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * CODIGO LEGADO, RUIM DE PROPOSITO. Nao use como exemplo.
 *
 * Esta classe funciona (os testes passam), mas viola varios principios do
 * SOLID. Refatora-la, mantendo os testes verdes, e o Desafio 3.
 */
public class CalculadoraDeTarifasLegada {

    public static List<String> historico = new ArrayList<>();

    public String processar(String tipoConta, String operacao, BigDecimal valor, boolean clienteVip) {
        BigDecimal tarifa = BigDecimal.ZERO;

        if (tipoConta.equals("CORRENTE")) {
            if (operacao.equals("TED")) {
                tarifa = new BigDecimal("8.50");
            } else if (operacao.equals("SAQUE")) {
                tarifa = new BigDecimal("2.50");
            } else if (operacao.equals("PIX")) {
                tarifa = BigDecimal.ZERO;
            }
        } else if (tipoConta.equals("POUPANCA")) {
            if (operacao.equals("TED")) {
                tarifa = new BigDecimal("5.00");
            } else if (operacao.equals("SAQUE")) {
                tarifa = BigDecimal.ZERO;
            } else if (operacao.equals("PIX")) {
                tarifa = BigDecimal.ZERO;
            }
        } else if (tipoConta.equals("SALARIO")) {
            if (operacao.equals("TED")) {
                throw new UnsupportedOperationException("Conta salario nao faz TED");
            } else if (operacao.equals("SAQUE")) {
                tarifa = BigDecimal.ZERO;
            } else if (operacao.equals("PIX")) {
                // 1% do valor, limitado a R$ 10,00
                tarifa = valor.multiply(new BigDecimal("0.01"));
                if (tarifa.compareTo(new BigDecimal("10.00")) > 0) {
                    tarifa = new BigDecimal("10.00");
                }
            }
        }

        if (clienteVip) {
            tarifa = tarifa.multiply(new BigDecimal("0.5"));
        }

        tarifa = tarifa.setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal total = valor.add(tarifa).setScale(2, RoundingMode.HALF_EVEN);

        String linha = tipoConta + ";" + operacao + ";" + valor.setScale(2, RoundingMode.HALF_EVEN)
                + ";" + tarifa + ";" + total;

        historico.add(linha);
        System.out.println("[TARIFA] " + linha);

        return linha;
    }
}
