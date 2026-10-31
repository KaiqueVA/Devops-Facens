package br.facens.gamificacao.recomendacao.application.port;

import java.util.List;

/** Porta para o assistente RAG (RF04). Fronteira separada da recomendacao: timeout proprio. */
public interface AssistenteEducacional {

    Resposta perguntar(String pergunta);

    record Resposta(String resposta, List<String> fontes) {
    }
}
