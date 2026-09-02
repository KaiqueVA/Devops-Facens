package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.CalculadoraDeHoras;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.ResumoHoras;
import br.facens.horascomplementares.domain.StatusCertificado;
import br.facens.horascomplementares.repository.CertificadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HorasService {

    private final CertificadoRepository certificadoRepository;
    private final CalculadoraDeHoras calculadora = new CalculadoraDeHoras();

    public HorasService(CertificadoRepository certificadoRepository) {
        this.certificadoRepository = certificadoRepository;
    }

    @Transactional(readOnly = true)
    public ResumoHoras resumoDoAluno(Long alunoId) {
        List<Certificado> aprovados =
                certificadoRepository.findByAlunoIdAndStatus(alunoId, StatusCertificado.APROVADO);
        return calculadora.calcular(aprovados);
    }
}
