package br.facens.gamificacao.recomendacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.gamificacao.api.ResumoDoAluno;
import br.facens.gamificacao.recomendacao.adapter.IaHttpAdapter;
import br.facens.gamificacao.recomendacao.application.port.AssistenteEducacional;
import br.facens.gamificacao.recomendacao.application.port.IaIndisponivelException;
import br.facens.gamificacao.recomendacao.application.port.MotorDeRecomendacao;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Adapter HTTP contra um "ia-service" falso (HttpServer da JDK): resposta normal,
 * erro 5xx, lentidao acima do timeout e servico desligado. Prova de que toda falha
 * da IA chega ao caso de uso como IaIndisponivelException, em tempo limitado.
 */
class IaHttpAdapterTest {

    private static final ResumoDoAluno ALUNO = new ResumoDoAluno(1L, "BASICO", 2, 2, 0);
    private HttpServer servidor;

    private IaHttpAdapter adapterPara(String url) {
        return new IaHttpAdapter(url, 300, 500, 500);
    }

    private String iaFalsa(int status, long atrasoMs, String corpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", troca -> {
            try {
                Thread.sleep(atrasoMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            troca.getResponseBody().write(bytes);
            troca.close();
        });
        servidor.start();
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void desligar() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    @DisplayName("CT-IA-01 - IA no ar: converte a resposta e ignora campos extras do contrato")
    void iaNoAr() throws IOException {
        String url = iaFalsa(200, 0, """
                {"modelo":"v1","alunoId":1,"recomendacoes":[
                  {"curso":"SPRING-201","titulo":"Spring Boot","trilha":"backend","score":1.0,"motivo":"nivel 2"}]}
                """);

        MotorDeRecomendacao.ResultadoDoModelo r = adapterPara(url).recomendar(ALUNO, 3);

        assertEquals("v1", r.modelo());
        assertEquals("SPRING-201", r.cursos().get(0).curso());
    }

    @Test
    @DisplayName("CT-IA-02 - assistente no ar: devolve resposta e fontes")
    void assistenteNoAr() throws IOException {
        String url = iaFalsa(200, 0, "{\"resposta\":\"12 cursos\",\"fontes\":[\"regras.md#Premium\"]}");

        AssistenteEducacional.Resposta r = adapterPara(url).perguntar("Como viro Premium?");

        assertEquals("12 cursos", r.resposta());
        assertEquals(1, r.fontes().size());
    }

    @Test
    @DisplayName("CT-IA-03 - IA devolve 503: vira IaIndisponivelException")
    void iaComErro() throws IOException {
        String url = iaFalsa(503, 0, "{\"detail\":\"modelo carregando\"}");

        assertThrows(IaIndisponivelException.class, () -> adapterPara(url).recomendar(ALUNO, 3));
        assertThrows(IaIndisponivelException.class, () -> adapterPara(url).perguntar("oi?"));
    }

    @Test
    @DisplayName("CT-IA-04 - IA lenta: o timeout corta a espera (~500 ms), nao trava o Core")
    void iaLenta() throws IOException {
        String url = iaFalsa(200, 3_000, "{}");

        long inicio = System.nanoTime();
        assertThrows(IaIndisponivelException.class, () -> adapterPara(url).recomendar(ALUNO, 3));
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        assertTrue(ms < 1_500, "esperou " + ms + " ms; o timeout deveria cortar em ~500 ms");
    }

    @Test
    @DisplayName("CT-IA-05 - IA desligada: conexao recusada vira IaIndisponivelException")
    void iaDesligada() throws IOException {
        int portaLivre;
        try (ServerSocket s = new ServerSocket(0)) {
            portaLivre = s.getLocalPort();
        }

        IaHttpAdapter adapter = adapterPara("http://localhost:" + portaLivre);

        assertThrows(IaIndisponivelException.class, () -> adapter.recomendar(ALUNO, 3));
        assertThrows(IaIndisponivelException.class, () -> adapter.perguntar("oi?"));
    }

    @Test
    @DisplayName("CT-IA-06 - resposta sem dados tambem e tratada como indisponibilidade")
    void corpoVazio() throws IOException {
        String url = iaFalsa(200, 0, "{}");
        assertThrows(IaIndisponivelException.class, () -> adapterPara(url).recomendar(ALUNO, 3));
        servidor.stop(0);

        String semCorpo = iaFalsa(200, 0, "");
        assertThrows(IaIndisponivelException.class, () -> adapterPara(semCorpo).recomendar(ALUNO, 3));
        assertThrows(IaIndisponivelException.class, () -> adapterPara(semCorpo).perguntar("oi?"));
    }
}
