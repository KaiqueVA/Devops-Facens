"""Traducao do payload compacto do dispositivo para o contrato unico do Core (ADR-005).

Payload do dispositivo (economiza bytes no radio):
    {"id": "a1b2c3d4e5f6a7b8", "d": "bancada-03", "t": "F", "a": 1, "ts": 1793480000, "at": "ARQ-LAB-1"}
Topico: lab/<laboratorio>/<dispositivo>/eventos

Contrato do Core (POST /iot/eventos):
    {"eventoId", "laboratorio", "dispositivo", "tipo", "alunoId", "atividade", "ocorridoEm"}
"""
import json
from datetime import datetime, timezone

TIPOS = {"P": "PRESENCA", "I": "INICIO_ATIVIDADE", "F": "FIM_ATIVIDADE"}
OBRIGATORIOS = ("id", "d", "t", "a", "ts")


class EventoInvalido(ValueError):
    """Payload que nunca sera aceito: vai para a DLQ em vez de ser reenviado."""


def partes_do_topico(topico: str):
    partes = topico.split("/")
    if len(partes) != 4 or partes[0] != "lab" or partes[3] != "eventos":
        raise EventoInvalido(f"topico fora do padrao lab/<lab>/<dispositivo>/eventos: {topico}")
    return partes[1], partes[2]


def traduzir(topico: str, payload: bytes) -> dict:
    laboratorio, dispositivo_do_topico = partes_do_topico(topico)
    try:
        bruto = json.loads(payload)
    except (ValueError, UnicodeDecodeError) as e:
        raise EventoInvalido(f"JSON invalido: {e}") from e
    if not isinstance(bruto, dict):
        raise EventoInvalido("payload deve ser um objeto JSON")
    faltando = [c for c in OBRIGATORIOS if c not in bruto]
    if faltando:
        raise EventoInvalido(f"campos obrigatorios ausentes: {faltando}")
    if bruto["t"] not in TIPOS:
        raise EventoInvalido(f"tipo desconhecido: {bruto['t']}")
    if bruto["d"] != dispositivo_do_topico:
        # seguranca de integracao: um dispositivo nao publica em nome de outro
        raise EventoInvalido(f"dispositivo {bruto['d']} publicou no topico de {dispositivo_do_topico}")
    if not isinstance(bruto["a"], int) or not isinstance(bruto["ts"], (int, float)):
        raise EventoInvalido("a (aluno) deve ser inteiro e ts deve ser epoch em segundos")
    return {
        "eventoId": str(bruto["id"]),
        "laboratorio": laboratorio,
        "dispositivo": bruto["d"],
        "tipo": TIPOS[bruto["t"]],
        "alunoId": bruto["a"],
        "atividade": bruto.get("at"),
        "ocorridoEm": datetime.fromtimestamp(bruto["ts"], tz=timezone.utc).isoformat().replace("+00:00", "Z"),
    }
