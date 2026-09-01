package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.repository.AlunoRepository;
import br.facens.horascomplementares.repository.CategoriaRepository;
import br.facens.horascomplementares.repository.CertificadoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final AlunoRepository alunoRepository;
    private final CategoriaRepository categoriaRepository;
    private final Clock clock;

    public CertificadoService(CertificadoRepository certificadoRepository,
                              AlunoRepository alunoRepository,
                              CategoriaRepository categoriaRepository,
                              Clock clock) {
        this.certificadoRepository = certificadoRepository;
        this.alunoRepository = alunoRepository;
        this.categoriaRepository = categoriaRepository;
        this.clock = clock;
    }

    public Certificado submeter(SubmissaoCertificadoDTO dados) {
        throw new UnsupportedOperationException("submeter ainda nao implementado");
    }
}
