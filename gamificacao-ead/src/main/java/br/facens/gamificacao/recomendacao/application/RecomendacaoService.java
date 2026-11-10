package br.facens.gamificacao.recomendacao.application;

import br.facens.gamificacao.gamificacao.api.ConsultaDeAluno;
import br.facens.gamificacao.gamificacao.api.ResumoDoAluno;
import br.facens.gamificacao.recomendacao.application.port.AssistenteEducacional;
import br.facens.gamificacao.recomendacao.application.port.IaIndisponivelException;
import br.facens.gamificacao.recomendacao.application.port.MotorDeRecomendacao;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * RF03/RF04 com tolerancia a falhas (ASR-06): a indisponibilidade da IA vira resposta
 * degradada, nunca erro 500 e nunca bloqueio do fluxo essencial.
 */
@Service
public class RecomendacaoService {

    private static final Logger LOG = LoggerFactory.getLogger(RecomendacaoService.class);
    private static final int QUANTIDADE = 3;

    private final ConsultaDeAluno alunos;
    private final MotorDeRecomendacao motor;
    private final AssistenteEducacional assistente;

    /** Contadores lidos pelas metricas do Desafio 6. */
    private final AtomicLong recomendacoesPelaIa = new AtomicLong();
    private final AtomicLong recomendacoesDegradadas = new AtomicLong();
    private final AtomicLong assistenteIndisponivel = new AtomicLong();

    public RecomendacaoService(ConsultaDeAluno alunos, MotorDeRecomendacao motor,
                               AssistenteEducacional assistente) {
        this.alunos = alunos;
        this.motor = motor;
        this.assistente = assistente;
    }

    public Recomendacoes recomendar(Long alunoId) {
        ResumoDoAluno aluno = alunos.buscarResumo(alunoId)
                .orElseThrow(() -> new AlunoSemCadastroException(alunoId));
        try {
            MotorDeRecomendacao.ResultadoDoModelo resultado = motor.recomendar(aluno, QUANTIDADE);
            recomendacoesPelaIa.incrementAndGet();
            return new Recomendacoes(alunoId, Recomendacoes.ORIGEM_IA, resultado.modelo(), false, resultado.cursos());
        } catch (IaIndisponivelException e) {
            recomendacoesDegradadas.incrementAndGet();
            LOG.warn("IA indisponivel, usando catalogo padrao para o aluno {}: {}", alunoId, e.getMessage());
            return new Recomendacoes(alunoId, Recomendacoes.ORIGEM_FALLBACK, CatalogoPadrao.VERSAO, true,
                    CatalogoPadrao.CURSOS);
        }
    }

    public AssistenteEducacional.Resposta perguntar(String pergunta) {
        try {
            return assistente.perguntar(pergunta);
        } catch (IaIndisponivelException e) {
            assistenteIndisponivel.incrementAndGet();
            LOG.warn("Assistente indisponivel: {}", e.getMessage());
            throw new AssistenteIndisponivelException(e);
        }
    }

    public long getRecomendacoesPelaIa() {
        return recomendacoesPelaIa.get();
    }

    public long getRecomendacoesDegradadas() {
        return recomendacoesDegradadas.get();
    }

    public long getAssistenteIndisponivel() {
        return assistenteIndisponivel.get();
    }
}
