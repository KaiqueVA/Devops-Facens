package br.facens.gamificacao.iot.application;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoIotRepository extends JpaRepository<EventoIot, Long> {

    boolean existsByEventoId(String eventoId);

    long countByAlunoId(Long alunoId);
}
