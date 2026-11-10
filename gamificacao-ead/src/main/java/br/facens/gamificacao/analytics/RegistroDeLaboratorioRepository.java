package br.facens.gamificacao.analytics;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroDeLaboratorioRepository extends JpaRepository<RegistroDeLaboratorio, Long> {

    boolean existsByEventoId(String eventoId);
}
