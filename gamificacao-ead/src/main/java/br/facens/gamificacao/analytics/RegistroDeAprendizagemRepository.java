package br.facens.gamificacao.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegistroDeAprendizagemRepository extends JpaRepository<RegistroDeAprendizagem, Long> {

    boolean existsByEventoId(String eventoId);

    long countByConquistadoTrue();

    long countByPromovidoAPremiumTrue();

    @Query("select avg(r.media) from RegistroDeAprendizagem r")
    Double mediaGeral();
}
