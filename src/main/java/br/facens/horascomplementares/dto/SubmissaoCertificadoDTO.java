package br.facens.horascomplementares.dto;

import java.time.LocalDate;

public record SubmissaoCertificadoDTO(
        Long alunoId,
        String categoria,
        String titulo,
        int cargaHoraria,
        LocalDate data
) {
}
