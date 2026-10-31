package br.facens.gamificacao.arquitetura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Evidencia automatica do ADR-001: as fronteiras do Monolito Modular sao
 * verificadas a cada build. Se alguem importar a classe interna de outro modulo,
 * o build quebra (e o Jenkins bloqueia o deploy, ADR-006).
 */
class ArquiteturaModularTest {

    /** Unica fonte da verdade das dependencias entre modulos (espelha o C4 Component). */
    static final Map<String, Set<String>> DEPENDENCIAS_PERMITIDAS = Map.of(
            "gamificacao", Set.of(),
            "ranking", Set.of(),
            "recomendacao", Set.of("gamificacao"),
            "eventos", Set.of(),
            "notificacao", Set.of("gamificacao", "eventos"),
            "analytics", Set.of("gamificacao", "eventos", "iot"),
            "iot", Set.of("gamificacao"));

    private static final Path FONTES = Path.of("src/main/java/br/facens/gamificacao");

    @Test
    @DisplayName("ARQ-01 - modulos so dependem do contrato publico (api) de modulos declarados")
    void modulosRespeitamFronteiras() {
        Map<String, List<String>> fontes = VerificadorDeArquitetura.lerFontes(FONTES);
        List<String> violacoes = VerificadorDeArquitetura.violacoesDeModulo(fontes, DEPENDENCIAS_PERMITIDAS);
        assertTrue(violacoes.isEmpty(), "Violacoes de fronteira:\n" + String.join("\n", violacoes));
    }

    @Test
    @DisplayName("ARQ-02 - todo pacote de primeiro nivel e um modulo declarado")
    void todoModuloEstaDeclarado() {
        Map<String, List<String>> fontes = VerificadorDeArquitetura.lerFontes(FONTES);
        fontes.keySet().stream()
                .map(VerificadorDeArquitetura::modulo)
                .filter(m -> !m.isEmpty())
                .forEach(m -> assertTrue(DEPENDENCIAS_PERMITIDAS.containsKey(m),
                        "Modulo nao declarado no ADR: " + m));
    }

    @Test
    @DisplayName("ARQ-04 - dominio da gamificacao nao conhece framework, banco nem adapters (ADR-002)")
    void dominioPuro() {
        Map<String, List<String>> fontes = VerificadorDeArquitetura.lerFontes(FONTES);
        List<String> violacoes = VerificadorDeArquitetura.violacoesDePureza(fontes,
                Set.of("br.facens.gamificacao.gamificacao.domain"),
                Set.of("org.springframework", "jakarta.", "com.fasterxml", "io.swagger",
                        "br.facens.gamificacao.gamificacao.adapter", "br.facens.gamificacao.gamificacao.application"));
        assertTrue(violacoes.isEmpty(), "Dominio acoplado:\n" + String.join("\n", violacoes));
    }

    @Test
    @DisplayName("ARQ-05 - casos de uso dependem de portas, nunca de adapters ou de JPA (ADR-002)")
    void aplicacaoDependeDePortas() {
        Map<String, List<String>> fontes = VerificadorDeArquitetura.lerFontes(FONTES);
        List<String> violacoes = VerificadorDeArquitetura.violacoesDePureza(fontes,
                Set.of("br.facens.gamificacao.gamificacao.application"),
                Set.of("br.facens.gamificacao.gamificacao.adapter", "jakarta.persistence",
                        "org.springframework.data", "org.springframework.web"));
        assertTrue(violacoes.isEmpty(), "Caso de uso acoplado a adapter:\n" + String.join("\n", violacoes));
    }

    @Test
    @DisplayName("ARQ-03 - o verificador detecta uma violacao (prova de que a regra funciona)")
    void verificadorDetectaViolacao() {
        Map<String, List<String>> fontesFalsas = Map.of(
                "br.facens.gamificacao.ranking.web.Intrusa",
                List.of("br.facens.gamificacao.gamificacao.adapter.persistence.AlunoRepositorioJpa"));

        List<String> violacoes = VerificadorDeArquitetura.violacoesDeModulo(fontesFalsas, DEPENDENCIAS_PERMITIDAS);

        assertEquals(1, violacoes.size());
        assertTrue(violacoes.get(0).contains("ranking -> gamificacao"));
    }
}
