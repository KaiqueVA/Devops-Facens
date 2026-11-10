package br.facens.gamificacao.ranking.application;

import java.util.List;

/**
 * Porta de leitura do ranking. A implementacao precisa devolver os alunos ja
 * ordenados (mais cursos conquistados, depois mais moedas, depois menor id) e
 * fazer o corte no banco: nunca carregar todos os alunos em memoria.
 */
public interface LeituraDoRanking {

    List<AlunoRankeado> buscarPagina(int offset, int limite);
}
