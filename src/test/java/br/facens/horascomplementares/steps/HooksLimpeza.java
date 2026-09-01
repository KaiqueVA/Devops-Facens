package br.facens.horascomplementares.steps;

import br.facens.horascomplementares.repository.AlunoRepository;
import br.facens.horascomplementares.repository.CertificadoRepository;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Garante isolamento entre cenários: antes de cada cenário remove os dados
 * transacionais (certificados, histórico e alunos). O catálogo de categorias,
 * carregado via data.sql, é preservado.
 */
public class HooksLimpeza {

    @Autowired
    private CertificadoRepository certificadoRepository;
    @Autowired
    private AlunoRepository alunoRepository;

    @Before
    public void limparBaseTransacional() {
        certificadoRepository.deleteAll();
        alunoRepository.deleteAll();
    }
}
