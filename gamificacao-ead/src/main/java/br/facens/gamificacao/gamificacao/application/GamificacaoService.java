package br.facens.gamificacao.gamificacao.application;

import br.facens.gamificacao.gamificacao.api.CursoConcluido;
import br.facens.gamificacao.gamificacao.application.port.AlunoRepositorio;
import br.facens.gamificacao.gamificacao.domain.Aluno;
import br.facens.gamificacao.gamificacao.domain.DestinoMoeda;
import br.facens.gamificacao.gamificacao.domain.GamificacaoException;
import br.facens.gamificacao.gamificacao.domain.ResultadoDaConclusao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso do RF02. Orquestra: busca pela porta, aplica a regra no dominio,
 * salva pela porta. Nao conhece JPA nem HTTP (ADR-002).
 *
 * <p>Desafio 4 (ADR-004): a conclusao de curso e SINCRONA para o que precisa ser
 * imediato (registrar, calcular recompensa, atualizar o aluno) e publica o evento
 * {@link CursoConcluido} na MESMA transacao. Quem reage ao evento (notificacao,
 * analytics) roda depois, de forma assincrona, sem poder desfazer a conclusao.</p>
 */
@Service
@Transactional
public class GamificacaoService {

    private final AlunoRepositorio repositorio;
    private final ApplicationEventPublisher eventos;
    private final Clock relogio = Clock.systemUTC();

    public GamificacaoService(AlunoRepositorio repositorio, ApplicationEventPublisher eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    public Aluno matricular(String email) {
        return repositorio.salvar(new Aluno(email));
    }

    public Aluno concluirCurso(Long id, String curso, BigDecimal media) {
        Aluno aluno = buscar(id);
        ResultadoDaConclusao resultado = aluno.concluirCurso(curso, media);
        Aluno salvo = repositorio.salvar(aluno);
        eventos.publishEvent(new CursoConcluido(salvo.getId(), salvo.getEmail(), curso, media,
                resultado.conquistado(), resultado.cursosLiberados(), resultado.promovidoAPremium(),
                salvo.getPlano().name(), salvo.getCursosConquistados(), Instant.now(relogio)));
        return salvo;
    }

    public Aluno participarDoForum(Long id, int topicos, int comentarios) {
        Aluno aluno = buscar(id);
        aluno.participarDoForum(topicos, comentarios);
        return repositorio.salvar(aluno);
    }

    public Aluno converterMoedas(Long id, int quantidade, DestinoMoeda destino) {
        Aluno aluno = buscar(id);
        aluno.converterMoedas(quantidade, destino);
        return repositorio.salvar(aluno);
    }

    /** RN3: no fechamento do mes, o destaque do forum (CriterioDoForum) ganha 1 curso. */
    public Aluno premiarDestaqueDoForum() {
        Aluno destaque = repositorio.buscarDestaqueDoForum()
                .orElseThrow(() -> new GamificacaoException("Nenhum aluno elegivel ao premio do forum"));
        destaque.receberPremioDoForum();
        return repositorio.salvar(destaque);
    }

    @Transactional(readOnly = true)
    public Aluno buscar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new GamificacaoException("Aluno nao encontrado: " + id));
    }
}
