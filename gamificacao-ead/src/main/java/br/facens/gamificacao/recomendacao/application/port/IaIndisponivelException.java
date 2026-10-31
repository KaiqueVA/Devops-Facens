package br.facens.gamificacao.recomendacao.application.port;

/** A IA nao respondeu a tempo, recusou a conexao ou devolveu erro. */
public class IaIndisponivelException extends RuntimeException {

    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
