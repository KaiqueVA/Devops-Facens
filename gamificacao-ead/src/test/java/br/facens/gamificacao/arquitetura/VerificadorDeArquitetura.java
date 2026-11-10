package br.facens.gamificacao.arquitetura;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Le os fontes de src/main/java e confere as fronteiras declaradas no ADR-001
 * (modulos) e no ADR-002 (dominio sem framework). Usa so a JDK: nao depende de
 * ArchUnit nem de Spring Modulith, entao roda em qualquer maquina da equipe.
 */
final class VerificadorDeArquitetura {

    static final String BASE = "br.facens.gamificacao";
    private static final Pattern PACKAGE = Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE);
    private static final Pattern IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?([\\w.]+)(?:\\.\\*)?;", Pattern.MULTILINE);

    /** classe (FQN) -> lista de imports. */
    static Map<String, List<String>> lerFontes(Path raiz) {
        Map<String, List<String>> resultado = new TreeMap<>();
        try (Stream<Path> arquivos = Files.walk(raiz)) {
            arquivos.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().equals("package-info.java"))
                    .forEach(p -> {
                        String fonte = ler(p);
                        Matcher pkg = PACKAGE.matcher(fonte);
                        String pacote = pkg.find() ? pkg.group(1) : "";
                        String classe = pacote + "." + p.getFileName().toString().replace(".java", "");
                        List<String> imports = new ArrayList<>();
                        Matcher imp = IMPORT.matcher(fonte);
                        while (imp.find()) {
                            imports.add(imp.group(1));
                        }
                        resultado.put(classe, imports);
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return resultado;
    }

    private static String ler(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** "br.facens.gamificacao.ranking.web.X" -> "ranking"; classes na raiz -> "" */
    static String modulo(String fqn) {
        if (!fqn.startsWith(BASE + ".")) {
            return null;
        }
        String resto = fqn.substring(BASE.length() + 1);
        int ponto = resto.indexOf('.');
        return ponto < 0 ? "" : resto.substring(0, ponto);
    }

    /** "br.facens.gamificacao.gamificacao.api.X" -> "api" */
    static String subpacote(String fqn) {
        String resto = fqn.substring(BASE.length() + 1);
        String[] partes = resto.split("\\.");
        return partes.length > 2 ? partes[1] : "";
    }

    /**
     * Regra dos modulos: um modulo so pode importar outro se a dependencia estiver
     * declarada E se o alvo for o subpacote publico {@code api} do outro modulo.
     */
    static List<String> violacoesDeModulo(Map<String, List<String>> fontes,
                                          Map<String, Set<String>> permitidas) {
        List<String> violacoes = new ArrayList<>();
        fontes.forEach((classe, imports) -> {
            String origem = modulo(classe);
            if (origem == null || origem.isEmpty()) {
                return;
            }
            for (String imp : imports) {
                String destino = modulo(imp);
                if (destino == null || destino.isEmpty() || destino.equals(origem)) {
                    continue;
                }
                if (!permitidas.getOrDefault(origem, Set.of()).contains(destino)) {
                    violacoes.add(classe + " -> " + imp + " (dependencia " + origem + " -> " + destino + " nao declarada)");
                } else if (!"api".equals(subpacote(imp))) {
                    violacoes.add(classe + " -> " + imp + " (so o subpacote api de " + destino + " e publico)");
                }
            }
        });
        return violacoes;
    }

    /** Regra do dominio: classes de pacotes informados nao importam nada que comece com os prefixos proibidos. */
    static List<String> violacoesDePureza(Map<String, List<String>> fontes, Set<String> pacotes, Set<String> proibidos) {
        List<String> violacoes = new ArrayList<>();
        fontes.forEach((classe, imports) -> {
            boolean vigiada = pacotes.stream().anyMatch(p -> classe.startsWith(p + "."));
            if (!vigiada) {
                return;
            }
            for (String imp : imports) {
                if (proibidos.stream().anyMatch(imp::startsWith)) {
                    violacoes.add(classe + " -> " + imp);
                }
            }
        });
        return violacoes;
    }

    private VerificadorDeArquitetura() {
    }
}
