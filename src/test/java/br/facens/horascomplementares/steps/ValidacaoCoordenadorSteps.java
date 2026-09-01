package br.facens.horascomplementares.steps;

import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.HistoricoValidacao;
import br.facens.horascomplementares.domain.StatusCertificado;
import br.facens.horascomplementares.domain.exception.NegocioException;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.repository.CertificadoRepository;
import br.facens.horascomplementares.repository.HistoricoValidacaoRepository;
import br.facens.horascomplementares.service.CertificadoService;
import br.facens.horascomplementares.service.ValidacaoService;
import br.facens.horascomplementares.support.ContextoCenario;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidacaoCoordenadorSteps {

    @Autowired
    private CertificadoService certificadoService;
    @Autowired
    private ValidacaoService validacaoService;
    @Autowired
    private CertificadoRepository certificadoRepository;
    @Autowired
    private HistoricoValidacaoRepository historicoRepository;
    @Autowired
    private ContextoCenario contexto;

    @Dado("um certificado {string} da categoria {string} com {int} horas na data {string} em análise")
    public void umCertificadoEmAnalise(String titulo, String categoria, int horas, String data) {
        Certificado certificado = certificadoService.submeter(new SubmissaoCertificadoDTO(
                contexto.getAluno().getId(), categoria, titulo, horas, LocalDate.parse(data)));
        contexto.setCertificadoId(certificado.getId());
    }

    @Dado("que o coordenador já aprovou o certificado")
    public void queOCoordenadorJaAprovou() {
        validacaoService.aprovar(contexto.getCertificadoId());
    }

    @Dado("que o coordenador já reprovou o certificado com a justificativa {string}")
    public void queOCoordenadorJaReprovou(String justificativa) {
        validacaoService.reprovar(contexto.getCertificadoId(), justificativa);
    }

    @Quando("o coordenador aprova o certificado")
    public void oCoordenadorAprovaOCertificado() {
        contexto.setErro(null);
        try {
            validacaoService.aprovar(contexto.getCertificadoId());
        } catch (NegocioException e) {
            contexto.setErro(e);
        }
    }

    @Quando("o coordenador reprova o certificado com a justificativa {string}")
    public void oCoordenadorReprovaOCertificado(String justificativa) {
        contexto.setErro(null);
        try {
            validacaoService.reprovar(contexto.getCertificadoId(), justificativa);
        } catch (NegocioException e) {
            contexto.setErro(e);
        }
    }

    @Então("o certificado fica com status {string}")
    @E("o certificado continua com status {string}")
    public void oCertificadoFicaComStatus(String status) {
        Certificado certificado = certificadoRepository.findById(contexto.getCertificadoId()).orElseThrow();
        assertThat(certificado.getStatus()).isEqualTo(StatusCertificado.valueOf(status));
    }

    @Então("é registrado um histórico de validação com resultado {string}")
    public void eRegistradoUmHistoricoComResultado(String resultado) {
        List<HistoricoValidacao> historico =
                historicoRepository.findByCertificadoIdOrderByDataHoraAsc(contexto.getCertificadoId());
        assertThat(historico).isNotEmpty();
        assertThat(historico.get(historico.size() - 1).getResultado())
                .isEqualTo(StatusCertificado.valueOf(resultado));
    }

    @Então("a validação é rejeitada com a mensagem contendo {string}")
    public void aValidacaoERejeitadaComAMensagemContendo(String trecho) {
        assertThat(contexto.getErro()).as("esperava uma NegocioException").isNotNull();
        assertThat(contexto.getErro().getMessage()).containsIgnoringCase(trecho);
    }
}
