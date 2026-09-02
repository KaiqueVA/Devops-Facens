package br.facens.horascomplementares.domain;

/**
 * Quanto uma categoria contribuiu para o total de horas complementares do aluno.
 */
public record ContribuicaoCategoria(
        String categoria,
        int horasEnviadas,
        int horasContabilizadas,
        int horasExcedentes
) {
}
