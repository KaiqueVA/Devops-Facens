package br.facens.gamificacao.recomendacao.application;

import br.facens.gamificacao.recomendacao.application.port.CursoRecomendado;
import java.util.List;

/**
 * Resposta ao front. {@code degradado=true} significa que a IA estava indisponivel e o
 * Core usou o catalogo padrao: o aluno continua recebendo sugestoes (ADR-003).
 */
public record Recomendacoes(Long alunoId, String origem, String modelo, boolean degradado,
                            List<CursoRecomendado> cursos) {

    public static final String ORIGEM_IA = "IA";
    public static final String ORIGEM_FALLBACK = "CATALOGO_PADRAO";
}
