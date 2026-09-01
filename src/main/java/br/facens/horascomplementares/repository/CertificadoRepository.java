package br.facens.horascomplementares.repository;

import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.StatusCertificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    boolean existsByAlunoIdAndTituloIgnoreCaseAndData(Long alunoId, String titulo, LocalDate data);

    List<Certificado> findByAlunoIdAndStatus(Long alunoId, StatusCertificado status);
}
