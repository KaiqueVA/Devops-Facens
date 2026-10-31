/**
 * Modulo Ranking (RF01).
 *
 * <p>Lado de leitura: monta o top N de alunos direto no banco (paginacao + indice
 * {@code idx_aluno_ranking}). Nao depende de nenhuma classe Java do modulo
 * {@code gamificacao}; le apenas as colunas publicadas da tabela {@code aluno}
 * (ver ADR-001, trade-off "tabela compartilhada").</p>
 */
package br.facens.gamificacao.ranking;
