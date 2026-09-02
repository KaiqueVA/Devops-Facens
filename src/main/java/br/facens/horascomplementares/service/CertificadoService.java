package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.CertificadoValidator;
import br.facens.horascomplementares.domain.exception.AlunoInexistenteException;
import br.facens.horascomplementares.domain.exception.CategoriaInexistenteException;
import br.facens.horascomplementares.domain.exception.CertificadoDuplicadoException;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.repository.AlunoRepository;
import br.facens.horascomplementares.repository.CategoriaRepository;
import br.facens.horascomplementares.repository.CertificadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final AlunoRepository alunoRepository;
    private final CategoriaRepository categoriaRepository;
    private final Clock clock;
    private final CertificadoValidator validator = new CertificadoValidator();

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

        validator.validar(dados.cargaHoraria(), dados.data(), aluno.getDataIngresso(), LocalDate.now(clock));

        if (certificadoRepository.existsByAlunoIdAndTituloIgnoreCaseAndData(
                aluno.getId(), dados.titulo(), dados.data())) {
            throw new CertificadoDuplicadoException(dados.titulo(), dados.data());
        }

        Certificado certificado = new Certificado(
                aluno, categoria, dados.titulo(), dados.cargaHoraria(), dados.data());
        return certificadoRepository.save(certificado);
    }

    @Transactional(readOnly = true)
    public List<Certificado> listarPorAluno(Long alunoId) {
        return certificadoRepository.findByAlunoIdOrderByDataAsc(alunoId);
    }
}
