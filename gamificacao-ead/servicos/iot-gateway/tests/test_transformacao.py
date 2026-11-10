import json

import pytest

import transformacao as t

TOPICO = "lab/LAB-ARQ/bancada-03/eventos"


def payload(**kw):
    base = {"id": "abc123", "d": "bancada-03", "t": "F", "a": 1, "ts": 1793480000, "at": "ARQ-LAB-1"}
    base.update(kw)
    return json.dumps(base).encode()


def test_traduz_payload_compacto_para_contrato_do_core():
    corpo = t.traduzir(TOPICO, payload())
    assert corpo == {"eventoId": "abc123", "laboratorio": "LAB-ARQ", "dispositivo": "bancada-03",
                     "tipo": "FIM_ATIVIDADE", "alunoId": 1, "atividade": "ARQ-LAB-1",
                     "ocorridoEm": "2026-10-31T20:53:20Z"}


def test_payload_compacto_e_pequeno():
    assert len(payload()) < 100  # o JSON do contrato do Core tem mais que o dobro


@pytest.mark.parametrize("ruim, motivo", [
    (b"{nao e json", "JSON invalido"),
    (b"[1,2]", "objeto JSON"),
    (json.dumps({"id": "x"}).encode(), "ausentes"),
    (payload(t="X"), "tipo desconhecido"),
    (payload(d="bancada-99"), "publicou no topico"),
    (payload(a="um"), "inteiro"),
])
def test_payloads_invalidos(ruim, motivo):
    with pytest.raises(t.EventoInvalido, match=motivo):
        t.traduzir(TOPICO, ruim)


def test_topico_fora_do_padrao():
    with pytest.raises(t.EventoInvalido, match="topico"):
        t.traduzir("outro/topico", payload())
