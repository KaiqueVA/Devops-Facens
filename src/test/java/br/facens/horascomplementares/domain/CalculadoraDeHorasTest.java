package br.facens.horascomplementares.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CalculadoraDeHorasTest {

    private final CalculadoraDeHoras calculadora = new CalculadoraDeHoras();

    private static final Categoria ENSINO = new Categoria("ENSINO", 80);
    private static final Categoria PESQUISA = new Categoria("PESQUISA", 80);
    private static final Categoria EXTENSAO = new Categoria("EXTENSAO", 80);
    private static final Categoria EVENTO = new Categoria("EVENTO", 60);

    private Certificado cert(Categoria categoria, int horas) {
        return new Certificado(
                new Aluno("Carla", LocalDate.of(2023, 1, 1)),
                categoria, "Atividade " + categoria.getNome() + " " + horas, horas,
                LocalDate.of(2024, 3, 1));
    }

    @Test
    void semCertificadosOResumoEhZerado() {
        ResumoHoras resumo = calculadora.calcular(List.of());

        assertThat(resumo.totalContabilizado()).isZero();
        assertThat(resumo.totalExcedente()).isZero();
        assertThat(resumo.contribuicoes()).isEmpty();
    }

    @Test
    void horasDentroDoTetoSaoContabilizadasIntegralmente() {
        ResumoHoras resumo = calculadora.calcular(List.of(cert(ENSINO, 50)));

        assertThat(resumo.totalContabilizado()).isEqualTo(50);
        assertThat(resumo.totalExcedente()).isZero();
        assertThat(resumo.contribuicoes())
                .extracting(ContribuicaoCategoria::categoria,
                        ContribuicaoCategoria::horasEnviadas,
                        ContribuicaoCategoria::horasContabilizadas,
                        ContribuicaoCategoria::horasExcedentes)
                .containsExactly(tuple("ENSINO", 50, 50, 0));
    }

    @Test
    void horasExatamenteNoTetoNaoGeramExcedente() {
        ResumoHoras resumo = calculadora.calcular(List.of(cert(ENSINO, 80)));

        assertThat(resumo.totalContabilizado()).isEqualTo(80);
        assertThat(resumo.totalExcedente()).isZero();
    }

    @Test
    void horasAcimaDoTetoDaCategoriaViramExcedente() {
        ResumoHoras resumo = calculadora.calcular(List.of(cert(EVENTO, 90)));

        assertThat(resumo.totalContabilizado()).isEqualTo(60);
        assertThat(resumo.totalExcedente()).isEqualTo(30);
    }

    @Test
    void certificadosDaMesmaCategoriaSaoSomadosAntesDoTeto() {
        ResumoHoras resumo = calculadora.calcular(List.of(cert(ENSINO, 50), cert(ENSINO, 50)));

        assertThat(resumo.totalContabilizado()).isEqualTo(80);
        assertThat(resumo.totalExcedente()).isEqualTo(20);
    }

    @Test
    void totalGeralNuncaPassaDe200() {
        ResumoHoras resumo = calculadora.calcular(List.of(
                cert(ENSINO, 80), cert(PESQUISA, 80), cert(EXTENSAO, 80)));

        assertThat(resumo.totalContabilizado()).isEqualTo(200);
        assertThat(resumo.totalExcedente()).isEqualTo(40);
    }

    @Test
    void somaDeCategoriasAbaixoDe200NaoGeraExcedente() {
        ResumoHoras resumo = calculadora.calcular(List.of(cert(ENSINO, 60), cert(EVENTO, 30)));

        assertThat(resumo.totalContabilizado()).isEqualTo(90);
        assertThat(resumo.totalExcedente()).isZero();
    }
}
