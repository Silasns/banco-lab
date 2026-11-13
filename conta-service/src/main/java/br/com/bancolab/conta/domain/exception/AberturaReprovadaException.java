package br.com.bancolab.conta.domain.exception;

public class AberturaReprovadaException extends RegraDeNegocioException {

    public AberturaReprovadaException() {
        super("Abertura de conta reprovada pela analise de antifraude");
    }
}
