package br.facens.gamificacao.gamificacao.domain;

/** O que a conclusao de um curso causou no agregado (usado para montar o evento CursoConcluido). */
public record ResultadoDaConclusao(boolean conquistado, int cursosLiberados, boolean promovidoAPremium) {
}
