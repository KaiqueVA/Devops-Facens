package br.facens.gamificacao.analytics;

import br.facens.gamificacao.eventos.api.AssinanteDeEvento;
import br.facens.gamificacao.eventos.api.Entrega;
import br.facens.gamificacao.iot.api.EventoDeLaboratorioRecebido;
import org.springframework.stereotype.Component;

/** Leva os eventos dos laboratorios (RF06) para o registro de aprendizagem. Idempotente. */
@Component
public class ColetorDeLaboratorio implements AssinanteDeEvento<EventoDeLaboratorioRecebido> {

    public static final String NOME = "analytics.laboratorio";

    private final RegistroDeLaboratorioRepository repositorio;

    public ColetorDeLaboratorio(RegistroDeLaboratorioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public Class<EventoDeLaboratorioRecebido> tipoDeEvento() {
        return EventoDeLaboratorioRecebido.class;
    }

    @Override
    public void receber(EventoDeLaboratorioRecebido e, Entrega entrega) {
        if (!repositorio.existsByEventoId(e.eventoId())) {
            repositorio.save(new RegistroDeLaboratorio(e.eventoId(), e.alunoId(), e.laboratorio(), e.tipo().name(),
                    e.ocorridoEm()));
        }
    }
}
