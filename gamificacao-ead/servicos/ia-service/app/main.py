"""ia-service: fronteira de IA da plataforma (ADR-003).

Endpoints:
  POST /recomendacoes          RF03 (ML)
  POST /assistente/perguntas   RF04 (RAG)
  GET  /health                 liveness/readiness
  GET  /metrics                Prometheus (ADR-006)
  PUT  /lab/latencia?ms=N      so para o lab: simula IA lenta sem reiniciar
"""
import asyncio
import os
import time

from fastapi import FastAPI, Request
from prometheus_client import Counter, Histogram, make_asgi_app
from pydantic import BaseModel, Field

from app import rag, recomendacao

app = FastAPI(title="ia-service", version=recomendacao.MODELO_VERSAO)
app.mount("/metrics", make_asgi_app())

REQUISICOES = Counter("ia_requisicoes_total", "Requisicoes atendidas pelo ia-service", ["endpoint", "status"])
LATENCIA = Histogram("ia_latencia_segundos", "Latencia das requisicoes do ia-service", ["endpoint"],
                     buckets=(0.05, 0.1, 0.25, 0.5, 0.8, 1, 2, 3, 5))

estado = {"latencia_ms": int(os.getenv("IA_LATENCIA_MS", "0"))}


class PedidoRecomendacao(BaseModel):
    alunoId: int
    plano: str = "BASICO"
    cursosConcluidos: int = Field(ge=0)
    cursosConquistados: int = Field(ge=0)
    quantidade: int = Field(default=3, ge=1, le=10)


class Pergunta(BaseModel):
    pergunta: str = Field(min_length=3, max_length=500)


@app.middleware("http")
async def medir(request: Request, call_next):
    if request.url.path.startswith("/metrics"):
        return await call_next(request)
    inicio = time.perf_counter()
    if estado["latencia_ms"] and request.url.path in ("/recomendacoes", "/assistente/perguntas"):
        await asyncio.sleep(estado["latencia_ms"] / 1000)  # simula modelo lento (o /health continua respondendo)
    resposta = await call_next(request)
    LATENCIA.labels(request.url.path).observe(time.perf_counter() - inicio)
    REQUISICOES.labels(request.url.path, str(resposta.status_code)).inc()
    return resposta


@app.get("/health")
def health():
    return {"status": "UP", "modelo": recomendacao.MODELO_VERSAO, "trechos_rag": len(rag.TRECHOS)}


@app.post("/recomendacoes")
def recomendar(pedido: PedidoRecomendacao):
    itens = recomendacao.recomendar(pedido.plano, pedido.cursosConcluidos, pedido.cursosConquistados,
                                    pedido.quantidade)
    return {"modelo": recomendacao.MODELO_VERSAO, "alunoId": pedido.alunoId, "recomendacoes": itens}


@app.post("/assistente/perguntas")
def perguntar(pergunta: Pergunta):
    return rag.responder(pergunta.pergunta)


@app.put("/lab/latencia")
def definir_latencia(ms: int = 0):
    estado["latencia_ms"] = max(0, ms)
    return {"latencia_ms": estado["latencia_ms"]}
