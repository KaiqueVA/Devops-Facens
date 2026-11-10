package br.facens.gamificacao.gamificacao.api;

/** Dados do aluno que o modulo Gamificacao publica para outros modulos (somente leitura). */
public record ResumoDoAluno(Long id, String plano, int cursosConcluidos, int cursosConquistados, int moedas) {
}
