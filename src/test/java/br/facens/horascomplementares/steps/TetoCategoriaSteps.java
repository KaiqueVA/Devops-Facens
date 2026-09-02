package br.facens.horascomplementares.steps;

import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.ContribuicaoCategoria;
import br.facens.horascomplementares.domain.ResumoHoras;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.service.CertificadoService;
import br.facens.horascomplementares.service.HorasService;
import br.facens.horascomplementares.service.ValidacaoService;
import br.facens.horascomplementares.support.ContextoCenario;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class TetoCategoriaSteps {

    @Autowired
    private CertificadoService certificadoService;
    @Autowired
    private ValidacaoService validacaoService;
    @Autowired
    private HorasService horasService;
    @Autowired
    private ContextoCenario contexto;

    private int contador;
    private ResumoHoras resumo;

    private Certificado submeter(int horas, String categoria) {
        contador++;
        return certificadoService.submeter(new SubmissaoCertificadoDTO(
                contexto.getAluno().getId(), categoria,
                "Atividade " + categoria + " #" + contador,
                horas, LocalDate.of(2024, 3, 1).plusDays(contador)));
    }

    @Dado("que o aluno teve {int} horas aprovadas na categoria {string}")
    public void queOAlunoTeveHorasAprovadas(int horas, String categoria) {
        Certificado certificado = submeter(horas, categoria);
        validacaoService.aprovar(certificado.getId());
    }

    @Dado("que o aluno submeteu {int} horas na categoria {string} sem aprovação")
    public void queOAlunoSubmeteuSemAprovacao(int horas, String categoria) {
        submeter(horas, categoria);
    }

    @Quando("o sistema calcula o resumo de horas do aluno")
    public void oSistemaCalculaOResumo() {
        resumo = horasService.resumoDoAluno(contexto.getAluno().getId());
    }

    @Então("o total de horas contabilizadas é {int}")
    public void oTotalDeHorasContabilizadasE(int esperado) {
        assertThat(resumo.totalContabilizado()).isEqualTo(esperado);
    }

    @Então("o total de horas excedentes é {int}")
    public void oTotalDeHorasExcedentesE(int esperado) {
        assertThat(resumo.totalExcedente()).isEqualTo(esperado);
    }

    @Então("a categoria {string} contabiliza {int} horas")
    public void aCategoriaContabiliza(String categoria, int esperado) {
        int contabilizadas = resumo.contribuicoes().stream()
                .filter(c -> c.categoria().equals(categoria))
                .mapToInt(ContribuicaoCategoria::horasContabilizadas)
                .findFirst()
                .orElse(0);
        assertThat(contabilizadas).isEqualTo(esperado);
    }
}
