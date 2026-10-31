package br.facens.gamificacao.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.facens.gamificacao.gamificacao.application.GamificacaoService;
import br.facens.gamificacao.ranking.application.PosicaoRanking;
import br.facens.gamificacao.ranking.web.RankingController;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Integracao do modulo Ranking com o banco real (H2): os dados entram pelo modulo
 * Gamificacao e saem pelo Ranking, sem que um modulo importe classes internas do outro.
 */
@SpringBootTest
class RankingIntegracaoTest {

    @Autowired
    private GamificacaoService gamificacao;

    @Autowired
    private RankingController ranking;

    @Autowired
    private JdbcClient jdbc;

    @Test
    @DisplayName("CT-RNK-04 - ranking ordena por cursos conquistados, depois moedas, depois id")
    void ordenaPorConquistasEMoedas() {
        jdbc.sql("DELETE FROM aluno").update();
        Long ana = gamificacao.matricular("ana@teste.com").getId();
        Long bia = gamificacao.matricular("bia@teste.com").getId();
        Long caio = gamificacao.matricular("caio@teste.com").getId();

        gamificacao.concluirCurso(bia, "Java", new BigDecimal("9.0"));
        gamificacao.concluirCurso(bia, "Spring", new BigDecimal("8.0"));
        gamificacao.concluirCurso(caio, "Java", new BigDecimal("9.5"));
        gamificacao.concluirCurso(ana, "Java", new BigDecimal("6.0")); // concluiu mas nao conquistou

        List<PosicaoRanking> top = ranking.consultar(0, 10);

        assertEquals(List.of("bia@teste.com", "caio@teste.com", "ana@teste.com"),
                top.stream().map(PosicaoRanking::email).toList());
        assertEquals(2, top.get(0).cursosConquistados());
        assertEquals(1, top.get(0).posicao());
    }
}
