package br.facens.gamificacao.eventos.api;

/**
 * Contrato para reagir a um evento de forma assincrona e confiavel.
 * Basta um modulo declarar um bean que implemente esta interface.
 *
 * <p>A entrega e "pelo menos uma vez": o assinante deve ser idempotente usando
 * {@link Entrega#eventoId()}.</p>
 */
public interface AssinanteDeEvento<T> {

    /** Nome estavel do assinante (vira coluna no outbox). Ex.: "notificacao.conclusao". */
    String nome();

    Class<T> tipoDeEvento();

    /** Lancar qualquer excecao = falha; a entrega sera tentada de novo com backoff. */
    void receber(T evento, Entrega entrega) throws Exception;
}
