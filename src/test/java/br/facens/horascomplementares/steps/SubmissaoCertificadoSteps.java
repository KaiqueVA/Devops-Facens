package br.facens.horascomplementares.steps;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.StatusCertificado;
import br.facens.horascomplementares.domain.exception.NegocioException;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.repository.AlunoRepository;
import br.facens.horascomplementares.repository.CategoriaRepository;
import br.facens.horascomplementares.repository.CertificadoRepository;
import br.facens.horascomplementares.service.CertificadoService;
import br.facens.horascomplementares.support.RelogioAjustavel;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SubmissaoCertificadoSteps {

    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private CertificadoRepository certificadoRepository;
    @Autowired
    private CertificadoService certificadoService;
    @Autowired
    private RelogioAjustavel relogio;

    private Aluno aluno;
    private Certificado resultado;
    private NegocioException erro;

    @Dado("o catálogo de categorias padrão da faculdade")
    public void oCatalogoDeCategoriasPadrao() {
        assertThat(categoriaRepository.findByNome("ENSINO")).isPresent();
        assertThat(categoriaRepository.findByNome("EVENTO")).isPresent();
    }

    @Dado("um aluno chamado {string} que ingressou em {string}")
    public void umAlunoQueIngressouEm(String nome, String dataIngresso) {
        aluno = alunoRepository.save(new Aluno(nome, LocalDate.parse(dataIngresso)));
    }

    @Dado("que a data de hoje é {string}")
    public void queADataDeHojeE(String data) {
        relogio.definirData(LocalDate.parse(data));
    }

    @Dado("que o aluno já submeteu um certificado {string} da categoria {string} com {int} horas na data {string}")
    public void queOAlunoJaSubmeteu(String titulo, String categoria, int horas, String data) {
        certificadoService.submeter(new SubmissaoCertificadoDTO(
                aluno.getId(), categoria, titulo, horas, LocalDate.parse(data)));
    }

    @Quando("o aluno submete um certificado {string} da categoria {string} com {int} horas na data {string}")
    public void oAlunoSubmeteUmCertificado(String titulo, String categoria, int horas, String data) {
        resultado = null;
        erro = null;
        try {
            resultado = certificadoService.submeter(new SubmissaoCertificadoDTO(
                    aluno.getId(), categoria, titulo, horas, LocalDate.parse(data)));
        } catch (NegocioException e) {
            erro = e;
        }
    }

    @Então("o certificado é registrado com status {string}")
    public void oCertificadoERegistradoComStatus(String status) {
        assertThat(erro).isNull();
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusCertificado.valueOf(status));
    }

    @Então("o total de horas aprovadas do aluno é {int}")
    public void oTotalDeHorasAprovadasDoAlunoE(int esperado) {
        int total = certificadoRepository
                .findByAlunoIdAndStatus(aluno.getId(), StatusCertificado.APROVADO)
                .stream()
                .mapToInt(Certificado::getCargaHoraria)
                .sum();
        assertThat(total).isEqualTo(esperado);
    }

    @Então("a submissão é rejeitada com a mensagem contendo {string}")
    public void aSubmissaoERejeitadaComAMensagemContendo(String trecho) {
        assertThat(erro).as("esperava uma NegocioException").isNotNull();
        assertThat(erro.getMessage()).containsIgnoringCase(trecho);
    }
}
