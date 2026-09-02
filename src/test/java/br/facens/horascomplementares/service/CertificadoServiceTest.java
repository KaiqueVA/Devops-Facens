package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.StatusCertificado;
import br.facens.horascomplementares.domain.exception.AlunoInexistenteException;
import br.facens.horascomplementares.domain.exception.CargaHorariaInvalidaException;
import br.facens.horascomplementares.domain.exception.CategoriaInexistenteException;
import br.facens.horascomplementares.domain.exception.CertificadoDuplicadoException;
import br.facens.horascomplementares.domain.exception.DataCertificadoInvalidaException;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.repository.AlunoRepository;
import br.facens.horascomplementares.repository.CategoriaRepository;
import br.facens.horascomplementares.repository.CertificadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificadoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2024, 6, 1);
    private static final LocalDate INGRESSO = LocalDate.of(2023, 1, 1);

    @Mock
    private CertificadoRepository certificadoRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    private final Clock clock = Clock.fixed(HOJE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    private CertificadoService service;

    private Aluno aluno;

    @BeforeEach
    void setUp() {
        service = new CertificadoService(certificadoRepository, alunoRepository, categoriaRepository, clock);
        aluno = new Aluno("Ana", INGRESSO);
        lenient().when(alunoRepository.findById(any())).thenReturn(Optional.of(aluno));
        lenient().when(categoriaRepository.findByNome("ENSINO")).thenReturn(Optional.of(new Categoria("ENSINO", 80)));
        lenient().when(certificadoRepository.existsByAlunoIdAndTituloIgnoreCaseAndData(any(), anyString(), any()))
                .thenReturn(false);
        lenient().when(certificadoRepository.save(any(Certificado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private SubmissaoCertificadoDTO dto(String titulo, String categoria, int horas, LocalDate data) {
        return new SubmissaoCertificadoDTO(1L, categoria, titulo, horas, data);
    }

    @Test
    void certificadoValidoNasceEmAnaliseESalva() {
        Certificado c = service.submeter(dto("Curso de Java", "ENSINO", 40, LocalDate.of(2024, 3, 10)));

        assertThat(c.getStatus()).isEqualTo(StatusCertificado.EM_ANALISE);
        assertThat(c.getCargaHoraria()).isEqualTo(40);
        assertThat(c.getTitulo()).isEqualTo("Curso de Java");
        verify(certificadoRepository).save(any(Certificado.class));
    }

    @Test
    void cargaHorariaZeroEhRejeitada() {
        assertThatThrownBy(() -> service.submeter(dto("X", "ENSINO", 0, LocalDate.of(2024, 3, 10))))
                .isInstanceOf(CargaHorariaInvalidaException.class)
                .hasMessageContaining("Carga horaria invalida");
        verify(certificadoRepository, never()).save(any());
    }

    @Test
    void cargaHorariaNegativaEhRejeitada() {
        assertThatThrownBy(() -> service.submeter(dto("X", "ENSINO", -5, LocalDate.of(2024, 3, 10))))
                .isInstanceOf(CargaHorariaInvalidaException.class);
    }

    @Test
    void cargaHorariaAcimaDe200EhRejeitada() {
        assertThatThrownBy(() -> service.submeter(dto("X", "ENSINO", 201, LocalDate.of(2024, 3, 10))))
                .isInstanceOf(CargaHorariaInvalidaException.class);
    }

    @Test
    void cargaHorariaDe200EhAceita() {
        Certificado c = service.submeter(dto("X", "ENSINO", 200, LocalDate.of(2024, 3, 10)));
        assertThat(c.getCargaHoraria()).isEqualTo(200);
    }

    @Test
    void cargaHorariaDe1EhAceita() {
        Certificado c = service.submeter(dto("X", "ENSINO", 1, LocalDate.of(2024, 3, 10)));
        assertThat(c.getCargaHoraria()).isEqualTo(1);
    }

    @Test
    void dataFuturaEhRejeitada() {
        assertThatThrownBy(() -> service.submeter(dto("X", "ENSINO", 10, HOJE.plusDays(1))))
                .isInstanceOf(DataCertificadoInvalidaException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void dataIgualAHojeEhAceita() {
        Certificado c = service.submeter(dto("X", "ENSINO", 10, HOJE));
        assertThat(c).isNotNull();
    }

    @Test
    void dataAnteriorAoIngressoEhRejeitada() {
        assertThatThrownBy(() -> service.submeter(dto("X", "ENSINO", 10, INGRESSO.minusDays(1))))
                .isInstanceOf(DataCertificadoInvalidaException.class)
                .hasMessageContaining("ingresso");
    }

    @Test
    void dataIgualAoIngressoEhAceita() {
        Certificado c = service.submeter(dto("X", "ENSINO", 10, INGRESSO));
        assertThat(c).isNotNull();
    }

    @Test
    void certificadoDuplicadoEhRejeitado() {
        when(certificadoRepository.existsByAlunoIdAndTituloIgnoreCaseAndData(any(), eq("Curso de Java"), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.submeter(dto("Curso de Java", "ENSINO", 40, LocalDate.of(2024, 3, 10))))
                .isInstanceOf(CertificadoDuplicadoException.class)
                .hasMessageContaining("duplicado");
    }

    @Test
    void categoriaForaDoCatalogoEhRejeitada() {
        when(categoriaRepository.findByNome("MONITORIA_EXTRA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submeter(dto("X", "MONITORIA_EXTRA", 10, LocalDate.of(2024, 3, 10))))
                .isInstanceOf(CategoriaInexistenteException.class)
                .hasMessageContaining("Categoria");
    }

    @Test
    void alunoInexistenteEhRejeitado() {
        when(alunoRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submeter(dto("X", "ENSINO", 10, LocalDate.of(2024, 3, 10))))
                .isInstanceOf(AlunoInexistenteException.class);
    }

    @Test
    void listarPorAlunoDelegaAoRepositorio() {
        Certificado certificado = new Certificado(aluno, new Categoria("ENSINO", 80),
                "Curso", 10, LocalDate.of(2024, 3, 10));
        when(certificadoRepository.findByAlunoIdOrderByDataAsc(1L)).thenReturn(java.util.List.of(certificado));

        assertThat(service.listarPorAluno(1L)).containsExactly(certificado);
    }
}
