package br.facens.gamificacao.notificacao.application.port;

/** Mensagem pronta para o provedor. {@code chaveIdempotencia} evita aviso duplicado em reentrega. */
public record Notificacao(String chaveIdempotencia, String destinatario, String titulo, String mensagem) {
}
