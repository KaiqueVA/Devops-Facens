package br.facens.gamificacao.gamificacao.adapter;

import br.facens.gamificacao.gamificacao.adapter.persistence.AlunoRepositorioJpa;
import br.facens.gamificacao.gamificacao.application.port.AlunoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/** O MESMO contrato rodando no adapter JPA, com H2 real. */
@DataJpaTest
@Import(AlunoRepositorioJpa.class)
class AlunoRepositorioJpaTest extends ContratoAlunoRepositorio {

    @Autowired
    private AlunoRepositorioJpa repositorio;

    @Override
    protected AlunoRepositorio repositorio() {
        return repositorio;
    }
}
