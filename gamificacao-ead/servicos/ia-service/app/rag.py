"""RF04 - Assistente educacional (RAG).

Recuperacao: os documentos de base_conhecimento/ sao quebrados por secao (##) e
pontuados por sobreposicao de termos com a pergunta.
Geracao: por padrao e EXTRATIVA (devolve o trecho mais relevante citando a fonte),
para o lab rodar sem chave de API. Um provedor LLM pode ser plugado em
gerar_resposta() sem mudar o contrato HTTP nem o Core.
"""
import pathlib
import re
import unicodedata

PASTA = pathlib.Path(__file__).resolve().parent.parent / "base_conhecimento"
STOPWORDS = {"a", "o", "e", "de", "da", "do", "das", "dos", "em", "um", "uma", "para", "por", "com",
             "que", "como", "eu", "meu", "minha", "se", "no", "na", "os", "as", "ao", "ou", "qual", "quais",
             "quando", "quanto", "quantos", "posso", "sao", "ser", "tem", "ter", "mais"}


def normalizar(texto: str):
    sem_acento = unicodedata.normalize("NFKD", texto.lower()).encode("ascii", "ignore").decode()
    return [t for t in re.findall(r"[a-z0-9]+", sem_acento) if t not in STOPWORDS and len(t) > 1]


def carregar_trechos(pasta: pathlib.Path = PASTA):
    trechos = []
    for arq in sorted(pasta.glob("*.md")):
        secao, linhas = arq.stem, []
        for linha in arq.read_text(encoding="utf-8").splitlines() + ["## fim"]:
            if linha.startswith("## "):
                if linhas:
                    texto = " ".join(linhas).strip()
                    trechos.append({"fonte": f"{arq.name}#{secao}", "texto": texto,
                                    "termos": set(normalizar(secao + " " + texto))})
                secao, linhas = linha[3:].strip(), []
            elif not linha.startswith("# "):
                linhas.append(linha.strip())
    return trechos


TRECHOS = carregar_trechos()


def recuperar(pergunta: str, k: int = 2):
    termos = set(normalizar(pergunta))
    pontuados = [(len(termos & t["termos"]), t) for t in TRECHOS]
    pontuados = [p for p in pontuados if p[0] > 0]
    pontuados.sort(key=lambda p: -p[0])
    return [t for _, t in pontuados[:k]]


def gerar_resposta(pergunta: str, contexto):
    if not contexto:
        return "Nao encontrei esse assunto na base de conhecimento da plataforma."
    return contexto[0]["texto"]


def responder(pergunta: str):
    contexto = recuperar(pergunta)
    return {"resposta": gerar_resposta(pergunta, contexto), "fontes": [c["fonte"] for c in contexto]}
