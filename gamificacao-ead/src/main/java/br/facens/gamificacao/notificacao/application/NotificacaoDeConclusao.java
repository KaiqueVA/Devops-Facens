package br.facens.gamificacao.notificacao.application;

import br.facens.gamificacao.eventos.api.AssinanteDeEvento;
import br.facens.gamificacao.eventos.api.Entrega;
import br.facens.gamificacao.gamificacao.api.CursoConcluido;
import br.facens.gamificacao.notificacao.application.port.CanalDeNotificacao;
import br.facens.gamificacao.notificacao.application.port.Notificacao;
import org.springframework.stereotype.Component;

/** RF05: transforma CursoConcluido em uma notificacao para o aluno. */
@Component
public class NotificacaoDeConclusao implements AssinanteDeEvento<CursoConcluido> {

    public static final String NOME = "notificacao.conclusao";

    private final CanalDeNotificacao canal;

    public NotificacaoDeConclusao(CanalDeNotificacao canal) {
        this.canal = canal;
    }

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public Class<CursoConcluido> tipoDeEvento() {
        return CursoConcluido.class;
    }

    @Override
    public void receber(CursoConcluido evento, Entrega entrega) {
        canal.enviar(montar(evento, entrega.eventoId()));
    }

    static Notificacao montar(CursoConcluido e, String eventoId) {
        String titulo;
        String mensagem;
        if (e.promovidoAPremium()) {
            titulo = "Voce agora e Premium!";
            mensagem = "Parabens! Com " + e.cursosConquistados() + " cursos conquistados voce ganhou o plano Premium, "
                    + "um voucher para projetos reais e moedas.";
        } else if (e.conquistado()) {
            titulo = "Curso conquistado: " + e.curso();
            mensagem = "Media " + e.media() + ". Voce liberou mais " + e.cursosLiberados() + " cursos.";
        } else {
            titulo = "Curso concluido: " + e.curso();
            mensagem = "Media " + e.media() + ". Conclua com media acima de 7,0 para liberar novos cursos.";
        }
        return new Notificacao(eventoId, e.email(), titulo, mensagem);
    }
}
