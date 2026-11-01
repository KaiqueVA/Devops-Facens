package br.facens.gamificacao.iot.application;

public enum ResultadoRecebimento {
    /** Primeira vez que o Core ve este eventoId. */
    NOVO,
    /** Reentrega (QoS 1 = pelo menos uma vez): aceito sem efeito. */
    DUPLICADO
}
