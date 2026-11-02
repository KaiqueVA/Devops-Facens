package br.facens.gamificacao.iot.adapter;

import br.facens.gamificacao.iot.application.RecebimentoDeEventoIot;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Evidencia do ADR-005: eventos de laboratorio aceitos no Core por resultado.
 * Duplicados > 0 e normal (QoS 1); o que importa e novos == publicados.
 * Exportado como iot_eventos_recebidos_total{resultado}.
 */
@Component
public class MetricasIot implements MeterBinder {

    private final RecebimentoDeEventoIot recebimento;

    public MetricasIot(RecebimentoDeEventoIot recebimento) {
        this.recebimento = recebimento;
    }

    @Override
    public void bindTo(MeterRegistry registro) {
        registrar(registro, "novo", RecebimentoDeEventoIot::getNovos);
        registrar(registro, "duplicado", RecebimentoDeEventoIot::getDuplicados);
        registrar(registro, "invalido", RecebimentoDeEventoIot::getInvalidos);
    }

    private void registrar(MeterRegistry registro, String resultado,
                           java.util.function.ToDoubleFunction<RecebimentoDeEventoIot> contador) {
        FunctionCounter.builder("iot.eventos.recebidos", recebimento, contador)
                .description("Eventos de laboratorio recebidos do iot-gateway (ADR-005)")
                .tag("resultado", resultado)
                .register(registro);
    }
}
