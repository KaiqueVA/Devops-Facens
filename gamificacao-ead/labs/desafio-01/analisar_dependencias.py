#!/usr/bin/env python3
"""
Mini-lab do Desafio 1: compara as dependencias entre pacotes ANTES (AC1, camadas)
e DEPOIS (B1, monolito modular) lendo os imports dos fontes Java.

Uso:
  python labs/desafio-01/analisar_dependencias.py <src/main/java> [<overlay>...] [--titulo T] [--dot saida.dot]

Para comparar com a AC1 sem trocar de branch:
  git worktree add ../ac1 main
  python labs/desafio-01/analisar_dependencias.py ../ac1/gamificacao-ead/src/main/java --titulo "Antes (AC1)"
  python labs/desafio-01/analisar_dependencias.py ../ac1/gamificacao-ead/src/main/java \
         labs/desafio-01/alternativa-a-camadas/src/main/java --titulo "Alternativa A"
  python labs/desafio-01/analisar_dependencias.py src/main/java --titulo "Depois (B1)"

Metricas (Robert C. Martin):
  Ce = acoplamento eferente  (de quantos pacotes este pacote depende)
  Ca = acoplamento aferente  (quantos pacotes dependem deste)
  I  = Ce / (Ca + Ce)        (instabilidade: 0 = estavel, 1 = instavel)
"""
import argparse
import collections
import pathlib
import re

BASE = "br.facens.gamificacao"
RE_PKG = re.compile(r"^package\s+([\w.]+);", re.M)
RE_IMP = re.compile(r"^import\s+(?:static\s+)?([\w.]+?)(?:\.\*)?;", re.M)


def pacote_de_import(imp, pacotes):
    # import de classe: remove o ultimo segmento ate achar um pacote conhecido
    partes = imp.split(".")
    while partes:
        candidato = ".".join(partes)
        if candidato in pacotes:
            return candidato
        partes.pop()
    return None


def analisar(raizes):
    fontes = {}
    arquivos = [a for raiz in raizes for a in sorted(pathlib.Path(raiz).rglob("*.java"))]
    for arq in arquivos:  # raizes posteriores sobrescrevem classes de mesmo nome (overlay)
        if arq.name == "package-info.java":
            continue
        texto = arq.read_text(encoding="utf-8")
        m = RE_PKG.search(texto)
        if not m or not m.group(1).startswith(BASE):
            continue
        fontes[(m.group(1), arq.stem)] = RE_IMP.findall(texto)
    pacotes = {p for p, _ in fontes}
    arestas = collections.Counter()
    for (pkg, _), imps in fontes.items():
        for imp in imps:
            alvo = pacote_de_import(imp, pacotes)
            if alvo and alvo != pkg:
                arestas[(pkg, alvo)] += 1
    return fontes, pacotes, arestas


def curto(p):
    return p[len(BASE) + 1:] if p.startswith(BASE + ".") else "(raiz)"


def modulo(p):
    c = curto(p)
    return c.split(".")[0]


def relatorio(raizes, titulo, dot):
    fontes, pacotes, arestas = analisar(raizes)
    classes_por_pacote = collections.Counter(p for p, _ in fontes)
    ce = collections.Counter()
    ca = collections.Counter()
    for (a, b) in arestas:
        ce[a] += 1
        ca[b] += 1
    modulos = sorted({modulo(p) for p in pacotes if curto(p) != "(raiz)"})
    entre_modulos = collections.Counter()
    for (a, b), n in arestas.items():
        if modulo(a) != modulo(b):
            entre_modulos[(modulo(a), modulo(b))] += n

    print(f"## {titulo}\n")
    print(f"- Classes: **{len(fontes)}** | Pacotes: **{len(pacotes)}** | "
          f"Dependencias entre pacotes: **{len(arestas)}** | Modulos de 1o nivel: **{len(modulos)}** ({', '.join(modulos)})")
    print(f"- Dependencias ENTRE modulos de 1o nivel: **{len(entre_modulos)}**\n")
    print("| Pacote | Classes | Ce | Ca | I |")
    print("|---|---:|---:|---:|---:|")
    for p in sorted(pacotes):
        total = ce[p] + ca[p]
        inst = f"{ce[p] / total:.2f}" if total else "-"
        print(f"| `{curto(p)}` | {classes_por_pacote[p]} | {ce[p]} | {ca[p]} | {inst} |")
    print("\n| De | Para | Imports |")
    print("|---|---|---:|")
    for (a, b), n in sorted(arestas.items()):
        print(f"| `{curto(a)}` | `{curto(b)}` | {n} |")
    if entre_modulos:
        print("\nDependencias entre modulos:")
        for (a, b), n in sorted(entre_modulos.items()):
            print(f"- {a} -> {b} ({n} imports)")
    print()

    if dot:
        linhas = ["digraph G {", '  rankdir=LR; node [shape=box, style="rounded,filled", fillcolor="#e8f0fe", fontname="Helvetica"];',
                  f'  label="{titulo}"; labelloc=t; fontname="Helvetica";']
        por_modulo = collections.defaultdict(list)
        for p in pacotes:
            por_modulo[modulo(p)].append(p)
        for i, (m, ps) in enumerate(sorted(por_modulo.items())):
            linhas.append(f'  subgraph cluster_{i} {{ label="{m}"; style=dashed;')
            for p in sorted(ps):
                linhas.append(f'    "{curto(p)}" [label="{curto(p)}\\n{classes_por_pacote[p]} classe(s)"];')
            linhas.append("  }")
        for (a, b), n in sorted(arestas.items()):
            cor = "red" if modulo(a) != modulo(b) else "gray30"
            linhas.append(f'  "{curto(a)}" -> "{curto(b)}" [label="{n}", color="{cor}"];')
        linhas.append("}")
        pathlib.Path(dot).write_text("\n".join(linhas), encoding="utf-8")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("raizes", nargs="+", help="uma ou mais pastas src/main/java (a ultima sobrepoe as anteriores)")
    ap.add_argument("--titulo", default="Dependencias")
    ap.add_argument("--dot")
    a = ap.parse_args()
    relatorio(a.raizes, a.titulo, a.dot)
