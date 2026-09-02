package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.ResumoHoras;
import br.facens.horascomplementares.domain.StatusCertificado;
import br.facens.horascomplementares.repository.CertificadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorasServiceTest {

    @Mock
    private CertificadoRepository certificadoRepository;

    private HorasService service;

    @BeforeEach
    void setUp() {
        service = new HorasService(certificadoRepository);
    }

    private Certificado cert(String categoria, int teto, int horas) {
        return new Certificado(
                new Aluno("Carla", LocalDate.of(2023, 1, 1)),
                new Categoria(categoria, teto), "Atividade " + horas, horas,
                LocalDate.of(2024, 3, 1));
    }

    @Test
    void resumoConsideraApenasCertificadosAprovadosDoAluno() {
        when(certificadoRepository.findByAlunoIdAndStatus(eq(7L), eq(StatusCertificado.APROVADO)))
                .thenReturn(List.of(cert("EVENTO", 60, 90), cert("ENSINO", 80, 40)));

        ResumoHoras resumo = service.resumoDoAluno(7L);

        assertThat(resumo.totalContabilizado()).isEqualTo(100);
        assertThat(resumo.totalExcedente()).isEqualTo(30);
    }

    @Test
    void alunoSemCertificadosAprovadosTemResumoZerado() {
        when(certificadoRepository.findByAlunoIdAndStatus(eq(7L), eq(StatusCertificado.APROVADO)))
                .thenReturn(List.of());

        ResumoHoras resumo = service.resumoDoAluno(7L);

        assertThat(resumo.totalContabilizado()).isZero();
        assertThat(resumo.contribuicoes()).isEmpty();
    }
}
