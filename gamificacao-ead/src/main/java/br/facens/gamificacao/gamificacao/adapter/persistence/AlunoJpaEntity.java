package br.facens.gamificacao.gamificacao.adapter.persistence;

import br.facens.gamificacao.gamificacao.domain.EstadoDoAluno;
import br.facens.gamificacao.gamificacao.domain.PlanoAssinatura;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Modelo de persistencia do Aluno. Mesma tabela e mesmas colunas da AC1
 * (RPC: legado), agora separado do modelo de dominio.
 */
@Entity
@Table(name = "aluno", indexes = {
        // RF01 / ASR-02: o ranking ordena por estas colunas
        @Index(name = "idx_aluno_ranking", columnList = "cursos_conquistados DESC, moedas DESC, id"),
        // RN3: o destaque do forum e buscado direto no banco
        @Index(name = "idx_aluno_forum", columnList = "topicos_no_forum DESC, comentarios_no_forum DESC, id")})
class AlunoJpaEntity {

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

    protected AlunoJpaEntity() {
        // exigido pelo JPA
    }

    static AlunoJpaEntity de(EstadoDoAluno e) {
        AlunoJpaEntity entity = new AlunoJpaEntity();
        entity.id = e.id();
        entity.email = e.email();
        entity.plano = e.plano();
        entity.cursosDisponiveis = e.cursosDisponiveis();
        entity.cursosConcluidos = e.cursosConcluidos();
        entity.cursosConquistados = e.cursosConquistados();
        entity.moedas = e.moedas();
        entity.moedasEmCripto = e.moedasEmCripto();
        entity.topicosNoForum = e.topicosNoForum();
        entity.comentariosNoForum = e.comentariosNoForum();
        entity.voucherProjetoReal = e.voucherProjetoReal();
        return entity;
    }

    EstadoDoAluno estado() {
        return new EstadoDoAluno(id, email, plano, cursosDisponiveis, cursosConcluidos, cursosConquistados,
                moedas, moedasEmCripto, topicosNoForum, comentariosNoForum, voucherProjetoReal);
    }
}
