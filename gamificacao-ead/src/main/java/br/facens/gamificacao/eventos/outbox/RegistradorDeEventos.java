package br.facens.gamificacao.eventos.outbox;

import br.facens.gamificacao.eventos.api.AssinanteDeEvento;
import br.facens.gamificacao.eventos.api.Entrega;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Escuta os eventos publicados pelos modulos e grava uma entrega por assinante no
 * outbox, DENTRO da transacao de quem publicou (listener sincrono): se a operacao
 * de negocio der rollback, o evento some junto; se der commit, o evento esta salvo.
 *
 * <p>Modo "sincrona" (so para o lab do Desafio 4): chama os assinantes na hora,
 * dentro da requisicao, para medir o efeito da alternativa rejeitada.</p>
 */
@Component
public class RegistradorDeEventos {

    private final List<AssinanteDeEvento<?>> assinantes;
    private final EntregaPendenteRepository repositorio;
    private final ObjectMapper json;
    private final Clock relogio;
    private final boolean sincrono;

    public RegistradorDeEventos(List<AssinanteDeEvento<?>> assinantes, EntregaPendenteRepository repositorio,
                                ObjectMapper json, Clock relogioDosEventos,
                                @Value("${eventos.entrega:assincrona}") String modo) {
        this.assinantes = assinantes;
        this.repositorio = repositorio;
        this.json = json;
        this.relogio = relogioDosEventos;
        this.sincrono = "sincrona".equalsIgnoreCase(modo);
    }

    @EventListener
    public void registrar(PayloadApplicationEvent<?> publicado) throws Exception {
        Object evento = publicado.getPayload();
        String eventoId = UUID.randomUUID().toString();
        Instant agora = Instant.now(relogio);
        for (AssinanteDeEvento<?> assinante : assinantes) {
            if (!assinante.tipoDeEvento().isInstance(evento)) {
                continue;
            }
            if (sincrono) {
                entregarAgora(assinante, evento, eventoId);
            } else {
                repositorio.save(new EntregaPendente(eventoId, assinante.nome(), evento.getClass().getName(),
                        serializar(evento), agora));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void entregarAgora(AssinanteDeEvento<T> assinante, Object evento, String eventoId) throws Exception {
        assinante.receber((T) evento, new Entrega(eventoId, 1));
    }

    /** Evento que nao serializa derruba a transacao de negocio: melhor falhar do que perder o evento. */
    private String serializar(Object evento) throws JsonProcessingException {
        return json.writeValueAsString(evento);
    }
}
