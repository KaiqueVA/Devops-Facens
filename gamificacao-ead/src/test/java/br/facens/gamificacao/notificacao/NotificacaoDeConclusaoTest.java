package br.facens.gamificacao.notificacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.eventos.api.Entrega;
import br.facens.gamificacao.gamificacao.api.CursoConcluido;
import br.facens.gamificacao.notificacao.application.NotificacaoDeConclusao;
import br.facens.gamificacao.notificacao.application.port.Notificacao;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificacaoDeConclusaoTest {

    private final List<Notificacao> enviadas = new ArrayList<>();
    private final NotificacaoDeConclusao assinante = new NotificacaoDeConclusao(enviadas::add);

    private CursoConcluido evento(boolean conquistado, boolean premium) {
        return new CursoConcluido(1L, "ana@facens.br", "Java", new BigDecimal("8.5"), conquistado,
                conquistado ? 3 : 0, premium, premium ? "PREMIUM" : "BASICO", 12, Instant.now());
    }

    @Test
    @DisplayName("NOT-01 - mensagem muda conforme o resultado: conquista, Premium ou so conclusao")
    void mensagens() {
        assinante.receber(evento(true, false), new Entrega("e1", 1));
        assinante.receber(evento(true, true), new Entrega("e2", 1));
        assinante.receber(evento(false, false), new Entrega("e3", 1));

        assertEquals("Curso conquistado: Java", enviadas.get(0).titulo());
        assertTrue(enviadas.get(0).mensagem().contains("3 cursos"));
        assertEquals("Voce agora e Premium!", enviadas.get(1).titulo());
        assertEquals("Curso concluido: Java", enviadas.get(2).titulo());
        assertEquals("e1", enviadas.get(0).chaveIdempotencia());
        assertEquals("ana@facens.br", enviadas.get(0).destinatario());
        assertEquals(NotificacaoDeConclusao.NOME, assinante.nome());
        assertEquals(CursoConcluido.class, assinante.tipoDeEvento());
    }
}
