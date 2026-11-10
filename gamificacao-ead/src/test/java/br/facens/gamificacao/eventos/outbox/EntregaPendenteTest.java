package br.facens.gamificacao.eventos.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntregaPendenteTest {

    private static final Instant T0 = Instant.parse("2026-10-31T12:00:00Z");
    private static final Duration BASE = Duration.ofSeconds(1);
    private static final Duration TETO = Duration.ofSeconds(5);

    @Test
    @DisplayName("EVT-U1 - backoff exponencial 1s, 2s, 4s, limitado ao teto, e DLQ ao esgotar")
    void backoffETeto() {
        EntregaPendente e = new EntregaPendente("ev-1", "a", "T", "{}", T0);

        e.registrarFalha("x", T0, 5, BASE, TETO);
        assertEquals(T0.plusSeconds(1), e.getProximaTentativaEm());
        e.registrarFalha("x", T0, 5, BASE, TETO);
        assertEquals(T0.plusSeconds(2), e.getProximaTentativaEm());
        e.registrarFalha("x", T0, 5, BASE, TETO);
        assertEquals(T0.plusSeconds(4), e.getProximaTentativaEm());
        e.registrarFalha(null, T0, 5, BASE, TETO);
        assertEquals(T0.plusSeconds(5), e.getProximaTentativaEm());
        assertEquals("erro sem mensagem", e.getUltimoErro());
        assertEquals(StatusEntrega.PENDENTE, e.getStatus());

        e.registrarFalha("y".repeat(900), T0, 5, BASE, TETO);
        assertEquals(StatusEntrega.FALHOU_DEFINITIVO, e.getStatus());
        assertEquals(500, e.getUltimoErro().length());

        e.reabrir(T0);
        assertEquals(StatusEntrega.PENDENTE, e.getStatus());
        assertEquals(0, e.getTentativas());
        e.marcarEntregue(T0);
        assertEquals(StatusEntrega.ENTREGUE, e.getStatus());
        assertEquals(T0, e.getEntregueEm());
        assertNull(e.getUltimoErro());
        assertEquals("T", e.getTipoEvento());
        assertEquals(T0, e.getCriadoEm());
    }
}
