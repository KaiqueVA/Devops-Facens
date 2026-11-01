package br.facens.gamificacao.eventos.outbox;

import br.facens.gamificacao.eventos.api.AssinanteDeEvento;
import br.facens.gamificacao.eventos.api.Entrega;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Le o outbox e entrega cada evento ao seu assinante, uma entrega por transacao.
 * Falha de um assinante nao afeta os outros nem a operacao que gerou o evento.
 */
@Component
public class RelayDoOutbox {

    private static final Logger LOG = LoggerFactory.getLogger(RelayDoOutbox.class);

    private final EntregaPendenteRepository repositorio;
    private final Map<String, AssinanteDeEvento<?>> assinantes;
    private final ObjectMapper json;
    private final TransactionTemplate transacao;
    private final Clock relogio;
    private final boolean habilitado;
    private final int lote;
    private final int maxTentativas;
    private final Duration backoffBase;
    private final Duration backoffTeto;

    public RelayDoOutbox(EntregaPendenteRepository repositorio, List<AssinanteDeEvento<?>> assinantes,
                         ObjectMapper json, TransactionTemplate transacao, Clock relogioDosEventos,
                         @Value("${eventos.relay.habilitado:true}") boolean habilitado,
                         @Value("${eventos.relay.lote:50}") int lote,
                         @Value("${eventos.relay.max-tentativas:8}") int maxTentativas,
                         @Value("${eventos.relay.backoff-base-ms:1000}") long backoffBaseMs,
                         @Value("${eventos.relay.backoff-teto-ms:60000}") long backoffTetoMs) {
        this.repositorio = repositorio;
        this.assinantes = assinantes.stream().collect(Collectors.toMap(AssinanteDeEvento::nome, Function.identity()));
        this.json = json;
        this.transacao = transacao;
        this.relogio = relogioDosEventos;
        this.habilitado = habilitado;
        this.lote = lote;
        this.maxTentativas = maxTentativas;
        this.backoffBase = Duration.ofMillis(backoffBaseMs);
        this.backoffTeto = Duration.ofMillis(backoffTetoMs);
    }

    @Scheduled(fixedDelayString = "${eventos.relay.intervalo-ms:2000}", initialDelayString = "${eventos.relay.intervalo-ms:2000}")
    public void agendado() {
        if (habilitado) {
            processarPendentes(Instant.now(relogio));
        }
    }

    /** Processa as entregas prontas ate {@code agora}. Devolve quantas foram entregues com sucesso. */
    public int processarPendentes(Instant agora) {
        List<Long> ids = repositorio.idsProntos(StatusEntrega.PENDENTE, agora, PageRequest.of(0, lote));
        int entregues = 0;
        for (Long id : ids) {
            Boolean ok = transacao.execute(status -> entregar(id, agora));
            if (Boolean.TRUE.equals(ok)) {
                entregues++;
            }
        }
        return entregues;
    }

    private boolean entregar(Long id, Instant agora) {
        EntregaPendente entrega = repositorio.findById(id).orElseThrow();
        AssinanteDeEvento<?> assinante = assinantes.get(entrega.getAssinante());
        try {
            if (assinante == null) {
                throw new IllegalStateException("Assinante nao registrado: " + entrega.getAssinante());
            }
            despachar(assinante, entrega);
            entrega.marcarEntregue(agora);
            return true;
        } catch (Exception e) {
            entrega.registrarFalha(e.getClass().getSimpleName() + ": " + e.getMessage(), agora,
                    maxTentativas, backoffBase, backoffTeto);
            LOG.warn("Entrega {} ({} / evento {}) falhou na tentativa {} -> {}: {}", entrega.getId(),
                    entrega.getAssinante(), entrega.getEventoId(), entrega.getTentativas(), entrega.getStatus(),
                    entrega.getUltimoErro());
            return false;
        }
    }

    private <T> void despachar(AssinanteDeEvento<T> assinante, EntregaPendente entrega) throws Exception {
        T evento = json.readValue(entrega.getPayload(), assinante.tipoDeEvento());
        assinante.receber(evento, new Entrega(entrega.getEventoId(), entrega.getTentativas() + 1));
    }

    /** Recuperabilidade: devolve para a fila tudo que esgotou as tentativas. */
    public int reprocessarFalhas() {
        Integer total = transacao.execute(status -> {
            List<EntregaPendente> falhas = repositorio.findByStatus(StatusEntrega.FALHOU_DEFINITIVO);
            falhas.forEach(e -> e.reabrir(Instant.now(relogio)));
            return falhas.size();
        });
        return Objects.requireNonNullElse(total, 0);
    }
}
