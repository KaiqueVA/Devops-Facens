package br.facens.gamificacao.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.facens.gamificacao.ranking.application.AlunoRankeado;
import br.facens.gamificacao.ranking.application.PosicaoRanking;
import br.facens.gamificacao.ranking.application.RankingService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regras do RankingService sem Spring e sem banco (leitura fake). */
class RankingServiceTest {

    private int ultimoOffset;
    private int ultimoLimite;

    private RankingService serviceCom(List<AlunoRankeado> base) {
        return new RankingService((offset, limite) -> {
            ultimoOffset = offset;
            ultimoLimite = limite;
            List<AlunoRankeado> pagina = new ArrayList<>();
            for (int i = offset; i < Math.min(base.size(), offset + limite); i++) {
                pagina.add(base.get(i));
            }
            return pagina;
        });
    }

    private List<AlunoRankeado> alunos(int quantidade) {
        List<AlunoRankeado> lista = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            lista.add(new AlunoRankeado((long) i, "a" + i + "@teste.com", "BASICO", 100 - i, 0));
        }
        return lista;
    }

    @Test
    @DisplayName("CT-RNK-01 - posicoes continuam a numeracao entre paginas")
    void posicaoConsideraOffset() {
        List<PosicaoRanking> segundaPagina = serviceCom(alunos(25)).consultar(1, 10);

        assertEquals(10, segundaPagina.size());
        assertEquals(11, segundaPagina.get(0).posicao());
        assertEquals(11L, segundaPagina.get(0).alunoId());
    }

    @Test
    @DisplayName("CT-RNK-02 - tamanho acima do maximo e limitado (nunca le a base inteira)")
    void tamanhoLimitado() {
        serviceCom(alunos(5)).consultar(0, 100_000);

        assertEquals(RankingService.TAMANHO_MAXIMO, ultimoLimite);
    }

    @Test
    @DisplayName("CT-RNK-03 - pagina negativa e tamanho invalido usam o padrao")
    void valoresInvalidosUsamPadrao() {
        serviceCom(alunos(5)).consultar(-3, 0);

        assertEquals(0, ultimoOffset);
        assertEquals(RankingService.TAMANHO_PADRAO, ultimoLimite);
    }
}
