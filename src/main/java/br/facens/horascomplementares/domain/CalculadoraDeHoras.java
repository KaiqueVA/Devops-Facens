package br.facens.horascomplementares.domain;

import java.util.List;

/**
 * Calcula o resumo de horas complementares aplicando o teto por categoria e o
 * limite geral de 200 horas. Domínio puro (sem Spring / sem persistência).
 */
public class CalculadoraDeHoras {

    public static final int TOTAL_MAXIMO = 200;

    public ResumoHoras calcular(List<Certificado> certificadosAprovados) {
        throw new UnsupportedOperationException("calcular ainda nao implementado");
    }
}
