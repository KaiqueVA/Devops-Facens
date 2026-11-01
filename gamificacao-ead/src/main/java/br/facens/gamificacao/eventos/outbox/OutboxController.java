package br.facens.gamificacao.eventos.outbox;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operacao do outbox: ver o que esta pendente e reprocessar o que falhou (ASR-11). */
@RestController
@RequestMapping("/eventos/outbox")
@Tag(name = "Eventos", description = "Outbox de eventos (ADR-004)")
public class OutboxController {

    private final EntregaPendenteRepository repositorio;
    private final RelayDoOutbox relay;

    public OutboxController(EntregaPendenteRepository repositorio, RelayDoOutbox relay) {
        this.repositorio = repositorio;
        this.relay = relay;
    }

    @GetMapping
    @Operation(summary = "Contagem por assinante/status e as 20 entregas mais recentes")
    public Map<String, Object> resumo() {
        Map<String, Map<String, Long>> porAssinante = new LinkedHashMap<>();
        for (Object[] linha : repositorio.contarPorAssinanteEStatus()) {
            porAssinante.computeIfAbsent((String) linha[0], k -> new LinkedHashMap<>())
                    .put(linha[1].toString(), (Long) linha[2]);
        }
        List<Map<String, Object>> recentes = new ArrayList<>();
        for (EntregaPendente e : repositorio.findTop20ByOrderByIdDesc()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", e.getId());
            item.put("eventoId", e.getEventoId());
            item.put("assinante", e.getAssinante());
            item.put("status", e.getStatus());
            item.put("tentativas", e.getTentativas());
            item.put("proximaTentativaEm", e.getProximaTentativaEm());
            item.put("ultimoErro", e.getUltimoErro());
            recentes.add(item);
        }
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("porAssinante", porAssinante);
        resposta.put("recentes", recentes);
        return resposta;
    }

    @PostMapping("/reprocessar")
    @Operation(summary = "Devolve para a fila as entregas em FALHOU_DEFINITIVO")
    public Map<String, Integer> reprocessar() {
        return Map.of("reabertas", relay.reprocessarFalhas());
    }
}
