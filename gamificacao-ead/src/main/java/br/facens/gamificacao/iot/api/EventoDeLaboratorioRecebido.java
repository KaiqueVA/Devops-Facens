package br.facens.gamificacao.iot.api;

import java.time.Instant;

/** Evento publicado pelo modulo IoT quando um evento de laboratorio NOVO e aceito (duplicatas nao publicam). */
public record EventoDeLaboratorioRecebido(String eventoId, String laboratorio, String dispositivo,
                                          TipoEventoLaboratorio tipo, Long alunoId, String atividade,
                                          Instant ocorridoEm) {
}
