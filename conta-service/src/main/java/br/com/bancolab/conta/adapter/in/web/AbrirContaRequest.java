package br.com.bancolab.conta.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formato do JSON de entrada. Validacao aqui e so de FORMATO (campo vazio,
 * tamanho). Regra de negocio (CPF valido, CPF duplicado) fica dentro do hexagono.
 */
public record AbrirContaRequest(
        @NotBlank @Size(max = 120) String titular,
        @NotBlank String cpf) {
}
