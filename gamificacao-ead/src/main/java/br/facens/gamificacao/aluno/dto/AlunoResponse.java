package br.facens.gamificacao.aluno.dto;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.PlanoAssinatura;

/**
 * Representacao do Aluno exposta pela API. Mantem a Entity JPA fora do
 * contrato HTTP, para que uma mudanca no modelo de persistencia nao quebre
 * quem consome o endpoint.
 */
public record AlunoResponse(
        Long id,
        String email,
        PlanoAssinatura plano,
        int cursosDisponiveis,
        int cursosConcluidos,
        int cursosConquistados,
        int moedas,
        int moedasEmCripto,
        int topicosNoForum,
        int comentariosNoForum,
        boolean voucherProjetoReal) {

    public static AlunoResponse from(Aluno aluno) {
        return new AlunoResponse(
                aluno.getId(),
                aluno.getEmail(),
                aluno.getPlano(),
                aluno.getCursosDisponiveis(),
                aluno.getCursosConcluidos(),
                aluno.getCursosConquistados(),
                aluno.getMoedas(),
                aluno.getMoedasEmCripto(),
                aluno.getTopicosNoForum(),
                aluno.getComentariosNoForum(),
                aluno.isVoucherProjetoReal());
    }
}
