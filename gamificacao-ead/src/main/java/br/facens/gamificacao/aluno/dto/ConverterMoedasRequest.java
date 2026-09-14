package br.facens.gamificacao.aluno.dto;

import br.facens.gamificacao.aluno.domain.DestinoMoeda;

/** Corpo da requisicao para converter moedas (RN5). */
public record ConverterMoedasRequest(int quantidade, DestinoMoeda destino) {
}
