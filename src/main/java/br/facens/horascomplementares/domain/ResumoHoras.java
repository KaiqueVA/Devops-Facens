package br.facens.horascomplementares.domain;

import java.util.List;

/**
 * Resultado do cálculo das horas complementares de um aluno, já aplicando o
 * teto de cada categoria e o limite geral de 200 horas.
 */
public record ResumoHoras(
        int totalContabilizado,
        int totalExcedente,
        List<ContribuicaoCategoria> contribuicoes
) {
}
