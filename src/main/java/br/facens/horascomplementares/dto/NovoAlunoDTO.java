package br.facens.horascomplementares.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record NovoAlunoDTO(
        @NotBlank String nome,
        @NotNull LocalDate dataIngresso
) {
}
