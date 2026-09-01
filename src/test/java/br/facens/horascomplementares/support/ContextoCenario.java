package br.facens.horascomplementares.support;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.exception.NegocioException;
import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

/**
 * Estado compartilhado entre as diferentes classes de step definitions
 * dentro de um mesmo cenário BDD.
 */
@Component
@ScenarioScope
public class ContextoCenario {

    private Aluno aluno;
    private Long certificadoId;
    private NegocioException erro;

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Long getCertificadoId() {
        return certificadoId;
    }

    public void setCertificadoId(Long certificadoId) {
        this.certificadoId = certificadoId;
    }

    public NegocioException getErro() {
        return erro;
    }

    public void setErro(NegocioException erro) {
        this.erro = erro;
    }
}
