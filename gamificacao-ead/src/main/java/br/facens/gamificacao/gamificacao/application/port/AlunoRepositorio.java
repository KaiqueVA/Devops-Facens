package br.facens.gamificacao.gamificacao.application.port;

import br.facens.gamificacao.gamificacao.domain.Aluno;
import java.util.Optional;

/**
 * Porta de saida (ADR-002). O caso de uso depende desta interface; quem a
 * implementa e um adapter (JPA em producao, memoria em testes/lab).
 */
public interface AlunoRepositorio {

    /** Insere (id nulo) ou atualiza. Devolve o agregado com id preenchido. */
    Aluno salvar(Aluno aluno);

    Optional<Aluno> buscarPorId(Long id);

    /** Vencedor do forum pelo {@code CriterioDoForum}, sem carregar todos os alunos. */
    Optional<Aluno> buscarDestaqueDoForum();
}
