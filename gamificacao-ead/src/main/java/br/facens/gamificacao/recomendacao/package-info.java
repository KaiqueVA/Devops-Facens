/**
 * Modulo Recomendacao (RF03/RF04): fronteira do Core com o ia-service Python (ADR-003).
 *
 * <p>O Core nunca depende da IA para o fluxo essencial: recomendacao cai para um
 * catalogo padrao (resposta degradada) e o assistente responde 503; concluir
 * curso, ranking e moedas nao passam por aqui.</p>
 */
package br.facens.gamificacao.recomendacao;
