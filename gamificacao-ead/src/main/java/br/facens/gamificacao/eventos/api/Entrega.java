package br.facens.gamificacao.eventos.api;

/** Metadados de uma entrega: id do evento (chave de idempotencia) e numero da tentativa. */
public record Entrega(String eventoId, int tentativa) {
}
