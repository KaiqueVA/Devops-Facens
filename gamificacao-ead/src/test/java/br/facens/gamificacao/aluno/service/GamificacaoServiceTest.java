package br.facens.gamificacao.aluno.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.domain.PlanoAssinatura;
import br.facens.gamificacao.aluno.repository.AlunoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GamificacaoServiceTest {

    @Mock
    private AlunoRepository repository;

    private GamificacaoService service;

    @BeforeEach
    void setUp() {
        service = new GamificacaoService(repository);
        Mockito.lenient()
                .when(repository.save(any(Aluno.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Aluno comForum(String email, int topicos, int comentarios) {
        Aluno aluno = new Aluno(email);
        aluno.participarDoForum(topicos, comentarios);
        return aluno;
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
        when(repository.findById(1L)).thenReturn(Optional.of(new Aluno("maria@teste.com")));

        Aluno aluno = service.concluirCurso(1L, "Java Basico", new BigDecimal("8.5"));

        assertEquals(7, aluno.getCursosDisponiveis());
        assertEquals(1, aluno.getCursosConquistados());
    }

    @Test
    @DisplayName("CT-SRV-03 - deve registrar participacao no forum")
    void deveRegistrarParticipacao() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Aluno("maria@teste.com")));

        Aluno aluno = service.participarDoForum(1L, 3, 4);

        assertEquals(3, aluno.getTopicosNoForum());
        assertEquals(4, aluno.getComentariosNoForum());
    }

    @Test
    @DisplayName("CT-SRV-04 - deve converter moedas de aluno Premium")
    void deveConverterMoedas() {
        when(repository.findById(1L)).thenReturn(Optional.of(premium()));

        Aluno aluno = service.converterMoedas(1L, 3, DestinoMoeda.CRIPTOMOEDA);

        assertEquals(0, aluno.getMoedas());
        assertEquals(3, aluno.getMoedasEmCripto());
    }

    @Test
    @DisplayName("CT-SRV-05 - premio vai para quem tem mais topicos, desempatando por comentarios")
    void devePremiarODestaqueDoForum() {
        Aluno poucosTopicos = comForum("um@teste.com", 4, 20);
        Aluno empatadoFraco = comForum("dois@teste.com", 10, 1);
        Aluno destaque = comForum("tres@teste.com", 10, 9);
        when(repository.findAll()).thenReturn(List.of(poucosTopicos, empatadoFraco, destaque));

        Aluno premiado = service.premiarDestaqueDoForum();

        assertEquals("tres@teste.com", premiado.getEmail());
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA + 1, premiado.getCursosDisponiveis());
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA, empatadoFraco.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-SRV-06 - quem nao ajudou outros participantes nao concorre ao premio")
    void naoDevePremiarQuemNaoComentou() {
        when(repository.findAll()).thenReturn(List.of(comForum("solo@teste.com", 30, 0)));

        assertEquals("Nenhum aluno elegivel ao premio do forum",
                assertThrows(GamificacaoException.class,
                        () -> service.premiarDestaqueDoForum()).getMessage());
    }

    @Test
    @DisplayName("CT-SRV-07 - deve buscar aluno existente")
    void deveBuscarAluno() {
        Aluno aluno = new Aluno("maria@teste.com");
        when(repository.findById(1L)).thenReturn(Optional.of(aluno));

        assertEquals(aluno, service.buscar(1L));
    }

    @Test
    @DisplayName("CT-SRV-08 - deve falhar ao buscar aluno inexistente")
    void deveFalharAoBuscarAlunoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertEquals("Aluno nao encontrado: 99",
                assertThrows(GamificacaoException.class, () -> service.buscar(99L)).getMessage());
    }
}
