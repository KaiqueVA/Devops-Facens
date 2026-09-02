package br.facens.horascomplementares.domain;

import br.facens.horascomplementares.domain.exception.JustificativaInsuficienteException;

/**
 * Objeto de valor que representa a justificativa de uma reprovação.
 * Garante, na construção, a regra de tamanho mínimo. Domínio puro.
 */
public final class Justificativa {

    public static final int TAMANHO_MINIMO = 10;

    private final String texto;

    public Justificativa(String texto) {
        if (texto == null || texto.trim().length() < TAMANHO_MINIMO) {
            throw new JustificativaInsuficienteException(TAMANHO_MINIMO);
        }
        this.texto = texto;
    }

    public String texto() {
        return texto;
    }
}
