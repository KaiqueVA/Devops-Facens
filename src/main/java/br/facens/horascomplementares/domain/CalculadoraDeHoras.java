package br.facens.horascomplementares.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula o resumo de horas complementares aplicando o teto por categoria e o
 * limite geral de 200 horas. Domínio puro (sem Spring / sem persistência).
 */
public class CalculadoraDeHoras {

    public static final int TOTAL_MAXIMO = 200;

    public ResumoHoras calcular(List<Certificado> certificadosAprovados) {
        Map<String, Categoria> categoriaPorNome = new LinkedHashMap<>();
        Map<String, Integer> horasEnviadasPorCategoria = new LinkedHashMap<>();
        for (Certificado certificado : certificadosAprovados) {
            Categoria categoria = certificado.getCategoria();
            categoriaPorNome.putIfAbsent(categoria.getNome(), categoria);
            horasEnviadasPorCategoria.merge(categoria.getNome(), certificado.getCargaHoraria(), Integer::sum);
        }

        List<ContribuicaoCategoria> contribuicoes = horasEnviadasPorCategoria.entrySet().stream()
                .map(entrada -> contribuicaoDe(categoriaPorNome.get(entrada.getKey()), entrada.getValue()))
                .toList();

        int contabilizadoPorCategoria = contribuicoes.stream()
                .mapToInt(ContribuicaoCategoria::horasContabilizadas)
                .sum();
        int enviadoTotal = contribuicoes.stream()
                .mapToInt(ContribuicaoCategoria::horasEnviadas)
                .sum();

        int totalContabilizado = Math.min(contabilizadoPorCategoria, TOTAL_MAXIMO);
        int totalExcedente = enviadoTotal - totalContabilizado;

        return new ResumoHoras(totalContabilizado, totalExcedente, contribuicoes);
    }

    private ContribuicaoCategoria contribuicaoDe(Categoria categoria, int horasEnviadas) {
        int contabilizadas = Math.min(horasEnviadas, categoria.getTetoHoras());
        return new ContribuicaoCategoria(
                categoria.getNome(), horasEnviadas, contabilizadas, horasEnviadas - contabilizadas);
    }
}
