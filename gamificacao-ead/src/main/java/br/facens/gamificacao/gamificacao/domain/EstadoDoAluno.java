package br.facens.gamificacao.gamificacao.domain;

/** Estado completo do agregado Aluno, usado para salvar e reconstituir sem expor setters. */
public record EstadoDoAluno(
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
}
