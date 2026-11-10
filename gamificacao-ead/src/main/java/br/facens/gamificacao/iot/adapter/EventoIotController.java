package br.facens.gamificacao.iot.adapter;

import br.facens.gamificacao.iot.application.ComandoEventoIot;
import br.facens.gamificacao.iot.application.EventoIotInvalidoException;
import br.facens.gamificacao.iot.application.EventoIotRepository;
import br.facens.gamificacao.iot.application.RecebimentoDeEventoIot;
import br.facens.gamificacao.iot.application.ResultadoRecebimento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint consumido SOMENTE pelo iot-gateway (nunca pelos dispositivos).
 * 201 = novo · 200 = duplicado (ja processado) · 422 = invalido (o gateway manda para a DLQ).
 */
@RestController
@Tag(name = "IoT", description = "RF06 - eventos de laboratorio vindos do iot-gateway")
public class EventoIotController {

    private final RecebimentoDeEventoIot recebimento;
    private final EventoIotRepository repositorio;

    public EventoIotController(RecebimentoDeEventoIot recebimento, EventoIotRepository repositorio) {
        this.recebimento = recebimento;
        this.repositorio = repositorio;
    }

    @PostMapping("/iot/eventos")
    @Operation(summary = "Recebe um evento de laboratorio (idempotente por eventoId)")
    public ResponseEntity<Map<String, String>> receber(@RequestBody ComandoEventoIot comando) {
        ResultadoRecebimento r = recebimento.receber(comando);
        HttpStatus status = r == ResultadoRecebimento.NOVO ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(Map.of("eventoId", comando.eventoId(), "resultado", r.name()));
    }

    @GetMapping("/iot/eventos/resumo")
    @Operation(summary = "Total armazenado + contadores desde o ultimo start (novos, duplicados, invalidos)")
    public Map<String, Long> resumo() {
        return Map.of("armazenados", repositorio.count(), "novos", recebimento.getNovos(),
                "duplicados", recebimento.getDuplicados(), "invalidos", recebimento.getInvalidos());
    }

    @ExceptionHandler(EventoIotInvalidoException.class)
    public ResponseEntity<Map<String, String>> invalido(EventoIotInvalidoException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("erro", e.getMessage()));
    }
}
