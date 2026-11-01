package br.facens.gamificacao.eventos.outbox;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EntregaPendenteRepository extends JpaRepository<EntregaPendente, Long> {

    @Query("select e.id from EntregaPendente e where e.status = :status and e.proximaTentativaEm <= :agora order by e.id")
    List<Long> idsProntos(StatusEntrega status, Instant agora, Pageable limite);

    long countByStatus(StatusEntrega status);

    @Query("select min(e.criadoEm) from EntregaPendente e where e.status = :status")
    Instant maisAntiga(StatusEntrega status);

    List<EntregaPendente> findByStatus(StatusEntrega status);

    List<EntregaPendente> findTop20ByOrderByIdDesc();

    @Query("select e.assinante, e.status, count(e) from EntregaPendente e group by e.assinante, e.status order by e.assinante, e.status")
    List<Object[]> contarPorAssinanteEStatus();
}
