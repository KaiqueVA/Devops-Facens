package br.facens.gamificacao.iot.application;

import br.facens.gamificacao.iot.api.TipoEventoLaboratorio;
import java.time.Instant;

/** Contrato unico que o iot-gateway entrega ao Core, ja traduzido do formato do dispositivo. */
public record ComandoEventoIot(String eventoId, String laboratorio, String dispositivo, TipoEventoLaboratorio tipo,
                               Long alunoId, String atividade, Instant ocorridoEm) {
}
