package br.facens.gamificacao.eventos;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Provedor de notificacoes falso para os testes: pode ser "desligado" (responde 503). */
public final class ProvedorFalso {

    private final HttpServer servidor;
    private final List<String> chavesRecebidas = new CopyOnWriteArrayList<>();
    private volatile boolean disponivel = true;

    public ProvedorFalso() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/notificacoes", troca -> {
            troca.getRequestBody().readAllBytes();
            if (disponivel) {
                chavesRecebidas.add(troca.getRequestHeaders().getFirst("Idempotency-Key"));
                troca.sendResponseHeaders(202, -1);
            } else {
                troca.sendResponseHeaders(503, -1);
            }
            troca.close();
        });
        servidor.start();
    }

    public String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    public void ligar() {
        disponivel = true;
    }

    public void desligar() {
        disponivel = false;
    }

    public List<String> chavesRecebidas() {
        return chavesRecebidas;
    }
}
