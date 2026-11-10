package br.facens.gamificacao.aluno.dto;

import br.facens.gamificacao.aluno.domain.Aluno;

public record RankingResponse(int posicao, Long alunoId, String email, int cursosConquistados, int moedas) {

    public static RankingResponse from(int posicao, Aluno aluno) {
        return new RankingResponse(posicao, aluno.getId(), aluno.getEmail(),
                aluno.getCursosConquistados(), aluno.getMoedas());
    }
}
