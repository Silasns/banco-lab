package br.com.bancolab.conta.domain.exception;

/**
 * Raiz de todas as excecoes de negocio. Quem esta do lado de fora do hexagono
 * (por exemplo o adaptador web) decide como traduzir isso: aqui dentro ninguem
 * sabe o que e um status HTTP.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
