/**
 * Modulo Notificacao (RF05): reage ao evento CursoConcluido e avisa o aluno por um
 * provedor externo (e-mail/push) via webhook HTTP. Roda de forma assincrona pelo
 * outbox: se o provedor cair, a conclusao do curso nao e afetada (ADR-004).
 */
package br.facens.gamificacao.notificacao;
