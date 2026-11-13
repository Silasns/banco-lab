package br.com.bancolab.conta.domain.exception;

public class CpfInvalidoException extends RegraDeNegocioException {

    public CpfInvalidoException(String valorInformado) {
        super("CPF invalido: " + valorInformado);
    }
}
