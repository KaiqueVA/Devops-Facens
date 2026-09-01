package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.exception.AlunoInexistenteException;
import br.facens.horascomplementares.domain.exception.CargaHorariaInvalidaException;
import br.facens.horascomplementares.domain.exception.CategoriaInexistenteException;
import br.facens.horascomplementares.domain.exception.CertificadoDuplicadoException;
import br.facens.horascomplementares.domain.exception.DataCertificadoInvalidaException;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.repository.AlunoRepository;
import br.facens.horascomplementares.repository.CategoriaRepository;
import br.facens.horascomplementares.repository.CertificadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class CertificadoService {

    private static final int CARGA_HORARIA_MINIMA = 1;
    private static final int CARGA_HORARIA_MAXIMA = 200;

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

    @Transactional
    public Certificado submeter(SubmissaoCertificadoDTO dados) {
        Aluno aluno = alunoRepository.findById(dados.alunoId())
                .orElseThrow(() -> new AlunoInexistenteException(dados.alunoId()));

        Categoria categoria = categoriaRepository.findByNome(dados.categoria())
                .orElseThrow(() -> new CategoriaInexistenteException(dados.categoria()));

        if (dados.cargaHoraria() < CARGA_HORARIA_MINIMA || dados.cargaHoraria() > CARGA_HORARIA_MAXIMA) {
            throw new CargaHorariaInvalidaException(dados.cargaHoraria());
        }

        LocalDate hoje = LocalDate.now(clock);
        if (dados.data().isAfter(hoje)) {
            throw new DataCertificadoInvalidaException("nao pode estar no futuro");
        }
        if (dados.data().isBefore(aluno.getDataIngresso())) {
            throw new DataCertificadoInvalidaException("nao pode ser anterior ao ingresso do aluno");
        }

        if (certificadoRepository.existsByAlunoIdAndTituloIgnoreCaseAndData(
                aluno.getId(), dados.titulo(), dados.data())) {
            throw new CertificadoDuplicadoException(dados.titulo(), dados.data());
        }

        Certificado certificado = new Certificado(
                aluno, categoria, dados.titulo(), dados.cargaHoraria(), dados.data());
        return certificadoRepository.save(certificado);
    }
}
