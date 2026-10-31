package br.facens.gamificacao.recomendacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.gamificacao.adapter.web.AlunoController;
import br.facens.gamificacao.gamificacao.adapter.web.dto.AlunoResponse;
import br.facens.gamificacao.gamificacao.adapter.web.dto.ConcluirCursoRequest;
import br.facens.gamificacao.gamificacao.adapter.web.dto.MatricularRequest;
import br.facens.gamificacao.recomendacao.application.AlunoSemCadastroException;
import br.facens.gamificacao.recomendacao.application.AssistenteIndisponivelException;
import br.facens.gamificacao.recomendacao.application.Recomendacoes;
import br.facens.gamificacao.recomendacao.web.RecomendacaoController;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

/**
 * RPC do Desafio 3: "indisponibilidade da IA nao pode bloquear o fluxo essencial".
 * O Core sobe apontando para uma porta onde nao ha ia-service.
 */
@SpringBootTest(properties = "ia.url=http://localhost:9")
class IaForaDoArIntegracaoTest {

    @Autowired
    private AlunoController alunos;

    @Autowired
    private RecomendacaoController ia;

    @Test
    @DisplayName("CT-REC-05 - com a IA fora: conclusao de curso funciona e recomendacao degrada")
    void fluxoEssencialSegue() {
        Long id = alunos.matricular(new MatricularRequest("ia-fora@teste.com")).getBody().id();

        AlunoResponse concluido = alunos.concluirCurso(id, new ConcluirCursoRequest("Java", new BigDecimal("9.0")))
                .getBody();
        Recomendacoes rec = ia.recomendar(id);

        assertEquals(1, concluido.cursosConquistados());
        assertTrue(rec.degradado());
        assertEquals(Recomendacoes.ORIGEM_FALLBACK, rec.origem());
    }

    @Test
    @DisplayName("CT-REC-06 - erros do modulo viram 404 e 503 com Retry-After")
    void mapeamentoHttp() {
        assertEquals(HttpStatus.NOT_FOUND, ia.alunoInexistente(new AlunoSemCadastroException(1L)).getStatusCode());
        var resposta = ia.assistenteIndisponivel(new AssistenteIndisponivelException(null));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resposta.getStatusCode());
        assertEquals("30", resposta.getHeaders().getFirst("Retry-After"));
        assertThrows(AssistenteIndisponivelException.class,
                () -> ia.perguntar(new RecomendacaoController.PerguntaRequest("Como viro Premium?")));
    }
}
