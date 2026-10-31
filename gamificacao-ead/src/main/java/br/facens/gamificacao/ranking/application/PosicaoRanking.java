package br.facens.gamificacao.ranking.application;

/** Uma linha do ranking: posicao calculada + dados minimos do aluno. */
public record PosicaoRanking(
        int posicao,
        Long alunoId,
        String email,
        String plano,
        int cursosConquistados,
        int moedas) {
}
