package br.facens.gamificacao.recomendacao.adapter;

import br.facens.gamificacao.gamificacao.api.ResumoDoAluno;
import br.facens.gamificacao.recomendacao.application.port.AssistenteEducacional;
import br.facens.gamificacao.recomendacao.application.port.CursoRecomendado;
import br.facens.gamificacao.recomendacao.application.port.IaIndisponivelException;
import br.facens.gamificacao.recomendacao.application.port.MotorDeRecomendacao;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adapter HTTP/JSON para o ia-service (FastAPI). Cada porta tem seu proprio cliente
 * e seu proprio timeout: recomendacao precisa ser rapida; o assistente (RAG/LLM)
 * pode demorar mais. Qualquer falha de rede, timeout ou 4xx/5xx vira
 * IaIndisponivelException, que o caso de uso transforma em resposta degradada.
 */
@Component
public class IaHttpAdapter implements MotorDeRecomendacao, AssistenteEducacional {

    private final RestClient recomendacao;
    private final RestClient assistente;

    public IaHttpAdapter(@Value("${ia.url:http://localhost:8000}") String url,
                         @Value("${ia.timeout.conexao-ms:300}") long conexaoMs,
                         @Value("${ia.timeout.recomendacao-ms:800}") long recomendacaoMs,
                         @Value("${ia.timeout.assistente-ms:3000}") long assistenteMs) {
        this.recomendacao = cliente(url, conexaoMs, recomendacaoMs);
        this.assistente = cliente(url, conexaoMs, assistenteMs);
    }

    private static RestClient cliente(String url, long conexaoMs, long leituraMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(conexaoMs));
        fabrica.setReadTimeout(Duration.ofMillis(leituraMs));
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @Override
    public ResultadoDoModelo recomendar(ResumoDoAluno aluno, int quantidade) {
        try {
            RespostaRecomendacao r = recomendacao.post().uri("/recomendacoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("alunoId", aluno.id(), "plano", aluno.plano(),
                            "cursosConcluidos", aluno.cursosConcluidos(),
                            "cursosConquistados", aluno.cursosConquistados(),
                            "quantidade", quantidade))
                    .retrieve()
                    .body(RespostaRecomendacao.class);
            if (r == null || r.recomendacoes() == null) {
                throw new IaIndisponivelException("Resposta vazia do ia-service", null);
            }
            return new ResultadoDoModelo(r.modelo(), r.recomendacoes());
        } catch (RestClientException e) {
            throw new IaIndisponivelException("Falha ao chamar /recomendacoes: " + causaRaiz(e), e);
        }
    }

    @Override
    public Resposta perguntar(String pergunta) {
        try {
            Resposta r = assistente.post().uri("/assistente/perguntas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("pergunta", pergunta))
                    .retrieve()
                    .body(Resposta.class);
            if (r == null) {
                throw new IaIndisponivelException("Resposta vazia do ia-service", null);
            }
            return r;
        } catch (RestClientException e) {
            throw new IaIndisponivelException("Falha ao chamar /assistente/perguntas: " + causaRaiz(e), e);
        }
    }

    private static String causaRaiz(RestClientException e) {
        Throwable raiz = e.getMostSpecificCause();
        return raiz.getClass().getSimpleName() + " - " + raiz.getMessage();
    }

    record RespostaRecomendacao(String modelo, List<CursoRecomendado> recomendacoes) {
    }
}
