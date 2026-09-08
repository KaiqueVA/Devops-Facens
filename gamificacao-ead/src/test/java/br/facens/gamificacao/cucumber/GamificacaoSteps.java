package br.facens.gamificacao.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.domain.PlanoAssinatura;
import br.facens.gamificacao.aluno.repository.AlunoRepository;
import br.facens.gamificacao.aluno.service.GamificacaoService;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;

public class GamificacaoSteps {

    @Autowired
    private GamificacaoService service;

    @Autowired
    private AlunoRepository repository;

    private Aluno aluno;
    private GamificacaoException erro;

    @Before
    public void limparBase() {
        repository.deleteAll();
        aluno = null;
        erro = null;
    }

    @Dado("um aluno {string} no plano {string} com {int} cursos disponiveis")
    public void umAlunoNoPlanoComCursos(String email, String plano, int cursos) {
        aluno = service.matricular(email);
        assertEquals(PlanoAssinatura.valueOf(plano), aluno.getPlano());
        assertEquals(cursos, aluno.getCursosDisponiveis());
    }

    @Dado("o aluno ja conquistou {int} cursos com media acima de 7,0")
    public void oAlunoJaConquistou(int quantidade) {
        for (int i = 1; i <= quantidade; i++) {
            aluno = service.concluirCurso(aluno.getId(), "Curso " + i, new BigDecimal("8.0"));
        }
        assertEquals(quantidade, aluno.getCursosConquistados());
    }

    @Dado("um aluno {string} ja promovido ao plano {string}")
    public void umAlunoJaPromovido(String email, String plano) {
        aluno = service.matricular(email);
        for (int i = 1; i <= Aluno.CURSOS_PARA_PREMIUM; i++) {
            aluno = service.concluirCurso(aluno.getId(), "Curso " + i, new BigDecimal("8.0"));
        }
        assertEquals(PlanoAssinatura.valueOf(plano), aluno.getPlano());
    }

    @Dado("o aluno registrou {int} topicos e {int} comentarios no forum")
    public void oAlunoRegistrouParticipacao(int topicos, int comentarios) {
        aluno = service.participarDoForum(aluno.getId(), topicos, comentarios);
    }

    @Dado("existe o aluno {string} com {int} topicos e {int} comentarios no forum")
    public void existeOutroAluno(String email, int topicos, int comentarios) {
        Aluno outro = service.matricular(email);
        service.participarDoForum(outro.getId(), topicos, comentarios);
    }

    @Quando("o aluno conclui o curso {string} com media {double}")
    public void oAlunoConcluiOCurso(String curso, double media) {
        aluno = service.concluirCurso(aluno.getId(), curso, BigDecimal.valueOf(media));
    }

    @Quando("o aluno tenta concluir o curso {string} com media {double}")
    public void oAlunoTentaConcluirOCurso(String curso, double media) {
        try {
            aluno = service.concluirCurso(aluno.getId(), curso, BigDecimal.valueOf(media));
        } catch (GamificacaoException e) {
            erro = e;
        }
    }

    @Quando("o aluno converte {int} moedas em {string}")
    public void oAlunoConverteMoedas(int quantidade, String destino) {
        aluno = service.converterMoedas(aluno.getId(), quantidade, DestinoMoeda.valueOf(destino));
    }

    @Quando("a plataforma premia o destaque do forum")
    public void aPlataformaPremiaODestaque() {
        service.premiarDestaqueDoForum();
        aluno = service.buscar(aluno.getId());
    }

    @Entao("o aluno deve ter {int} cursos disponiveis")
    public void oAlunoDeveTerCursosDisponiveis(int esperado) {
        assertEquals(esperado, service.buscar(aluno.getId()).getCursosDisponiveis());
    }

    @Entao("o aluno deve ter {int} curso conquistado")
    public void oAlunoDeveTerCursoConquistado(int esperado) {
        assertEquals(esperado, aluno.getCursosConquistados());
    }

    @Entao("a operacao deve ser recusada com a mensagem {string}")
    public void aOperacaoDeveSerRecusada(String mensagem) {
        assertNotNull(erro);
        assertEquals(mensagem, erro.getMessage());
    }

    @Entao("o plano do aluno deve ser {string}")
    public void oPlanoDoAlunoDeveSer(String plano) {
        assertEquals(PlanoAssinatura.valueOf(plano), aluno.getPlano());
    }

    @Entao("o aluno deve possuir voucher para projetos reais")
    public void oAlunoDevePossuirVoucher() {
        assertTrue(aluno.isVoucherProjetoReal());
    }

    @Entao("o saldo de moedas do aluno deve ser {int}")
    public void oSaldoDeMoedasDeveSer(int esperado) {
        assertEquals(esperado, aluno.getMoedas());
    }

    @Entao("a participacao do aluno no forum deve estar zerada")
    public void aParticipacaoDeveEstarZerada() {
        assertEquals(0, aluno.getTopicosNoForum());
        assertEquals(0, aluno.getComentariosNoForum());
    }
}
