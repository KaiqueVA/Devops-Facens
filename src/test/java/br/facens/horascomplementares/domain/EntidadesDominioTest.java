package br.facens.horascomplementares.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercita os construtores e acessores das entidades de domínio.
 */
class EntidadesDominioTest {

    @Test
    void alunoExpoeSeusDados() {
        Aluno aluno = new Aluno("Ana", LocalDate.of(2023, 1, 1));

        assertThat(aluno.getId()).isNull();
        assertThat(aluno.getNome()).isEqualTo("Ana");
        assertThat(aluno.getDataIngresso()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    void categoriaExpoeNomeETeto() {
        Categoria categoria = new Categoria("ENSINO", 80);

        assertThat(categoria.getId()).isNull();
        assertThat(categoria.getNome()).isEqualTo("ENSINO");
        assertThat(categoria.getTetoHoras()).isEqualTo(80);
    }

    @Test
    void certificadoNasceEmAnaliseEExpoeSeusDados() {
        Aluno aluno = new Aluno("Ana", LocalDate.of(2023, 1, 1));
        Categoria categoria = new Categoria("EVENTO", 60);
        LocalDate data = LocalDate.of(2024, 3, 10);

        Certificado certificado = new Certificado(aluno, categoria, "Palestra", 8, data);

        assertThat(certificado.getId()).isNull();
        assertThat(certificado.getAluno()).isSameAs(aluno);
        assertThat(certificado.getCategoria()).isSameAs(categoria);
        assertThat(certificado.getTitulo()).isEqualTo("Palestra");
        assertThat(certificado.getCargaHoraria()).isEqualTo(8);
        assertThat(certificado.getData()).isEqualTo(data);
        assertThat(certificado.getStatus()).isEqualTo(StatusCertificado.EM_ANALISE);
    }
}
