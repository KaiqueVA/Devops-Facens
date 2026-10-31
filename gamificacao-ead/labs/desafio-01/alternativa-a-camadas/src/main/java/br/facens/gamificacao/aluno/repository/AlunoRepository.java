package br.facens.gamificacao.aluno.repository;

import br.facens.gamificacao.aluno.domain.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Alternativa A: o mesmo repositorio do CRUD de alunos ganha a consulta do ranking. */
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Page<Aluno> findAllByOrderByCursosConquistadosDescMoedasDescIdAsc(Pageable pagina);
}
