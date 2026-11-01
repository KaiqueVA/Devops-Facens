package br.facens.gamificacao.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.analytics.AnalyticsController;
import br.facens.gamificacao.analytics.ColetorDeAprendizagem;
import br.facens.gamificacao.analytics.RegistroDeAprendizagemRepository;
import br.facens.gamificacao.eventos.api.Entrega;
import br.facens.gamificacao.eventos.outbox.EntregaPendente;
import br.facens.gamificacao.eventos.outbox.EntregaPendenteRepository;
import br.facens.gamificacao.eventos.outbox.OutboxController;
import br.facens.gamificacao.eventos.outbox.RelayDoOutbox;
import br.facens.gamificacao.eventos.outbox.StatusEntrega;
import br.facens.gamificacao.gamificacao.api.CursoConcluido;
import br.facens.gamificacao.gamificacao.application.GamificacaoService;
import br.facens.gamificacao.gamificacao.domain.GamificacaoException;
import br.facens.gamificacao.notificacao.application.NotificacaoDeConclusao;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * ADR-004 ponta a ponta no Core: conclusao sincrona + evento no outbox na mesma
 * transacao; notificacao e analytics entregues depois pelo relay. O relay agendado
 * fica desligado e e acionado pelo teste, com o "agora" controlado.
 */
@SpringBootTest(properties = {"eventos.relay.habilitado=false", "eventos.relay.max-tentativas=3"})
class OutboxIntegracaoTest {

    static final ProvedorFalso PROVEDOR = new ProvedorFalso();

    @DynamicPropertySource
    static void provedor(DynamicPropertyRegistry registro) {
        registro.add("notificacao.url", PROVEDOR::url);
    }

    @Autowired
    private GamificacaoService gamificacao;

    @Autowired
    private EntregaPendenteRepository outbox;

    @Autowired
    private RelayDoOutbox relay;

    @Autowired
    private RegistroDeAprendizagemRepository registros;

    @Autowired
    private ColetorDeAprendizagem coletor;

    @Autowired
    private OutboxController outboxController;

    @Autowired
    private AnalyticsController analytics;

    private Long aluno;

    @BeforeEach
    void limpar() {
        outbox.deleteAll();
        registros.deleteAll();
        PROVEDOR.chavesRecebidas().clear();
        PROVEDOR.ligar();
        aluno = gamificacao.matricular("evento-" + System.nanoTime() + "@teste.com").getId();
    }

