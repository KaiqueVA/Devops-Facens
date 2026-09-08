package br.facens.gamificacao.aluno.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/**
 * Raiz do agregado. Concentra as regras de gamificacao do case:
 * RN2 - concluir com media acima de 7,0 libera 3 cursos;
 * RN3 - destaque do forum ganha 1 curso no fim do mes;
 * RN4 - 12 cursos conquistados promovem o aluno a Premium (voucher + 3 moedas);
 * RN5 - moeda vira conhecimento, fica acumulada ou vira criptomoeda.
 */
@Entity
public class Aluno {

    public static final BigDecimal MEDIA_MINIMA = new BigDecimal("7.0");
    public static final BigDecimal MEDIA_MAXIMA = new BigDecimal("10.0");
    public static final int CURSOS_DA_ASSINATURA_BASICA = 5;
    public static final int CURSOS_LIBERADOS_POR_APROVACAO = 3;
    public static final int CURSOS_PARA_PREMIUM = 12;
    public static final int MOEDAS_DA_PROMOCAO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private PlanoAssinatura plano;

    private int cursosDisponiveis;
    private int cursosConcluidos;
    private int cursosConquistados;
    private int moedas;
    private int moedasEmCripto;
    private int topicosNoForum;
    private int comentariosNoForum;
    private boolean voucherProjetoReal;

    protected Aluno() {
        // exigido pelo JPA
    }

    public Aluno(String email) {
        if (email == null || email.isBlank()) {
            throw new GamificacaoException("E-mail do aluno e obrigatorio");
        }
        this.email = email;
        this.plano = PlanoAssinatura.BASICO;
        this.cursosDisponiveis = CURSOS_DA_ASSINATURA_BASICA;
    }

    /** RN2: curso terminado com media acima de 7,0 da direito a mais 3 cursos. */
    public void concluirCurso(String curso, BigDecimal media) {
        if (curso == null || curso.isBlank()) {
            throw new GamificacaoException("Nome do curso e obrigatorio");
        }
        if (media == null
                || media.compareTo(BigDecimal.ZERO) < 0
                || media.compareTo(MEDIA_MAXIMA) > 0) {
            throw new GamificacaoException("Media deve estar entre 0,0 e 10,0");
        }
        if (cursosDisponiveis <= 0) {
            throw new GamificacaoException("Aluno nao possui curso disponivel na assinatura");
        }

        this.cursosDisponiveis--;
        this.cursosConcluidos++;

        if (media.compareTo(MEDIA_MINIMA) > 0) {
            this.cursosConquistados++;
            this.cursosDisponiveis += CURSOS_LIBERADOS_POR_APROVACAO;
            promoverSeAtingiuAMeta();
        }
    }

    /** RN4: 12 cursos conquistados -> Premium, com voucher de projetos reais e 3 moedas. */
    private void promoverSeAtingiuAMeta() {
        if (plano == PlanoAssinatura.BASICO && cursosConquistados >= CURSOS_PARA_PREMIUM) {
            this.plano = PlanoAssinatura.PREMIUM;
            this.voucherProjetoReal = true;
            this.moedas += MOEDAS_DA_PROMOCAO;
        }
    }

    public void participarDoForum(int topicos, int comentarios) {
        if (topicos < 0 || comentarios < 0) {
            throw new GamificacaoException("Participacao no forum nao pode ser negativa");
        }
        this.topicosNoForum += topicos;
        this.comentariosNoForum += comentarios;
    }

    /** RN3: so concorre ao premio quem ajudou outros participantes com comentarios. */
    public boolean ajudouOutrosParticipantes() {
        return comentariosNoForum > 0;
    }

    public void receberPremioDoForum() {
        this.cursosDisponiveis++;
        this.topicosNoForum = 0;
        this.comentariosNoForum = 0;
    }

    /** RN5: moeda vira conhecimento, fica acumulada ou vira criptomoeda. */
    public void converterMoedas(int quantidade, DestinoMoeda destino) {
        if (plano != PlanoAssinatura.PREMIUM) {
            throw new GamificacaoException("Apenas aluno Premium possui moedas para converter");
        }
        if (destino == null) {
            throw new GamificacaoException("Destino da moeda e obrigatorio");
        }
        if (quantidade <= 0) {
            throw new GamificacaoException("Quantidade de moedas deve ser maior que zero");
        }
        if (quantidade > moedas) {
            throw new GamificacaoException("Saldo de moedas insuficiente");
        }

        if (destino == DestinoMoeda.CONHECIMENTO) {
            this.moedas -= quantidade;
            this.cursosDisponiveis += quantidade;
        } else if (destino == DestinoMoeda.CRIPTOMOEDA) {
            this.moedas -= quantidade;
            this.moedasEmCripto += quantidade;
        }
        // ACUMULO: o saldo permanece com o aluno
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public PlanoAssinatura getPlano() {
        return plano;
    }

    public int getCursosDisponiveis() {
        return cursosDisponiveis;
    }

    public int getCursosConcluidos() {
        return cursosConcluidos;
    }

    public int getCursosConquistados() {
        return cursosConquistados;
    }

    public int getMoedas() {
        return moedas;
    }

    public int getMoedasEmCripto() {
        return moedasEmCripto;
    }

    public int getTopicosNoForum() {
        return topicosNoForum;
    }

    public int getComentariosNoForum() {
        return comentariosNoForum;
    }

    public boolean isVoucherProjetoReal() {
        return voucherProjetoReal;
    }
}
