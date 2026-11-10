/**
 * Modulo Eventos (ADR-004): entrega assincrona e confiavel de eventos entre modulos
 * e para consumidores externos, usando o padrao Transactional Outbox no proprio
 * banco relacional (sem broker).
 *
 * <p>Cada evento publicado vira uma linha por assinante em {@code outbox_entrega},
 * gravada na MESMA transacao da operacao de negocio. Um relay agendado entrega,
 * com retentativa e backoff; apos o limite, a entrega vai para FALHOU_DEFINITIVO
 * (equivalente a uma DLQ) e pode ser reprocessada.</p>
 */
package br.facens.gamificacao.eventos;
