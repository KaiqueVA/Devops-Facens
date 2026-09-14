package br.facens.gamificacao.aluno.dto;

import java.math.BigDecimal;

/** Corpo da requisicao para concluir um curso (RN2). */
public record ConcluirCursoRequest(String curso, BigDecimal media) {
}
