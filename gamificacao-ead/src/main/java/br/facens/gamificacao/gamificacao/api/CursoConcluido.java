package br.facens.gamificacao.gamificacao.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Evento publicado pelo modulo Gamificacao quando um aluno conclui um curso (ADR-004).
 * Faz parte do contrato publico do modulo: notificacao e analytics reagem a ele
 * sem conhecer nenhuma classe interna da gamificacao.
 */
public record CursoConcluido(
        Long alunoId,
        String email,
        String curso,
        BigDecimal media,
        boolean conquistado,
        int cursosLiberados,
        boolean promovidoAPremium,
        String plano,
        int cursosConquistados,
        Instant ocorridoEm) {
}
