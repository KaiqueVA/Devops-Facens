package br.facens.gamificacao.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.facens.gamificacao.eventos.outbox.EntregaPendenteRepository;
import br.facens.gamificacao.gamificacao.application.GamificacaoService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Alternativa REJEITADA no ADR-004, medida: notificacao sincrona dentro da conclusao.
 * Com o provedor fora, a conclusao do curso falha e e desfeita.
 */
@SpringBootTest(properties = {"eventos.entrega=sincrona", "eventos.relay.habilitado=false"})
class EntregaSincronaTest {

    static final ProvedorFalso PROVEDOR = new ProvedorFalso();

    @DynamicPropertySource
    static void provedor(DynamicPropertyRegistry registro) {
        registro.add("notificacao.url", PROVEDOR::url);
    }

    @Autowired
    private GamificacaoService gamificacao;

    @Autowired
    private EntregaPendenteRepository outbox;

    @Test
    @DisplayName("EVT-08 - modo sincrono: provedor no ar entrega na hora; provedor fora DERRUBA a conclusao")
    void sincronoAcoplaAoConsumidor() {
        Long id = gamificacao.matricular("sincrono-" + System.nanoTime() + "@teste.com").getId();
        long outboxAntes = outbox.count();

        PROVEDOR.ligar();
        gamificacao.concluirCurso(id, "Java", new BigDecimal("9.0"));
        assertEquals(1, PROVEDOR.chavesRecebidas().size());

        PROVEDOR.desligar();
        assertThrows(RuntimeException.class, () -> gamificacao.concluirCurso(id, "Spring", new BigDecimal("9.0")));

        assertEquals(1, gamificacao.buscar(id).getCursosConcluidos(), "a segunda conclusao foi desfeita");
        assertEquals(outboxAntes, outbox.count(), "no modo sincrono nada passa pelo outbox");
    }
}
