package br.facens.gamificacao.iot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.analytics.AnalyticsController;
import br.facens.gamificacao.eventos.outbox.EntregaPendenteRepository;
import br.facens.gamificacao.eventos.outbox.RelayDoOutbox;
import br.facens.gamificacao.gamificacao.application.GamificacaoService;
import br.facens.gamificacao.iot.adapter.EventoIotController;
import br.facens.gamificacao.iot.api.TipoEventoLaboratorio;
import br.facens.gamificacao.iot.application.ComandoEventoIot;
import br.facens.gamificacao.iot.application.EventoIotRepository;
import br.facens.gamificacao.iot.application.RecebimentoDeEventoIot;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** RF06 / ADR-005: o Core recebe o contrato do gateway de forma idempotente e encaminha ao ecossistema. */
@SpringBootTest
class EventoIotIntegracaoTest {

    @Autowired
    private EventoIotController controller;

    @Autowired
    private RecebimentoDeEventoIot recebimento;

    @Autowired
    private EventoIotRepository eventosIot;

    @Autowired
    private EntregaPendenteRepository outbox;

    @Autowired
    private RelayDoOutbox relay;

    @Autowired
    private AnalyticsController analytics;

    @Autowired
    private GamificacaoService gamificacao;

    @Autowired
    private br.facens.gamificacao.analytics.ColetorDeLaboratorio coletor;

    private Long aluno;

    @BeforeEach
    void preparar() {
        aluno = gamificacao.matricular("iot-" + System.nanoTime() + "@teste.com").getId();
    }

    private ComandoEventoIot evento(String id, TipoEventoLaboratorio tipo, Long alunoId, Instant quando) {
        return new ComandoEventoIot(id, "LAB-ARQ", "bancada-03", tipo, alunoId, "ARQ-LAB-1", quando);
    }

    @Test
    @DisplayName("IOT-01 - evento novo: 201, gravado e encaminhado ao ecossistema pelo outbox")
    void eventoNovo() {
        String id = UUID.randomUUID().toString();
        relay.processarPendentes(Instant.now()); // esvazia o que outros testes deixaram
        long antesOutbox = outbox.count();
        Object antesLab = analytics.resumo().get("eventosDeLaboratorio");

        ResponseEntity<Map<String, String>> r = controller.receber(
                evento(id, TipoEventoLaboratorio.FIM_ATIVIDADE, aluno, Instant.now()));

        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        assertEquals("NOVO", r.getBody().get("resultado"));
        assertTrue(eventosIot.existsByEventoId(id));
        assertEquals(antesOutbox + 1, outbox.count());
        relay.processarPendentes(Instant.now());
        assertEquals((Long) antesLab + 1, analytics.resumo().get("eventosDeLaboratorio"));

        // reentrega do mesmo evento ao assinante de analytics nao duplica o registro
        coletor.receber(new br.facens.gamificacao.iot.api.EventoDeLaboratorioRecebido(id, "LAB-ARQ", "bancada-03",
                TipoEventoLaboratorio.FIM_ATIVIDADE, aluno, "ARQ-LAB-1", Instant.now()),
                new br.facens.gamificacao.eventos.api.Entrega(id, 2));
        assertEquals((Long) antesLab + 1, analytics.resumo().get("eventosDeLaboratorio"));
    }

    @Test
    @DisplayName("IOT-02 - reentrega (QoS 1) do mesmo eventoId: 200 DUPLICADO, sem efeito colateral")
    void eventoDuplicado() {
        String id = UUID.randomUUID().toString();
        controller.receber(evento(id, TipoEventoLaboratorio.PRESENCA, aluno, Instant.now()));
        long outboxDepoisDoPrimeiro = outbox.count();
        long duplicadosAntes = recebimento.getDuplicados();

        ResponseEntity<Map<String, String>> r = controller.receber(
                evento(id, TipoEventoLaboratorio.PRESENCA, aluno, Instant.now()));
        controller.receber(evento(id, TipoEventoLaboratorio.PRESENCA, aluno, Instant.now()));

        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals("DUPLICADO", r.getBody().get("resultado"));
        assertEquals(1, eventosIot.countByAlunoId(aluno));
        assertEquals(outboxDepoisDoPrimeiro, outbox.count());
        assertEquals(duplicadosAntes + 2, recebimento.getDuplicados());
    }

    @Test
    @DisplayName("IOT-03 - evento atrasado (guardado no dispositivo por horas) e aceito")
    void eventoAtrasado() {
        String id = UUID.randomUUID().toString();
        assertEquals(HttpStatus.CREATED, controller.receber(
                evento(id, TipoEventoLaboratorio.INICIO_ATIVIDADE, aluno, Instant.now().minusSeconds(6 * 3600)))
                .getStatusCode());
    }

    @Test
    @DisplayName("IOT-04 - invalidos viram 422 (o gateway manda para a DLQ): sem id, sem campos, futuro, aluno inexistente")
    void invalidos() {
        Instant agora = Instant.now();
        long antes = recebimento.getInvalidos();
        ComandoEventoIot[] ruins = {
            evento(" ", TipoEventoLaboratorio.PRESENCA, aluno, agora),
            evento(null, TipoEventoLaboratorio.PRESENCA, aluno, agora),
            evento("x1", null, aluno, agora),
            evento("x2", TipoEventoLaboratorio.PRESENCA, null, agora),
            evento("x3", TipoEventoLaboratorio.PRESENCA, aluno, null),
            new ComandoEventoIot("x4", "LAB", null, TipoEventoLaboratorio.PRESENCA, aluno, null, agora),
            new ComandoEventoIot("x5", null, "d", TipoEventoLaboratorio.PRESENCA, aluno, null, agora),
            evento("x6", TipoEventoLaboratorio.PRESENCA, aluno, agora.plusSeconds(3600)),
            evento("x7", TipoEventoLaboratorio.PRESENCA, 999_999L, agora)};

        for (ComandoEventoIot ruim : ruins) {
            try {
                controller.receber(ruim);
            } catch (br.facens.gamificacao.iot.application.EventoIotInvalidoException e) {
                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, controller.invalido(e).getStatusCode());
            }
        }

        assertEquals(antes + ruins.length, recebimento.getInvalidos());
        assertEquals(antes + ruins.length, controller.resumo().get("invalidos"));
    }
}
