package br.facens.gamificacao.aluno.service;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.repository.AlunoRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class GamificacaoService {

    private final AlunoRepository repository;

    public GamificacaoService(AlunoRepository repository) {
        this.repository = repository;
    }

    public Aluno matricular(String email) {
        return repository.save(new Aluno(email));
    }

    public Aluno concluirCurso(Long id, String curso, BigDecimal media) {
        Aluno aluno = buscar(id);
        aluno.concluirCurso(curso, media);
        return repository.save(aluno);
    }

    public Aluno participarDoForum(Long id, int topicos, int comentarios) {
        Aluno aluno = buscar(id);
        aluno.participarDoForum(topicos, comentarios);
        return repository.save(aluno);
    }

    public Aluno converterMoedas(Long id, int quantidade, DestinoMoeda destino) {
        Aluno aluno = buscar(id);
        aluno.converterMoedas(quantidade, destino);
        return repository.save(aluno);
    }

    /** RN3: no fechamento do mes, mais topicos vence; empate decide por comentarios. */
    public Aluno premiarDestaqueDoForum() {
        Aluno destaque = repository.findAll().stream()
                .filter(Aluno::ajudouOutrosParticipantes)
                .max(Comparator.comparingInt(Aluno::getTopicosNoForum)
                        .thenComparingInt(Aluno::getComentariosNoForum))
                .orElseThrow(() ->
                        new GamificacaoException("Nenhum aluno elegivel ao premio do forum"));

        destaque.receberPremioDoForum();
        return repository.save(destaque);
    }

    public Aluno buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new GamificacaoException("Aluno nao encontrado: " + id));
    }
}
