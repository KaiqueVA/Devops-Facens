package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.exception.AlunoInexistenteException;
import br.facens.horascomplementares.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public Aluno cadastrar(String nome, LocalDate dataIngresso) {
        return alunoRepository.save(new Aluno(nome, dataIngresso));
    }

    @Transactional(readOnly = true)
    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Aluno buscar(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new AlunoInexistenteException(id));
    }
}
