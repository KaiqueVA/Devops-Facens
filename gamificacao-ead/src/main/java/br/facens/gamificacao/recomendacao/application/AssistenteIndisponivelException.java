package br.facens.gamificacao.recomendacao.application;

public class AssistenteIndisponivelException extends RuntimeException {

    public AssistenteIndisponivelException(Throwable causa) {
        super("Assistente educacional indisponivel no momento. Tente novamente em instantes.", causa);
    }
}
