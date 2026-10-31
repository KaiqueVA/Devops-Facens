package br.facens.gamificacao.gamificacao.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.facens.gamificacao.gamificacao.adapter.memoria.AlunoRepositorioEmMemoria;
import br.facens.gamificacao.gamificacao.api.CursoConcluido;
import br.facens.gamificacao.gamificacao.domain.Aluno;
import br.facens.gamificacao.gamificacao.domain.DestinoMoeda;
import br.facens.gamificacao.gamificacao.domain.GamificacaoException;
import br.facens.gamificacao.gamificacao.domain.PlanoAssinatura;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Casos de uso testados com o adapter em memoria (ADR-002): sem Spring, sem banco
 * e sem Mockito. Na AC1 este teste precisava mockar o JpaRepository.
 */
class GamificacaoServiceTest {

    private AlunoRepositorioEmMemoria repositorio;
    private GamificacaoService service;
    private final List<Object> eventosPublicados = new ArrayList<>();

    @BeforeEach
    void setUp() {
        repositorio = new AlunoRepositorioEmMemoria();
        service = new GamificacaoService(repositorio, eventosPublicados::add);
    }

    private Long salvo(Aluno aluno) {
        return repositorio.salvar(aluno).getId();
    }

    private Aluno comForum(String email, int topicos, int comentarios) {
        Aluno aluno = new Aluno(email);
        aluno.participarDoForum(topicos, comentarios);
        return repositorio.salvar(aluno);
    }

    private Aluno premium() {
        Aluno aluno = new Aluno("maria@teste.com");
        for (int i = 1; i <= Aluno.CURSOS_PARA_PREMIUM; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("8.0"));
        }
        return aluno;
    }

    @Test
    @DisplayName("CT-SRV-01 - deve matricular aluno no plano Basico")
    void deveMatricular() {
        Aluno aluno = service.matricular("maria@teste.com");

        assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
        assertEquals("maria@teste.com", aluno.getEmail());
    }

    @Test
    @DisplayName("CT-SRV-02 - deve concluir curso e liberar o bonus")
    void deveConcluirCurso() {
        Long id = salvo(new Aluno("maria@teste.com"));

        Aluno aluno = service.concluirCurso(id, "Java Basico", new BigDecimal("8.5"));

        assertEquals(7, aluno.getCursosDisponiveis());
        assertEquals(1, aluno.getCursosConquistados());
    }

    @Test
    @DisplayName("CT-SRV-03 - deve registrar participacao no forum")
    void deveRegistrarParticipacao() {
        Long id = salvo(new Aluno("maria@teste.com"));

        Aluno aluno = service.participarDoForum(id, 3, 4);

        assertEquals(3, aluno.getTopicosNoForum());
        assertEquals(4, aluno.getComentariosNoForum());
    }

    @Test
    @DisplayName("CT-SRV-04 - deve converter moedas de aluno Premium")
    void deveConverterMoedas() {
        Long id = salvo(premium());

        Aluno aluno = service.converterMoedas(id, 3, DestinoMoeda.CRIPTOMOEDA);

        assertEquals(0, aluno.getMoedas());
        assertEquals(3, aluno.getMoedasEmCripto());
    }

    @Test
    @DisplayName("CT-SRV-05 - premio vai para quem tem mais topicos, desempatando por comentarios")
    void devePremiarODestaqueDoForum() {
        comForum("um@teste.com", 4, 20);
        Aluno empatadoFraco = comForum("dois@teste.com", 10, 1);
        comForum("tres@teste.com", 10, 9);

        Aluno premiado = service.premiarDestaqueDoForum();

        assertEquals("tres@teste.com", premiado.getEmail());
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA + 1, premiado.getCursosDisponiveis());
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA,
                service.buscar(empatadoFraco.getId()).getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-SRV-06 - quem nao ajudou outros participantes nao concorre ao premio")
    void naoDevePremiarQuemNaoComentou() {
        comForum("solo@teste.com", 30, 0);

        assertEquals("Nenhum aluno elegivel ao premio do forum",
                assertThrows(GamificacaoException.class,
                        () -> service.premiarDestaqueDoForum()).getMessage());
    }

    @Test
    @DisplayName("CT-SRV-07 - deve buscar aluno existente")
    void deveBuscarAluno() {
        Long id = salvo(new Aluno("maria@teste.com"));

        assertEquals("maria@teste.com", service.buscar(id).getEmail());
    }

    @Test
    @DisplayName("CT-SRV-08 - deve falhar ao buscar aluno inexistente")
    void deveFalharAoBuscarAlunoInexistente() {
        assertEquals("Aluno nao encontrado: 99",
                assertThrows(GamificacaoException.class, () -> service.buscar(99L)).getMessage());
    }

    @Test
    @DisplayName("CT-SRV-09 - concluir curso publica CursoConcluido com o resultado da regra (ADR-004)")
    void concluirPublicaEvento() {
        Aluno quaseLa = new Aluno("quase@teste.com");
        for (int i = 1; i < Aluno.CURSOS_PARA_PREMIUM; i++) {
            quaseLa.concluirCurso("Curso " + i, new BigDecimal("9.0"));
        }
        Long id = salvo(quaseLa);

        service.concluirCurso(id, "Curso 12", new BigDecimal("9.5"));

        assertEquals(1, eventosPublicados.size());
        CursoConcluido evento = (CursoConcluido) eventosPublicados.get(0);
        assertEquals(id, evento.alunoId());
        assertEquals("quase@teste.com", evento.email());
        assertEquals(true, evento.conquistado());
        assertEquals(true, evento.promovidoAPremium());
        assertEquals("PREMIUM", evento.plano());
        assertEquals(Aluno.CURSOS_LIBERADOS_POR_APROVACAO, evento.cursosLiberados());
    }

    @Test
    @DisplayName("CT-SRV-10 - regra violada nao publica evento")
    void regraVioladaNaoPublica() {
        Long id = salvo(new Aluno("maria@teste.com"));

        assertThrows(GamificacaoException.class, () -> service.concluirCurso(id, "Java", new BigDecimal("11")));

        assertEquals(0, eventosPublicados.size());
    }
}
