package br.facens.gamificacao.ranking.application;

/** Projecao lida do banco, ja na ordem do ranking. */
public record AlunoRankeado(Long alunoId, String email, String plano, int cursosConquistados, int moedas) {
}
