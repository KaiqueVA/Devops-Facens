package br.facens.gamificacao.recomendacao.application;

public class AlunoSemCadastroException extends RuntimeException {

    public AlunoSemCadastroException(Long id) {
        super("Aluno nao encontrado: " + id);
    }
}