    private EntregaPendente entrega(String assinante) {
        return outbox.findAll().stream().filter(e -> e.getAssinante().equals(assinante)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("EVT-01 - concluir curso grava 1 entrega por assinante, mesmo eventoId, na mesma transacao")
    void conclusaoGravaOutbox() {
        gamificacao.concluirCurso(aluno, "Java", new BigDecimal("9.0"));

        List<EntregaPendente> entregas = outbox.findAll();
        assertEquals(2, entregas.size());
        assertEquals(1, entregas.stream().map(EntregaPendente::getEventoId).distinct().count());
        assertTrue(entregas.stream().allMatch(e -> e.getStatus() == StatusEntrega.PENDENTE));
        assertTrue(entregas.get(0).getPayload().contains("\"curso\":\"Java\""));
    }

    @Test
    @DisplayName("EVT-02 - regra violada faz rollback: nenhum evento fica orfao no outbox")
    void rollbackNaoDeixaEvento() {
        assertThrows(GamificacaoException.class,
                () -> gamificacao.concluirCurso(aluno, "Java", new BigDecimal("11")));

        assertEquals(0, outbox.count());
    }

    @Test
    @DisplayName("EVT-03 - provedor fora: analytics entregue, notificacao fica PENDENTE com backoff")
    void consumidorForaNaoAfetaOutros() {
        PROVEDOR.desligar();
        gamificacao.concluirCurso(aluno, "Java", new BigDecimal("9.0"));

        int entregues = relay.processarPendentes(Instant.now());

        assertEquals(1, entregues);
        assertEquals(StatusEntrega.ENTREGUE, entrega(ColetorDeAprendizagem.NOME).getStatus());
        EntregaPendente notificacao = entrega(NotificacaoDeConclusao.NOME);
        assertEquals(StatusEntrega.PENDENTE, notificacao.getStatus());
        assertEquals(1, notificacao.getTentativas());
        assertTrue(notificacao.getProximaTentativaEm().isAfter(Instant.now()));
        assertTrue(notificacao.getUltimoErro().contains("503"));
        assertEquals(1L, analytics.resumo().get("conclusoes"));
    }

    @Test
    @DisplayName("EVT-04 - provedor volta: a notificacao retida e entregue uma vez, com Idempotency-Key")
    void recuperacaoAposVolta() {
        PROVEDOR.desligar();
        gamificacao.concluirCurso(aluno, "Java", new BigDecimal("9.0"));
        relay.processarPendentes(Instant.now());

        PROVEDOR.ligar();
        relay.processarPendentes(Instant.now().plusSeconds(120));

        EntregaPendente notificacao = entrega(NotificacaoDeConclusao.NOME);
        assertEquals(StatusEntrega.ENTREGUE, notificacao.getStatus());
        assertEquals(2, notificacao.getTentativas());
        assertEquals(List.of(notificacao.getEventoId()), PROVEDOR.chavesRecebidas());
    }

    @Test
    @DisplayName("EVT-05 - esgotou tentativas: FALHOU_DEFINITIVO (DLQ) e reprocessamento manual")
    void dlqEReprocessamento() {
        PROVEDOR.desligar();
        gamificacao.concluirCurso(aluno, "Java", new BigDecimal("9.0"));
        Instant t = Instant.now();
        for (int i = 0; i < 3; i++) {
            relay.processarPendentes(t);
            t = t.plusSeconds(3600);
        }
        assertEquals(StatusEntrega.FALHOU_DEFINITIVO, entrega(NotificacaoDeConclusao.NOME).getStatus());
        Map<String, Object> resumo = outboxController.resumo();
        assertTrue(resumo.toString().contains("FALHOU_DEFINITIVO"));

        PROVEDOR.ligar();
        assertEquals(Map.of("reabertas", 1), outboxController.reprocessar());
        relay.processarPendentes(Instant.now().plusSeconds(1));

        assertEquals(StatusEntrega.ENTREGUE, entrega(NotificacaoDeConclusao.NOME).getStatus());
    }

    @Test
    @DisplayName("EVT-06 - reentrega do mesmo evento nao duplica o registro de aprendizagem")
    void assinanteIdempotente() {
        CursoConcluido evento = new CursoConcluido(aluno, "x@teste.com", "Java", new BigDecimal("9.0"), true, 3,
                false, "BASICO", 1, Instant.now());

        coletor.receber(evento, new Entrega("ev-repetido", 1));
        coletor.receber(evento, new Entrega("ev-repetido", 2));

        assertEquals(1, registros.count());
        assertEquals(9.0, analytics.resumo().get("mediaGeral"));
    }

    @Test
    @DisplayName("EVT-07 - assinante desconhecido no outbox vira falha registrada, sem derrubar o relay")
    void assinanteDesconhecido() throws Exception {
        gamificacao.concluirCurso(aluno, "Java", new BigDecimal("9.0"));
        // simula uma linha antiga de um assinante que foi removido do codigo
        EntregaPendente orfa = entrega(ColetorDeAprendizagem.NOME);
        org.springframework.test.util.ReflectionTestUtils.setField(orfa, "assinante", "assinante.removido");
        outbox.save(orfa);

        relay.processarPendentes(Instant.now());

        EntregaPendente depois = outbox.findById(orfa.getId()).orElseThrow();
        assertEquals(StatusEntrega.PENDENTE, depois.getStatus());
        assertTrue(depois.getUltimoErro().contains("Assinante nao registrado"));
        assertEquals(0L, analytics.resumo().get("conclusoes"));
        assertEquals(0.0, analytics.resumo().get("mediaGeral"));
    }

    @Test
    @DisplayName("EVT-09 - o agendador so dispara o relay quando habilitado")
    void agendador() {
        PROVEDOR.ligar();
        gamificacao.concluirCurso(aluno, "Java", new BigDecimal("9.0"));

        relay.agendado();
        assertEquals(2, outbox.countByStatus(StatusEntrega.PENDENTE), "desabilitado: nada acontece");

        org.springframework.test.util.ReflectionTestUtils.setField(relay, "habilitado", true);
        try {
            relay.agendado();
        } finally {
            org.springframework.test.util.ReflectionTestUtils.setField(relay, "habilitado", false);
        }
        assertEquals(2, outbox.countByStatus(StatusEntrega.ENTREGUE));
    }
}
