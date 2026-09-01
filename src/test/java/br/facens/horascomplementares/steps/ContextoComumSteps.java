package br.facens.horascomplementares.steps;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.repository.AlunoRepository;
import br.facens.horascomplementares.repository.CategoriaRepository;
import br.facens.horascomplementares.support.ContextoCenario;
import br.facens.horascomplementares.support.RelogioAjustavel;
import io.cucumber.java.pt.Dado;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps de background reutilizados por todas as features.
 */
public class ContextoComumSteps {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private RelogioAjustavel relogio;
    @Autowired
    private ContextoCenario contexto;

    @Dado("o catálogo de categorias padrão da faculdade")
    public void oCatalogoDeCategoriasPadrao() {
        assertThat(categoriaRepository.findByNome("ENSINO")).isPresent();
        assertThat(categoriaRepository.findByNome("PESQUISA")).isPresent();
        assertThat(categoriaRepository.findByNome("EXTENSAO")).isPresent();
        assertThat(categoriaRepository.findByNome("EVENTO")).isPresent();
    }

    @Dado("um aluno chamado {string} que ingressou em {string}")
    public void umAlunoQueIngressouEm(String nome, String dataIngresso) {
        contexto.setAluno(alunoRepository.save(new Aluno(nome, LocalDate.parse(dataIngresso))));
    }

    @Dado("que a data de hoje é {string}")
    public void queADataDeHojeE(String data) {
        relogio.definirData(LocalDate.parse(data));
    }
}
