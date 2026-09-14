package br.facens.gamificacao.aluno.repository;

import br.facens.gamificacao.aluno.domain.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
}
