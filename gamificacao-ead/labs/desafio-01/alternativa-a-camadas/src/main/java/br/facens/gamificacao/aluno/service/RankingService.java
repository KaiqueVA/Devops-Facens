package br.facens.gamificacao.aluno.service;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.repository.AlunoRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/** Alternativa A: ranking na camada service, usando a entidade JPA Aluno diretamente. */
@Service
public class RankingService {

    private final AlunoRepository repository;

    public RankingService(AlunoRepository repository) {
        this.repository = repository;
    }

    public List<Aluno> consultar(int pagina, int tamanho) {
        return repository.findAllByOrderByCursosConquistadosDescMoedasDescIdAsc(
                PageRequest.of(pagina, Math.min(tamanho, 100))).getContent();
    }
}
