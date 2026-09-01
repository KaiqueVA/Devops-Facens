package br.facens.horascomplementares.steps;

import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.StatusCertificado;
import br.facens.horascomplementares.domain.exception.NegocioException;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.repository.CertificadoRepository;
import br.facens.horascomplementares.service.CertificadoService;
import br.facens.horascomplementares.support.ContextoCenario;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SubmissaoCertificadoSteps {

    @Autowired
    private CertificadoRepository certificadoRepository;
    @Autowired
    private CertificadoService certificadoService;
    @Autowired
    private ContextoCenario contexto;

    private Certificado resultado;

    @Dado("que o aluno já submeteu um certificado {string} da categoria {string} com {int} horas na data {string}")
    public void queOAlunoJaSubmeteu(String titulo, String categoria, int horas, String data) {
        certificadoService.submeter(new SubmissaoCertificadoDTO(
                contexto.getAluno().getId(), categoria, titulo, horas, LocalDate.parse(data)));
    }

    @Quando("o aluno submete um certificado {string} da categoria {string} com {int} horas na data {string}")
    public void oAlunoSubmeteUmCertificado(String titulo, String categoria, int horas, String data) {
        resultado = null;
        contexto.setErro(null);
        try {
            resultado = certificadoService.submeter(new SubmissaoCertificadoDTO(
                    contexto.getAluno().getId(), categoria, titulo, horas, LocalDate.parse(data)));
        } catch (NegocioException e) {
            contexto.setErro(e);
        }
    }

    @Então("o certificado é registrado com status {string}")
    public void oCertificadoERegistradoComStatus(String status) {
        assertThat(contexto.getErro()).isNull();
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusCertificado.valueOf(status));
    }

    @Então("o total de horas aprovadas do aluno é {int}")
    public void oTotalDeHorasAprovadasDoAlunoE(int esperado) {
        int total = certificadoRepository
                .findByAlunoIdAndStatus(contexto.getAluno().getId(), StatusCertificado.APROVADO)
                .stream()
                .mapToInt(Certificado::getCargaHoraria)
                .sum();
        assertThat(total).isEqualTo(esperado);
    }

    @Então("a submissão é rejeitada com a mensagem contendo {string}")
    public void aSubmissaoERejeitadaComAMensagemContendo(String trecho) {
        assertThat(contexto.getErro()).as("esperava uma NegocioException").isNotNull();
        assertThat(contexto.getErro().getMessage()).containsIgnoringCase(trecho);
    }
}
