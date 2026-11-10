package br.facens.gamificacao.eventos.outbox;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Evidencia do ADR-004: tamanho da fila do outbox por status e idade da entrega
 * pendente mais antiga (quanto tempo um consumidor esta "atrasado").
 * Exportado como outbox_entregas{status} e outbox_pendente_mais_antiga_seconds.
 */
@Component
public class MetricasDoOutbox implements MeterBinder {

    private final EntregaPendenteRepository repositorio;
    private final Clock relogio;

    public MetricasDoOutbox(EntregaPendenteRepository repositorio, Clock relogioDosEventos) {
        this.repositorio = repositorio;
        this.relogio = relogioDosEventos;
    }

    @Override
    public void bindTo(MeterRegistry registro) {
        for (StatusEntrega status : new StatusEntrega[] {StatusEntrega.PENDENTE, StatusEntrega.FALHOU_DEFINITIVO}) {
            Gauge.builder("outbox.entregas", repositorio, r -> r.countByStatus(status))
                    .description("Entregas no outbox por status (ADR-004)")
                    .tag("status", status.name())
                    .register(registro);
        }
        Gauge.builder("outbox.pendente.mais.antiga", this, MetricasDoOutbox::idadeDaMaisAntigaEmSegundos)
                .description("Idade da entrega pendente mais antiga (ADR-004)")
                .baseUnit("seconds")
                .register(registro);
    }

    double idadeDaMaisAntigaEmSegundos() {
        Instant maisAntiga = repositorio.maisAntiga(StatusEntrega.PENDENTE);
        return maisAntiga == null ? 0 : Duration.between(maisAntiga, Instant.now(relogio)).toMillis() / 1000.0;
    }
}
