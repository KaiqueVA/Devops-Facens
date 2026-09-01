package br.facens.horascomplementares.domain.exception;

public class JustificativaInsuficienteException extends NegocioException {

    public JustificativaInsuficienteException(int minimo) {
        super("A justificativa da reprovacao deve ter no minimo " + minimo + " caracteres.");
    }
}
