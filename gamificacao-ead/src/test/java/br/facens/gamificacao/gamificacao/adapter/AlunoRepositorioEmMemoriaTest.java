package br.facens.gamificacao.gamificacao.adapter;

import br.facens.gamificacao.gamificacao.adapter.memoria.AlunoRepositorioEmMemoria;
import br.facens.gamificacao.gamificacao.application.port.AlunoRepositorio;
import org.junit.jupiter.api.BeforeEach;

/** Contrato da porta rodando no adapter em memoria (sem Spring). */
class AlunoRepositorioEmMemoriaTest extends ContratoAlunoRepositorio {

    private AlunoRepositorio repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new AlunoRepositorioEmMemoria();
    }

    @Override
    protected AlunoRepositorio repositorio() {
        return repositorio;
    }
}
