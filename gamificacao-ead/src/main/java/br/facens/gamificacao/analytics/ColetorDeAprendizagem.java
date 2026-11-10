package br.facens.gamificacao.analytics;

import br.facens.gamificacao.eventos.api.AssinanteDeEvento;
import br.facens.gamificacao.eventos.api.Entrega;
import br.facens.gamificacao.gamificacao.api.CursoConcluido;
import org.springframework.stereotype.Component;

/** Assinante idempotente: o mesmo evento entregue duas vezes gera um unico registro. */
@Component
public class ColetorDeAprendizagem implements AssinanteDeEvento<CursoConcluido> {

    public static final String NOME = "analytics.aprendizagem";

    private final RegistroDeAprendizagemRepository repositorio;

    public ColetorDeAprendizagem(RegistroDeAprendizagemRepository repositorio) {
        this.repositorio = repositorio;
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
    public void receber(CursoConcluido e, Entrega entrega) {
        if (repositorio.existsByEventoId(entrega.eventoId())) {
            return;
        }
        repositorio.save(new RegistroDeAprendizagem(entrega.eventoId(), e.alunoId(), e.curso(), e.media(),
                e.conquistado(), e.promovidoAPremium(), e.ocorridoEm()));
    }
}
