package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.HistoricoValidacao;
import br.facens.horascomplementares.domain.StatusCertificado;
import br.facens.horascomplementares.domain.exception.CertificadoImutavelException;
import br.facens.horascomplementares.domain.exception.CertificadoInexistenteException;
import br.facens.horascomplementares.domain.exception.JustificativaInsuficienteException;
import br.facens.horascomplementares.repository.CertificadoRepository;
import br.facens.horascomplementares.repository.HistoricoValidacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidacaoServiceTest {

    @Mock
    private CertificadoRepository certificadoRepository;
    @Mock
    private HistoricoValidacaoRepository historicoRepository;

    private final Clock clock =
            Clock.fixed(LocalDate.of(2024, 6, 1).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    private ValidacaoService service;
    private Certificado certificado;

    @BeforeEach
    void setUp() {
        service = new ValidacaoService(certificadoRepository, historicoRepository, clock);
        certificado = new Certificado(
                new Aluno("Bruno", LocalDate.of(2023, 1, 1)),
                new Categoria("ENSINO", 80),
                "Curso de Testes", 30, LocalDate.of(2024, 3, 1));
        lenient().when(certificadoRepository.findById(any())).thenReturn(Optional.of(certificado));
        lenient().when(certificadoRepository.save(any(Certificado.class)))
                .thenAnswer(i -> i.getArgument(0));
        lenient().when(historicoRepository.save(any(HistoricoValidacao.class)))
                .thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void aprovarRegistraHistoricoEMudaStatus() {
        HistoricoValidacao historico = service.aprovar(1L);

        assertThat(certificado.getStatus()).isEqualTo(StatusCertificado.APROVADO);
        assertThat(historico.getResultado()).isEqualTo(StatusCertificado.APROVADO);
        assertThat(historico.getDataHora()).isEqualTo(LocalDateTime.of(2024, 6, 1, 0, 0));
        verify(certificadoRepository).save(certificado);
        verify(historicoRepository).save(any(HistoricoValidacao.class));
    }

    @Test
    void aprovarCertificadoInexistenteLancaExcecao() {
        when(certificadoRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aprovar(99L))
                .isInstanceOf(CertificadoInexistenteException.class);
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void aprovarCertificadoJaAprovadoPropagaImutavel() {
        certificado.aprovar();

        assertThatThrownBy(() -> service.aprovar(1L))
                .isInstanceOf(CertificadoImutavelException.class);
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void reprovarComJustificativaAdequadaRegistraHistorico() {
        HistoricoValidacao historico = service.reprovar(1L, "Comprovante nao corresponde a atividade");

        assertThat(certificado.getStatus()).isEqualTo(StatusCertificado.REPROVADO);
        assertThat(historico.getResultado()).isEqualTo(StatusCertificado.REPROVADO);
        assertThat(historico.getJustificativa()).isEqualTo("Comprovante nao corresponde a atividade");
        verify(certificadoRepository).save(certificado);
    }

    @Test
    void reprovarCertificadoInexistenteLancaExcecao() {
        when(certificadoRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reprovar(99L, "Justificativa suficientemente longa"))
                .isInstanceOf(CertificadoInexistenteException.class);
    }

    @Test
    void reprovarComJustificativaCurtaPropagaExcecaoENaoRegistraHistorico() {
        assertThatThrownBy(() -> service.reprovar(1L, "curta"))
                .isInstanceOf(JustificativaInsuficienteException.class);

        assertThat(certificado.getStatus()).isEqualTo(StatusCertificado.EM_ANALISE);
        verify(historicoRepository, never()).save(any());
    }
}
