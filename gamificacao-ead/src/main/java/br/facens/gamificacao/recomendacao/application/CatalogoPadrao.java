package br.facens.gamificacao.recomendacao.application;

import br.facens.gamificacao.recomendacao.application.port.CursoRecomendado;
import java.util.List;

/** Comportamento degradado aceitavel: trilha introdutoria fixa, igual para todos. */
final class CatalogoPadrao {

    static final String VERSAO = "catalogo-padrao";

    static final List<CursoRecomendado> CURSOS = List.of(
            new CursoRecomendado("JAVA-101", "Java Basico", 0.0, "sugestao padrao (IA indisponivel)"),
            new CursoRecomendado("GIT-101", "Git e GitHub", 0.0, "sugestao padrao (IA indisponivel)"),
            new CursoRecomendado("SQL-101", "SQL Essencial", 0.0, "sugestao padrao (IA indisponivel)"));

    private CatalogoPadrao() {
    }
}
