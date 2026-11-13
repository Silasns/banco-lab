package br.com.bancolab.conta.domain.exception;

public class CpfJaCadastradoException extends RegraDeNegocioException {

    public CpfJaCadastradoException() {
        super("Ja existe uma conta para o CPF informado");
    }
}
