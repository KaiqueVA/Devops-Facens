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
        List<ContribuicaoCategoria> contribuicoes = contribuicoesPorCategoria(certificadosAprovados);

        int contabilizadoPorCategoria = somar(contribuicoes, ContribuicaoCategoria::horasContabilizadas);
        int enviadoTotal = somar(contribuicoes, ContribuicaoCategoria::horasEnviadas);

        int totalContabilizado = Math.min(contabilizadoPorCategoria, TOTAL_MAXIMO);
        int totalExcedente = enviadoTotal - totalContabilizado;

        return new ResumoHoras(totalContabilizado, totalExcedente, contribuicoes);
    }

    private List<ContribuicaoCategoria> contribuicoesPorCategoria(List<Certificado> certificados) {
        Map<String, Categoria> categoriaPorNome = new LinkedHashMap<>();
        Map<String, Integer> horasEnviadasPorCategoria = new LinkedHashMap<>();
        for (Certificado certificado : certificados) {
            Categoria categoria = certificado.getCategoria();
            categoriaPorNome.putIfAbsent(categoria.getNome(), categoria);
            horasEnviadasPorCategoria.merge(categoria.getNome(), certificado.getCargaHoraria(), Integer::sum);
        }
        return horasEnviadasPorCategoria.entrySet().stream()
                .map(entrada -> ContribuicaoCategoria.aplicarTeto(
                        categoriaPorNome.get(entrada.getKey()), entrada.getValue()))
                .toList();
    }

    private int somar(List<ContribuicaoCategoria> contribuicoes,
                      java.util.function.ToIntFunction<ContribuicaoCategoria> campo) {
        return contribuicoes.stream().mapToInt(campo).sum();
    }
}
