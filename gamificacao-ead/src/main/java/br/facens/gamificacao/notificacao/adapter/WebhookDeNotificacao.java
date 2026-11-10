package br.facens.gamificacao.notificacao.adapter;

import br.facens.gamificacao.notificacao.application.port.CanalDeNotificacao;
import br.facens.gamificacao.notificacao.application.port.Notificacao;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adapter para o provedor externo de notificacoes (no lab: servicos/notificacao-stub).
 * Envia o cabecalho Idempotency-Key para que reentregas nao virem avisos duplicados.
 */
@Component
public class WebhookDeNotificacao implements CanalDeNotificacao {

    private final RestClient http;

    public WebhookDeNotificacao(@Value("${notificacao.url:http://localhost:8090}") String url,
                                @Value("${notificacao.timeout-ms:2000}") long timeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @Override
    public void enviar(Notificacao n) {
        http.post().uri("/notificacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", n.chaveIdempotencia())
                .body(n)
                .retrieve()
                .toBodilessEntity();
    }
}
