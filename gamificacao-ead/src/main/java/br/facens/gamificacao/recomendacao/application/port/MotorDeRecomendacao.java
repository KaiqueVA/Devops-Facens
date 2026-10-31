package br.facens.gamificacao.recomendacao.application.port;

import br.facens.gamificacao.gamificacao.api.ResumoDoAluno;
import java.util.List;

/** Porta para o modelo de recomendacao (RF03). Lanca IaIndisponivelException em falha/timeout. */
public interface MotorDeRecomendacao {

    ResultadoDoModelo recomendar(ResumoDoAluno aluno, int quantidade);

    record ResultadoDoModelo(String modelo, List<CursoRecomendado> cursos) {
    }
}
