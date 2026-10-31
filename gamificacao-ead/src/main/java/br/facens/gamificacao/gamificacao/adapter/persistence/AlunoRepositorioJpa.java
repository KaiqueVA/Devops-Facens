package br.facens.gamificacao.gamificacao.adapter.persistence;

import br.facens.gamificacao.gamificacao.application.port.AlunoRepositorio;
import br.facens.gamificacao.gamificacao.domain.Aluno;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Adapter de saida padrao: JPA sobre H2/PostgreSQL. Desligado no perfil "memoria". */
@Repository
@Profile("!memoria")
public class AlunoRepositorioJpa implements AlunoRepositorio {

    private final AlunoJpaRepository jpa;

    AlunoRepositorioJpa(AlunoJpaRepository jpa) {
        this.jpa = jpa;
        LoggerFactory.getLogger(getClass()).info("Porta AlunoRepositorio -> adapter JPA (tabela aluno)");
    }

    @Override
    public Aluno salvar(Aluno aluno) {
        AlunoJpaEntity salvo = jpa.save(AlunoJpaEntity.de(aluno.estado()));
        return Aluno.reconstituir(salvo.estado());
    }

    @Override
    public Optional<Aluno> buscarPorId(Long id) {
        return jpa.findById(id).map(e -> Aluno.reconstituir(e.estado()));
    }

    @Override
    public Optional<Aluno> buscarDestaqueDoForum() {
        return jpa.findFirstByComentariosNoForumGreaterThanOrderByTopicosNoForumDescComentariosNoForumDescIdAsc(0)
                .map(e -> Aluno.reconstituir(e.estado()));
    }

    /** Apoio a testes e labs: limpa a tabela. */
    public void removerTodos() {
        jpa.deleteAllInBatch();
    }
}
