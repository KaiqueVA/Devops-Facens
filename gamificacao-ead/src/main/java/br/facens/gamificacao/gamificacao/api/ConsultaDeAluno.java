package br.facens.gamificacao.gamificacao.api;

import java.util.Optional;

/**
 * Contrato publico do modulo Gamificacao (ADR-001: so o subpacote api e visivel).
 * Outros modulos consultam o aluno por aqui, nunca pela entidade ou pelo repositorio.
 */
public interface ConsultaDeAluno {

    Optional<ResumoDoAluno> buscarResumo(Long alunoId);
}
