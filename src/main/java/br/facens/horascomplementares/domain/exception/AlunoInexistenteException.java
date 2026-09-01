package br.facens.horascomplementares.domain.exception;

public class AlunoInexistenteException extends NegocioException {

    public AlunoInexistenteException(Long id) {
        super("Aluno com id " + id + " nao encontrado.");
    }
}
