package br.facens.gamificacao.aluno.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlunoTest {

    private static final BigDecimal APROVADO = new BigDecimal("8.5");

    private Aluno alunoPremium() {
        Aluno aluno = new Aluno("maria@teste.com");
        for (int i = 1; i <= Aluno.CURSOS_PARA_PREMIUM; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("8.0"));
        }
        return aluno;
    }

    @Test
    @DisplayName("CT-01 - deve liberar 3 cursos ao concluir com media acima de 7,0")
    void deveLiberarTresCursosAoConcluirComMediaAcimaDeSete() {
        Aluno aluno = new Aluno("maria@teste.com");
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA, aluno.getCursosDisponiveis());

        aluno.concluirCurso("Java Basico", APROVADO);

        assertEquals(7, aluno.getCursosDisponiveis());
        assertEquals(1, aluno.getCursosConquistados());
        assertEquals(1, aluno.getCursosConcluidos());
        assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
        assertEquals("maria@teste.com", aluno.getEmail());
        assertFalse(aluno.isVoucherProjetoReal());
    }

    @Test
    @DisplayName("CT-02 - media exatamente 7,0 nao libera cursos extras")
    void mediaExatamenteSeteNaoLiberaCursos() {
        Aluno aluno = new Aluno("maria@teste.com");

        aluno.concluirCurso("Scrum", new BigDecimal("7.0"));

        assertEquals(4, aluno.getCursosDisponiveis());
        assertEquals(0, aluno.getCursosConquistados());
        assertEquals(1, aluno.getCursosConcluidos());
    }

    @Test
    @DisplayName("CT-03 - nome do curso e obrigatorio")
    void nomeDoCursoEObrigatorio() {
        Aluno aluno = new Aluno("maria@teste.com");

        assertEquals("Nome do curso e obrigatorio",
                assertThrows(GamificacaoException.class,
                        () -> aluno.concluirCurso(null, APROVADO)).getMessage());
        assertEquals("Nome do curso e obrigatorio",
                assertThrows(GamificacaoException.class,
                        () -> aluno.concluirCurso("   ", APROVADO)).getMessage());
    }

    @Test
    @DisplayName("CT-04 - media deve estar entre 0,0 e 10,0")
    void mediaForaDaFaixaERecusada() {
        Aluno aluno = new Aluno("maria@teste.com");
        String esperada = "Media deve estar entre 0,0 e 10,0";

        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.concluirCurso("Java", null)).getMessage());
        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.concluirCurso("Java", new BigDecimal("-1.0"))).getMessage());
        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.concluirCurso("Java", new BigDecimal("11.0"))).getMessage());

        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA, aluno.getCursosDisponiveis());
        assertEquals(0, aluno.getCursosConcluidos());
    }

    @Test
    @DisplayName("CT-05 - nao deve concluir curso sem saldo na assinatura")
    void naoDeveConcluirSemSaldo() {
        Aluno aluno = new Aluno("maria@teste.com");
        for (int i = 1; i <= Aluno.CURSOS_DA_ASSINATURA_BASICA; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("6.0"));
        }
        assertEquals(0, aluno.getCursosDisponiveis());

        assertEquals("Aluno nao possui curso disponivel na assinatura",
                assertThrows(GamificacaoException.class,
                        () -> aluno.concluirCurso("Curso 6", APROVADO)).getMessage());
    }

    @Test
    @DisplayName("CT-06 - e-mail do aluno e obrigatorio")
    void emailEObrigatorio() {
        assertEquals("E-mail do aluno e obrigatorio",
                assertThrows(GamificacaoException.class, () -> new Aluno(null)).getMessage());
        assertEquals("E-mail do aluno e obrigatorio",
                assertThrows(GamificacaoException.class, () -> new Aluno(" ")).getMessage());
    }

    @Test
    @DisplayName("CT-07 - ao conquistar 12 cursos vira Premium com voucher e 3 moedas")
    void deveVirarPremiumAoConquistarDozeCursos() {
        Aluno aluno = new Aluno("maria@teste.com");
        for (int i = 1; i <= 11; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("8.0"));
        }
        assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
        assertEquals(0, aluno.getMoedas());

        aluno.concluirCurso("Curso 12", new BigDecimal("9.0"));

        assertEquals(PlanoAssinatura.PREMIUM, aluno.getPlano());
        assertTrue(aluno.isVoucherProjetoReal());
        assertEquals(Aluno.MOEDAS_DA_PROMOCAO, aluno.getMoedas());
        assertEquals(12, aluno.getCursosConquistados());
        assertEquals(29, aluno.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-08 - aluno ja Premium nao recebe a promocao duas vezes")
    void naoDevePromoverDuasVezes() {
        Aluno aluno = alunoPremium();

        aluno.concluirCurso("Curso 13", new BigDecimal("10.0"));

        assertEquals(Aluno.MOEDAS_DA_PROMOCAO, aluno.getMoedas());
        assertEquals(13, aluno.getCursosConquistados());
        assertEquals(PlanoAssinatura.PREMIUM, aluno.getPlano());
    }

    @Test
    @DisplayName("CT-09 - aluno Basico nao possui moedas para converter")
    void alunoBasicoNaoConverteMoedas() {
        Aluno aluno = new Aluno("maria@teste.com");

        assertEquals("Apenas aluno Premium possui moedas para converter",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(1, DestinoMoeda.CONHECIMENTO)).getMessage());
    }

    @Test
    @DisplayName("CT-10 - destino da moeda e obrigatorio")
    void destinoDaMoedaEObrigatorio() {
        Aluno aluno = alunoPremium();

        assertEquals("Destino da moeda e obrigatorio",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(1, null)).getMessage());
    }

    @Test
    @DisplayName("CT-11 - quantidade de moedas deve ser maior que zero")
    void quantidadeDeMoedasDeveSerPositiva() {
        Aluno aluno = alunoPremium();

        assertEquals("Quantidade de moedas deve ser maior que zero",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(0, DestinoMoeda.ACUMULO)).getMessage());
    }

    @Test
    @DisplayName("CT-12 - nao deve converter mais moedas do que possui")
    void naoDeveConverterAlemDoSaldo() {
        Aluno aluno = alunoPremium();

        assertEquals("Saldo de moedas insuficiente",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(4, DestinoMoeda.CONHECIMENTO)).getMessage());
    }

    @Test
    @DisplayName("CT-13 - deve converter moedas em conhecimento (novos cursos)")
    void deveConverterMoedasEmConhecimento() {
        Aluno aluno = alunoPremium();
        int cursosAntes = aluno.getCursosDisponiveis();

        aluno.converterMoedas(3, DestinoMoeda.CONHECIMENTO);

        assertEquals(0, aluno.getMoedas());
        assertEquals(cursosAntes + 3, aluno.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-14 - deve converter moedas em criptomoeda")
    void deveConverterMoedasEmCripto() {
        Aluno aluno = alunoPremium();

        aluno.converterMoedas(2, DestinoMoeda.CRIPTOMOEDA);

        assertEquals(1, aluno.getMoedas());
        assertEquals(2, aluno.getMoedasEmCripto());
    }

    @Test
    @DisplayName("CT-15 - acumular mantem o saldo de moedas com o aluno")
    void deveAcumularMoedas() {
        Aluno aluno = alunoPremium();
        int cursosAntes = aluno.getCursosDisponiveis();

        aluno.converterMoedas(3, DestinoMoeda.ACUMULO);

        assertEquals(3, aluno.getMoedas());
        assertEquals(0, aluno.getMoedasEmCripto());
        assertEquals(cursosAntes, aluno.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-16 - participacao no forum nao pode ser negativa")
    void participacaoNegativaERecusada() {
        Aluno aluno = new Aluno("maria@teste.com");
        String esperada = "Participacao no forum nao pode ser negativa";

        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.participarDoForum(-1, 0)).getMessage());
        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.participarDoForum(0, -1)).getMessage());
    }

    @Test
    @DisplayName("CT-17 - participacao no forum acumula e identifica quem ajudou outros")
    void participacaoAcumula() {
        Aluno aluno = new Aluno("maria@teste.com");
        assertFalse(aluno.ajudouOutrosParticipantes());

        aluno.participarDoForum(4, 6);
        aluno.participarDoForum(1, 2);

        assertEquals(5, aluno.getTopicosNoForum());
        assertEquals(8, aluno.getComentariosNoForum());
        assertTrue(aluno.ajudouOutrosParticipantes());
    }

    @Test
    @DisplayName("CT-18 - premio do forum da 1 curso e zera a participacao do mes")
    void premioDoForumDaUmCurso() {
        Aluno aluno = new Aluno("maria@teste.com");
        aluno.participarDoForum(10, 9);

        aluno.receberPremioDoForum();

        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA + 1, aluno.getCursosDisponiveis());
        assertEquals(0, aluno.getTopicosNoForum());
        assertEquals(0, aluno.getComentariosNoForum());
    }

    @Test
    @DisplayName("CT-19 - construtor protegido exigido pelo JPA")
    void construtorDoJpa() {
        Aluno aluno = new Aluno();

        assertNotNull(aluno);
        assertNull(aluno.getId());
        assertNull(aluno.getEmail());
        assertNull(aluno.getPlano());
    }
}
