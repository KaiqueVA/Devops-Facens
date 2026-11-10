package br.facens.gamificacao.recomendacao.adapter;

import br.facens.gamificacao.recomendacao.application.RecomendacaoService;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Evidencia do ADR-003 em producao: quantas recomendacoes vieram da IA e quantas
 * cairam no catalogo padrao. A razao entre as duas e o gatilho do circuit breaker.
 * Exportado como ia_recomendacoes_total{origem} e ia_assistente_indisponivel_total.
 */
@Component
public class MetricasDaIa implements MeterBinder {

    private final RecomendacaoService service;

    public MetricasDaIa(RecomendacaoService service) {
        this.service = service;
    }

    @Override
    public void bindTo(MeterRegistry registro) {
        FunctionCounter.builder("ia.recomendacoes", service, RecomendacaoService::getRecomendacoesPelaIa)
                .description("Recomendacoes entregues, por origem (ADR-003)")
                .tag("origem", "ia")
                .register(registro);
        FunctionCounter.builder("ia.recomendacoes", service, RecomendacaoService::getRecomendacoesDegradadas)
                .description("Recomendacoes entregues, por origem (ADR-003)")
                .tag("origem", "catalogo_padrao")
                .register(registro);
        FunctionCounter.builder("ia.assistente.indisponivel", service, RecomendacaoService::getAssistenteIndisponivel)
                .description("Perguntas ao assistente respondidas com 503 (ADR-003)")
                .register(registro);
    }
}
