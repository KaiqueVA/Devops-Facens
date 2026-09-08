package br.facens.gamificacao.aluno.domain;

/** Excecao unica de regra de negocio do dominio de gamificacao. */
public class GamificacaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GamificacaoException(String mensagem) {
        super(mensagem);
    }
}
