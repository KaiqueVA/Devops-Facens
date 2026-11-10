package br.facens.gamificacao.gamificacao.adapter.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data: detalhe de implementacao do adapter, invisivel para o caso de uso. */
interface AlunoJpaRepository extends JpaRepository<AlunoJpaEntity, Long> {

    /** Mesma ordem do CriterioDoForum, resolvida pelo banco (idx_aluno_forum). */
    Optional<AlunoJpaEntity> findFirstByComentariosNoForumGreaterThanOrderByTopicosNoForumDescComentariosNoForumDescIdAsc(
            int comentarios);
}
