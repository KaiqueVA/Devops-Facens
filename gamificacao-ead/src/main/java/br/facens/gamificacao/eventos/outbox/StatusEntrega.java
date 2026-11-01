package br.facens.gamificacao.eventos.outbox;

public enum StatusEntrega {
    PENDENTE,
    ENTREGUE,
    /** Esgotou as tentativas: fica parado ate alguem reprocessar (equivalente a DLQ). */
    FALHOU_DEFINITIVO
}
