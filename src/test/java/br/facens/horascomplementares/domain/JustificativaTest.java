package br.facens.horascomplementares.domain;

import br.facens.horascomplementares.domain.exception.JustificativaInsuficienteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JustificativaTest {

    @Test
    void aceitaTextoComTamanhoMinimo() {
        Justificativa justificativa = new Justificativa("1234567890");

        assertThat(justificativa.texto()).isEqualTo("1234567890");
    }

    @Test
    void aceitaTextoLongo() {
        Justificativa justificativa = new Justificativa("Comprovante ilegivel e sem assinatura do responsavel");

        assertThat(justificativa.texto()).isEqualTo("Comprovante ilegivel e sem assinatura do responsavel");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "curta", "123456789"})
    void rejeitaTextoInsuficiente(String texto) {
        assertThatThrownBy(() -> new Justificativa(texto))
                .isInstanceOf(JustificativaInsuficienteException.class)
                .hasMessageContaining("no minimo 10 caracteres");
    }
}
