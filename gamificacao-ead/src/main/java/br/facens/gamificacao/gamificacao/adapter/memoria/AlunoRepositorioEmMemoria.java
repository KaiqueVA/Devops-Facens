package br.facens.gamificacao.gamificacao.adapter.memoria;

import br.facens.gamificacao.gamificacao.application.port.AlunoRepositorio;
import br.facens.gamificacao.gamificacao.domain.Aluno;
import br.facens.gamificacao.gamificacao.domain.CriterioDoForum;
import br.facens.gamificacao.gamificacao.domain.EstadoDoAluno;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * Adapter de saida alternativo (ADR-002): guarda fotografias do agregado num mapa.
 * Ativado com o perfil "memoria"; usado tambem diretamente nos testes de
 * aplicacao, sem Spring e sem banco.
 */
@Repository
@Profile("memoria")
public class AlunoRepositorioEmMemoria implements AlunoRepositorio {

    private final Map<Long, EstadoDoAluno> dados = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    public AlunoRepositorioEmMemoria() {
        LoggerFactory.getLogger(getClass()).info("Porta AlunoRepositorio -> adapter EM MEMORIA (sem banco)");
    }

    @Override
    public Aluno salvar(Aluno aluno) {
        EstadoDoAluno e = aluno.estado();
        Long id = e.id() != null ? e.id() : sequencia.incrementAndGet();
        EstadoDoAluno salvo = new EstadoDoAluno(id, e.email(), e.plano(), e.cursosDisponiveis(),
                e.cursosConcluidos(), e.cursosConquistados(), e.moedas(), e.moedasEmCripto(),
                e.topicosNoForum(), e.comentariosNoForum(), e.voucherProjetoReal());
        dados.put(id, salvo);
        return Aluno.reconstituir(salvo);
    }

    @Override
    public Optional<Aluno> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id)).map(Aluno::reconstituir);
    }

    @Override
    public Optional<Aluno> buscarDestaqueDoForum() {
        return dados.values().stream()
                .map(Aluno::reconstituir)
                .filter(CriterioDoForum::elegivel)
                .min(CriterioDoForum.ORDEM);
    }
}
