/**
 * Modulo IoT (RF06): porta de entrada dos eventos de laboratorio no Core (ADR-005).
 *
 * <p>O Core NAO fala MQTT e nao conhece o formato de cada dispositivo: recebe um
 * contrato unico do iot-gateway por HTTP, de forma idempotente (eventoId), e
 * encaminha o fato ao ecossistema pelo outbox (evento EventoDeLaboratorioRecebido).</p>
 */
package br.facens.gamificacao.iot;
