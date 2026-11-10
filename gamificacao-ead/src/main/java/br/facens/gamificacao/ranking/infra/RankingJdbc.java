package br.facens.gamificacao.ranking.infra;

import br.facens.gamificacao.ranking.application.AlunoRankeado;
import br.facens.gamificacao.ranking.application.LeituraDoRanking;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Adapter de leitura: SQL direto sobre a tabela {@code aluno}, com ORDER BY +
 * LIMIT/OFFSET resolvidos no banco e apoiados pelo indice idx_aluno_ranking.
 *
 * <p>Trade-off consciente (ADR-001): o Ranking conhece o nome de 5 colunas da
 * tabela do modulo Gamificacao. Se o Ranking precisar de escala ou banco
 * proprios, esta classe e a unica a ser trocada (por uma projecao alimentada
 * por eventos, Desafio 4).</p>
 */
@Repository
public class RankingJdbc implements LeituraDoRanking {

    private static final String SQL = """
            SELECT id, email, plano, cursos_conquistados, moedas
              FROM aluno
             ORDER BY cursos_conquistados DESC, moedas DESC, id ASC
             LIMIT :limite OFFSET :offset
            """;

    private final JdbcClient jdbc;

    public RankingJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<AlunoRankeado> buscarPagina(int offset, int limite) {
        return jdbc.sql(SQL)
                .param("limite", limite)
                .param("offset", offset)
                .query((rs, n) -> new AlunoRankeado(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("plano"),
                        rs.getInt("cursos_conquistados"),
                        rs.getInt("moedas")))
                .list();
    }
}
