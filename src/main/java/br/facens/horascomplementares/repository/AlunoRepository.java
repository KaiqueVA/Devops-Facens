package br.facens.horascomplementares.repository;

import br.facens.horascomplementares.domain.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
}
