package br.facens.horascomplementares.repository;

import br.facens.horascomplementares.domain.HistoricoValidacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoValidacaoRepository extends JpaRepository<HistoricoValidacao, Long> {

    List<HistoricoValidacao> findByCertificadoIdOrderByDataHoraAsc(Long certificadoId);
}
