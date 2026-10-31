package br.facens.gamificacao.ranking.application;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * RF01 - Ranking de alunos.
 *
 * <p>Criterio: cursos conquistados (desc), moedas (desc) e, no empate, quem
 * entrou primeiro (menor id). O tamanho da pagina e limitado para que nenhuma
 * requisicao force a leitura de 100 mil linhas (ASR-02).</p>
 */
@Service
public class RankingService {

    public static final int TAMANHO_PADRAO = 10;
    public static final int TAMANHO_MAXIMO = 100;

    private final LeituraDoRanking leitura;

    public RankingService(LeituraDoRanking leitura) {
        this.leitura = leitura;
    }

    public List<PosicaoRanking> consultar(int pagina, int tamanho) {
        int paginaValida = Math.max(pagina, 0);
        int tamanhoValido = tamanho <= 0 ? TAMANHO_PADRAO : Math.min(tamanho, TAMANHO_MAXIMO);
        int offset = paginaValida * tamanhoValido;

        List<AlunoRankeado> alunos = leitura.buscarPagina(offset, tamanhoValido);
        List<PosicaoRanking> posicoes = new ArrayList<>(alunos.size());
        for (int i = 0; i < alunos.size(); i++) {
            AlunoRankeado a = alunos.get(i);
            posicoes.add(new PosicaoRanking(offset + i + 1, a.alunoId(), a.email(), a.plano(),
                    a.cursosConquistados(), a.moedas()));
        }
        return posicoes;
    }
}
