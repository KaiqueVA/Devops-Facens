package br.facens.horascomplementares.domain;

import br.facens.horascomplementares.domain.exception.CertificadoImutavelException;
import br.facens.horascomplementares.domain.exception.JustificativaInsuficienteException;
import br.facens.horascomplementares.domain.exception.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificadoTransicaoTest {

    private Certificado emAnalise() {
        return new Certificado(
                new Aluno("Ana", LocalDate.of(2023, 1, 1)),
                new Categoria("ENSINO", 80),
                "Curso de Testes", 30, LocalDate.of(2024, 3, 1));
    }

    @Test
    void aprovarMudaStatusDeEmAnaliseParaAprovado() {
        Certificado c = emAnalise();

        c.aprovar();

        assertThat(c.getStatus()).isEqualTo(StatusCertificado.APROVADO);
    }

    @Test
    void reprovarComJustificativaAdequadaMudaStatusEGuardaMotivo() {
        Certificado c = emAnalise();

        c.reprovar("Carga horaria nao confere com o comprovante");

        assertThat(c.getStatus()).isEqualTo(StatusCertificado.REPROVADO);
        assertThat(c.getJustificativaReprovacao()).isEqualTo("Carga horaria nao confere com o comprovante");
    }

    @Test
    void reprovarComJustificativaDeExatamente10CaracteresEhAceita() {
        Certificado c = emAnalise();

        assertThatCode(() -> c.reprovar("1234567890")).doesNotThrowAnyException();
        assertThat(c.getStatus()).isEqualTo(StatusCertificado.REPROVADO);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "curta", "123456789"})
    void reprovarComJustificativaInsuficienteEhRejeitado(String justificativa) {
        Certificado c = emAnalise();

        assertThatThrownBy(() -> c.reprovar(justificativa))
                .isInstanceOf(JustificativaInsuficienteException.class)
                .hasMessageContaining("no minimo 10");
        assertThat(c.getStatus()).isEqualTo(StatusCertificado.EM_ANALISE);
    }

    @Test
    void aprovarUmCertificadoJaAprovadoLancaImutavel() {
        Certificado c = emAnalise();
        c.aprovar();

        assertThatThrownBy(c::aprovar).isInstanceOf(CertificadoImutavelException.class);
    }

    @Test
    void reprovarUmCertificadoJaAprovadoLancaImutavel() {
        Certificado c = emAnalise();
        c.aprovar();

        assertThatThrownBy(() -> c.reprovar("Justificativa suficientemente longa"))
                .isInstanceOf(CertificadoImutavelException.class);
    }

    @Test
    void aprovarUmCertificadoJaReprovadoLancaTransicaoInvalida() {
        Certificado c = emAnalise();
        c.reprovar("Documento ilegivel e sem assinatura");

        assertThatThrownBy(c::aprovar)
                .isInstanceOf(TransicaoStatusInvalidaException.class)
                .hasMessageContaining("REPROVADO");
    }

    @Test
    void reprovarUmCertificadoJaReprovadoLancaTransicaoInvalida() {
        Certificado c = emAnalise();
        c.reprovar("Documento ilegivel e sem assinatura");

        assertThatThrownBy(() -> c.reprovar("Outra justificativa bem longa aqui"))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }
}
