package br.facens.horascomplementares.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContribuicaoCategoriaTest {

    private final Categoria evento = new Categoria("EVENTO", 60);

    @Test
    void aplicarTetoContabilizaTudoQuandoAbaixoDoLimite() {
        ContribuicaoCategoria contribuicao = ContribuicaoCategoria.aplicarTeto(evento, 45);

        assertThat(contribuicao.categoria()).isEqualTo("EVENTO");
        assertThat(contribuicao.horasEnviadas()).isEqualTo(45);
        assertThat(contribuicao.horasContabilizadas()).isEqualTo(45);
        assertThat(contribuicao.horasExcedentes()).isZero();
    }

    @Test
    void aplicarTetoLimitaAoTetoERegistraExcedente() {
        ContribuicaoCategoria contribuicao = ContribuicaoCategoria.aplicarTeto(evento, 100);

        assertThat(contribuicao.horasContabilizadas()).isEqualTo(60);
        assertThat(contribuicao.horasExcedentes()).isEqualTo(40);
    }
}
