package br.com.bancolab.conta.domain.model;

import br.com.bancolab.conta.domain.exception.CpfInvalidoException;

/**
 * Value object: um CPF valido, guardado somente com digitos.
 *
 * Se existe um objeto Cpf, ele e valido. Isso evita espalhar "if (cpfValido)"
 * pelo sistema inteiro.
 */
public record Cpf(String numero) {

    public Cpf {
        String digitos = numero == null ? "" : numero.replaceAll("\\D", "");
        if (!digitosValidos(digitos)) {
            throw new CpfInvalidoException(numero);
        }
        numero = digitos;
    }

    private static boolean digitosValidos(String digitos) {
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    @Override
    public String toString() {
        return numero;
    }
}
