package br.com.bancolab.conta.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.bancolab.conta.domain.exception.CpfJaCadastradoException;
import br.com.bancolab.conta.domain.exception.RegraDeNegocioException;

/**
 * Traduz excecoes do hexagono para respostas HTTP no formato RFC 9457
 * (application/problem+json). E aqui, e so aqui, que excecao vira status code.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CpfJaCadastradoException.class)
    ProblemDetail conflito(CpfJaCadastradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    ProblemDetail regraDeNegocio(RegraDeNegocioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }
}
