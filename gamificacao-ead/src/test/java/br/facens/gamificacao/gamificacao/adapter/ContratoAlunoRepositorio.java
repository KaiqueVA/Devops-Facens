package br.facens.gamificacao.gamificacao.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.gamificacao.application.port.AlunoRepositorio;
import br.facens.gamificacao.gamificacao.domain.Aluno;
import br.facens.gamificacao.gamificacao.domain.PlanoAssinatura;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de CONTRATO da porta AlunoRepositorio (ADR-002). Os mesmos cenarios rodam
 * contra cada adapter; se os dois passam, trocar um pelo outro nao muda o
 * comportamento observado pelos casos de uso.
 */
public abstract class ContratoAlunoRepositorio {

    protected abstract AlunoRepositorio repositorio();

    private Aluno comForum(String email, int topicos, int comentarios) {
        Aluno aluno = new Aluno(email);
        aluno.participarDoForum(topicos, comentarios);
        return repositorio().salvar(aluno);
    }

    @Test
    @DisplayName("CTR-01 - salvar gera id e preserva todo o estado do agregado")
    void salvarPreservaEstado() {
        Aluno aluno = new Aluno("premium@teste.com");
        for (int i = 1; i <= Aluno.CURSOS_PARA_PREMIUM; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("9.0"));
        }

        Aluno salvo = repositorio().salvar(aluno);
        Aluno lido = repositorio().buscarPorId(salvo.getId()).orElseThrow();

        assertNotNull(salvo.getId());
        assertEquals(aluno.estado().email(), lido.getEmail());
        assertEquals(PlanoAssinatura.PREMIUM, lido.getPlano());
        assertEquals(Aluno.MOEDAS_DA_PROMOCAO, lido.getMoedas());
        assertTrue(lido.isVoucherProjetoReal());
        assertEquals(salvo.estado(), lido.estado());
    }

    @Test
    @DisplayName("CTR-02 - salvar de novo atualiza em vez de duplicar")
    void salvarAtualiza() {
        Aluno salvo = repositorio().salvar(new Aluno("ana@teste.com"));
        salvo.concluirCurso("Java", new BigDecimal("8.0"));

        Aluno atualizado = repositorio().salvar(salvo);

        assertEquals(salvo.getId(), atualizado.getId());
        assertEquals(1, repositorio().buscarPorId(salvo.getId()).orElseThrow().getCursosConquistados());
    }

    @Test
    @DisplayName("CTR-03 - id inexistente devolve vazio")
    void idInexistente() {
        assertTrue(repositorio().buscarPorId(987_654L).isEmpty());
    }

    @Test
    @DisplayName("CTR-04 - destaque do forum segue o CriterioDoForum do dominio")
    void destaqueDoForum() {
        comForum("solo@teste.com", 50, 0);      // nao ajudou ninguem: inelegivel
        comForum("um@teste.com", 4, 20);
        comForum("dois@teste.com", 10, 1);
        Aluno esperado = comForum("tres@teste.com", 10, 9);
        comForum("quatro@teste.com", 10, 9);    // empate total: vence quem chegou antes

        assertEquals(esperado.getId(), repositorio().buscarDestaqueDoForum().orElseThrow().getId());
    }

    @Test
    @DisplayName("CTR-05 - sem elegiveis, nao ha destaque")
    void semElegiveis() {
        comForum("solo@teste.com", 50, 0);

        assertTrue(repositorio().buscarDestaqueDoForum().isEmpty());
    }
}
