package br.facens.gamificacao.notificacao.application.port;

/** Porta de saida para o provedor de notificacoes. Lanca excecao se nao conseguir entregar. */
public interface CanalDeNotificacao {

    void enviar(Notificacao notificacao);
}
