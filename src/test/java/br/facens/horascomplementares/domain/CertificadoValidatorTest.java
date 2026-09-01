package br.facens.horascomplementares.domain;

import br.facens.horascomplementares.domain.exception.CargaHorariaInvalidaException;
import br.facens.horascomplementares.domain.exception.DataCertificadoInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificadoValidatorTest {

    private static final LocalDate INGRESSO = LocalDate.of(2023, 1, 1);
    private static final LocalDate HOJE = LocalDate.of(2024, 6, 1);

    private final CertificadoValidator validator = new CertificadoValidator();

    @ParameterizedTest
    @ValueSource(ints = {0, -5, 201, 500})
    void rejeitaCargaHorariaForaDaFaixa(int carga) {
        assertThatThrownBy(() -> validator.validar(carga, LocalDate.of(2024, 3, 10), INGRESSO, HOJE))
                .isInstanceOf(CargaHorariaInvalidaException.class)
                .hasMessageContaining("Carga horaria invalida: " + carga);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 200})
    void aceitaCargaHorariaDentroDaFaixa(int carga) {
        assertThatCode(() -> validator.validar(carga, LocalDate.of(2024, 3, 10), INGRESSO, HOJE))
                .doesNotThrowAnyException();
    }

    @Test
    void rejeitaDataNoFuturo() {
        assertThatThrownBy(() -> validator.validar(40, HOJE.plusDays(1), INGRESSO, HOJE))
                .isInstanceOf(DataCertificadoInvalidaException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void aceitaDataIgualAHoje() {
        assertThatCode(() -> validator.validar(40, HOJE, INGRESSO, HOJE))
                .doesNotThrowAnyException();
    }

    @Test
    void rejeitaDataAnteriorAoIngresso() {
        assertThatThrownBy(() -> validator.validar(40, INGRESSO.minusDays(1), INGRESSO, HOJE))
                .isInstanceOf(DataCertificadoInvalidaException.class)
                .hasMessageContaining("ingresso");
    }

    @Test
    void aceitaDataIgualAoIngresso() {
        assertThatCode(() -> validator.validar(40, INGRESSO, INGRESSO, HOJE))
                .doesNotThrowAnyException();
    }
}
