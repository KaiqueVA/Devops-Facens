package br.facens.gamificacao.iot.application;

public class EventoIotInvalidoException extends RuntimeException {

    public EventoIotInvalidoException(String mensagem) {
        super(mensagem);
    }
}
