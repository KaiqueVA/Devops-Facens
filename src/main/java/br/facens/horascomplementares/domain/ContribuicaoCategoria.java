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

    /**
     * Cria a contribuição aplicando o teto da categoria: o que passar do teto
     * é registrado como excedente e não é contabilizado.
     */
    public static ContribuicaoCategoria aplicarTeto(Categoria categoria, int horasEnviadas) {
        int contabilizadas = Math.min(horasEnviadas, categoria.getTetoHoras());
        return new ContribuicaoCategoria(
                categoria.getNome(), horasEnviadas, contabilizadas, horasEnviadas - contabilizadas);
    }
}
