package br.facens.gamificacao.observabilidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.facens.gamificacao.eventos.outbox.EntregaPendenteRepository;
import br.facens.gamificacao.eventos.outbox.MetricasDoOutbox;
import br.facens.gamificacao.eventos.outbox.StatusEntrega;
import br.facens.gamificacao.gamificacao.api.ResumoDoAluno;
import br.facens.gamificacao.iot.adapter.MetricasIot;
import br.facens.gamificacao.iot.api.TipoEventoLaboratorio;
import br.facens.gamificacao.iot.application.ComandoEventoIot;
import br.facens.gamificacao.iot.application.EventoIotRepository;
import br.facens.gamificacao.iot.application.RecebimentoDeEventoIot;
import br.facens.gamificacao.recomendacao.adapter.MetricasDaIa;
import br.facens.gamificacao.recomendacao.application.RecomendacaoService;
import br.facens.gamificacao.recomendacao.application.port.IaIndisponivelException;
import br.facens.gamificacao.recomendacao.application.port.MotorDeRecomendacao;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ADR-006: as metricas que comprovam decisoes anteriores existem, tem os nomes que
 * os alertas do Prometheus usam e acompanham o comportamento do sistema.
 */
class MetricasArquiteturaisTest {

    private final MeterRegistry registro = new SimpleMeterRegistry();

    @Test
    @DisplayName("OBS-01 - ia.recomendacoes{origem} separa IA de catalogo padrao (ADR-003)")
    void metricasDaIa() {
        AtomicBoolean iaNoAr = new AtomicBoolean(true);
        MotorDeRecomendacao motor = (a, q) -> {
            if (!iaNoAr.get()) {
                throw new IaIndisponivelException("fora", null);
            }
            return new MotorDeRecomendacao.ResultadoDoModelo("v1", List.of());
        };
        RecomendacaoService service = new RecomendacaoService(
                id -> Optional.of(new ResumoDoAluno(id, "BASICO", 0, 0, 0)), motor, p -> {
                    throw new IaIndisponivelException("fora", null);
                });
        new MetricasDaIa(service).bindTo(registro);

        service.recomendar(1L);
        iaNoAr.set(false);
        service.recomendar(1L);
        service.recomendar(1L);
        try {
            service.perguntar("oi?");
        } catch (RuntimeException esperado) {
            // 503
        }

        assertEquals(1, registro.get("ia.recomendacoes").tag("origem", "ia").functionCounter().count());
        assertEquals(2, registro.get("ia.recomendacoes").tag("origem", "catalogo_padrao").functionCounter().count());
        assertEquals(1, registro.get("ia.assistente.indisponivel").functionCounter().count());
    }

    @Test
    @DisplayName("OBS-02 - outbox.entregas{status} e idade da pendente mais antiga (ADR-004)")
    void metricasDoOutbox() {
        Instant agora = Instant.parse("2026-10-31T12:00:00Z");
        EntregaPendenteRepository repo = mock(EntregaPendenteRepository.class);
        when(repo.countByStatus(StatusEntrega.PENDENTE)).thenReturn(3L);
        when(repo.countByStatus(StatusEntrega.FALHOU_DEFINITIVO)).thenReturn(1L);
        when(repo.maisAntiga(StatusEntrega.PENDENTE)).thenReturn(agora.minusSeconds(42), (Instant) null);
        new MetricasDoOutbox(repo, Clock.fixed(agora, ZoneOffset.UTC)).bindTo(registro);

        assertEquals(3, registro.get("outbox.entregas").tag("status", "PENDENTE").gauge().value());
        assertEquals(1, registro.get("outbox.entregas").tag("status", "FALHOU_DEFINITIVO").gauge().value());
        assertEquals(42, registro.get("outbox.pendente.mais.antiga").gauge().value());
        assertEquals(0, registro.get("outbox.pendente.mais.antiga").gauge().value(), "sem pendentes = 0");
    }

    @Test
    @DisplayName("OBS-03 - iot.eventos.recebidos{resultado} conta novos e duplicados (ADR-005)")
    void metricasIot() {
        EventoIotRepository repo = mock(EventoIotRepository.class);
        when(repo.existsByEventoId(anyString())).thenReturn(false, true);
        RecebimentoDeEventoIot recebimento = new RecebimentoDeEventoIot(repo,
                id -> Optional.of(new ResumoDoAluno(id, "BASICO", 0, 0, 0)), evento -> { });
        new MetricasIot(recebimento).bindTo(registro);
        ComandoEventoIot c = new ComandoEventoIot("e1", "LAB", "b1", TipoEventoLaboratorio.PRESENCA, 1L, null,
                Instant.now());

        recebimento.receber(c);
        recebimento.receber(c);

        assertEquals(1, registro.get("iot.eventos.recebidos").tag("resultado", "novo").functionCounter().count());
        assertEquals(1, registro.get("iot.eventos.recebidos").tag("resultado", "duplicado").functionCounter().count());
        assertTrue(registro.get("iot.eventos.recebidos").tag("resultado", "invalido").functionCounter().count() == 0);
    }
}
