package br.facens.gamificacao.gamificacao.domain;

import java.util.Comparator;

/**
 * RN3: concorre quem ajudou outros participantes (comentou); vence quem escreveu
 * mais topicos; empate decide por comentarios; persistindo, quem chegou primeiro.
 *
 * <p>A regra mora no dominio. Os adapters de persistencia precisam devolver o
 * mesmo vencedor: isso e garantido pelo teste de contrato da porta.</p>
 */
public final class CriterioDoForum {

    public static final Comparator<Aluno> ORDEM = Comparator
            .comparingInt(Aluno::getTopicosNoForum).reversed()
            .thenComparing(Comparator.comparingInt(Aluno::getComentariosNoForum).reversed())
            .thenComparing(Aluno::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    public static boolean elegivel(Aluno aluno) {
        return aluno.ajudouOutrosParticipantes();
    }

    private CriterioDoForum() {
    }
}
