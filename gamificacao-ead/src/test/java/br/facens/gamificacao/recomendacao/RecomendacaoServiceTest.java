package br.facens.gamificacao.recomendacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.gamificacao.api.ResumoDoAluno;
import br.facens.gamificacao.recomendacao.application.AlunoSemCadastroException;
import br.facens.gamificacao.recomendacao.application.AssistenteIndisponivelException;
import br.facens.gamificacao.recomendacao.application.RecomendacaoService;
import br.facens.gamificacao.recomendacao.application.Recomendacoes;
import br.facens.gamificacao.recomendacao.application.port.AssistenteEducacional;
import br.facens.gamificacao.recomendacao.application.port.CursoRecomendado;
import br.facens.gamificacao.recomendacao.application.port.IaIndisponivelException;
import br.facens.gamificacao.recomendacao.application.port.MotorDeRecomendacao;
import br.facens.gamificacao.recomendacao.web.RecomendacaoController;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecomendacaoServiceTest {

    private static final ResumoDoAluno ALUNO = new ResumoDoAluno(7L, "BASICO", 3, 2, 0);

    private final MotorDeRecomendacao motorOk = (a, q) -> new MotorDeRecomendacao.ResultadoDoModelo("v1",
            List.of(new CursoRecomendado("SPRING-201", "Spring Boot", 1.0, "nivel 2")));
    private final MotorDeRecomendacao motorFora = (a, q) -> {
        throw new IaIndisponivelException("connection refused", null);
    };
    private final AssistenteEducacional assistenteOk = p -> new AssistenteEducacional.Resposta("ok", List.of("x.md"));
    private final AssistenteEducacional assistenteFora = p -> {
        throw new IaIndisponivelException("timeout", null);
    };

    private RecomendacaoService service(MotorDeRecomendacao motor, AssistenteEducacional assistente) {
        return new RecomendacaoService(id -> id.equals(7L) ? Optional.of(ALUNO) : Optional.empty(), motor, assistente);
    }

    @Test
    @DisplayName("CT-REC-01 - IA no ar: recomendacao personalizada, nao degradada")
    void iaNoAr() {
        RecomendacaoService s = service(motorOk, assistenteOk);

        Recomendacoes r = s.recomendar(7L);

        assertEquals(Recomendacoes.ORIGEM_IA, r.origem());
        assertFalse(r.degradado());
        assertEquals(1, s.getRecomendacoesPelaIa());
    }

    @Test
    @DisplayName("CT-REC-02 - IA fora: catalogo padrao com degradado=true (sem erro para o aluno)")
    void iaForaDegrada() {
        RecomendacaoService s = service(motorFora, assistenteOk);

        Recomendacoes r = s.recomendar(7L);

        assertEquals(Recomendacoes.ORIGEM_FALLBACK, r.origem());
        assertTrue(r.degradado());
        assertEquals(3, r.cursos().size());
        assertEquals(1, s.getRecomendacoesDegradadas());
    }

    @Test
    @DisplayName("CT-REC-03 - aluno inexistente nao chega a chamar a IA")
    void alunoInexistente() {
        assertThrows(AlunoSemCadastroException.class, () -> service(motorOk, assistenteOk).recomendar(99L));
    }

    @Test
    @DisplayName("CT-REC-04 - assistente no ar responde; fora, vira AssistenteIndisponivel (503)")
    void assistente() {
        assertEquals("ok", service(motorOk, assistenteOk).perguntar("p?").resposta());

        RecomendacaoService fora = service(motorOk, assistenteFora);
        assertThrows(AssistenteIndisponivelException.class, () -> fora.perguntar("p?"));
        assertEquals(1, fora.getAssistenteIndisponivel());
    }

    @Test
    @DisplayName("CT-REC-07 - controller repassa a resposta do assistente quando a IA esta no ar")
    void controllerAssistenteNoAr() {
        RecomendacaoController controller = new RecomendacaoController(service(motorOk, assistenteOk));

        assertEquals("ok", controller.perguntar(new RecomendacaoController.PerguntaRequest("p?")).resposta());
        assertEquals(Recomendacoes.ORIGEM_IA, controller.recomendar(7L).origem());
    }
}
