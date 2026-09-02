package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.ResumoHoras;
import br.facens.horascomplementares.repository.CertificadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HorasService {

    private final CertificadoRepository certificadoRepository;

    public HorasService(CertificadoRepository certificadoRepository) {
        this.certificadoRepository = certificadoRepository;
    }

    @Transactional(readOnly = true)
    public ResumoHoras resumoDoAluno(Long alunoId) {
        throw new UnsupportedOperationException("resumoDoAluno ainda nao implementado");
    }
}
