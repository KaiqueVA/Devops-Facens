package br.facens.gamificacao.gamificacao.adapter.web.dto;

import br.facens.gamificacao.gamificacao.domain.DestinoMoeda;

/** Corpo da requisicao para converter moedas (RN5). */
public record ConverterMoedasRequest(int quantidade, DestinoMoeda destino) {
}
