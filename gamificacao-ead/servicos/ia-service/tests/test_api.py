from fastapi.testclient import TestClient

from app import main, rag, recomendacao

cliente = TestClient(main.app)


def test_health_informa_modelo_e_base():
    r = cliente.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "UP"
    assert r.json()["trechos_rag"] > 0


def test_recomendacao_respeita_quantidade_e_versao_do_modelo():
    r = cliente.post("/recomendacoes", json={"alunoId": 1, "plano": "BASICO",
                                             "cursosConcluidos": 3, "cursosConquistados": 3, "quantidade": 2})
    assert r.status_code == 200
    corpo = r.json()
    assert corpo["modelo"] == recomendacao.MODELO_VERSAO
    assert len(corpo["recomendacoes"]) == 2


def test_aluno_basico_nao_recebe_curso_premium():
    itens = recomendacao.recomendar("BASICO", 14, 14, quantidade=10)
    assert all(not c["curso"].startswith(("K8S", "LLM")) for c in itens)


def test_aluno_premium_avancado_recebe_nivel_4():
    itens = recomendacao.recomendar("PREMIUM", 14, 14, quantidade=3)
    assert itens[0]["curso"] in {"K8S-401", "LLM-401"}


def test_iniciante_recebe_cursos_introdutorios():
    itens = recomendacao.recomendar("BASICO", 0, 0, quantidade=3)
    assert {c["curso"] for c in itens} <= {"JAVA-101", "GIT-101", "SQL-101"}


def test_validacao_do_contrato():
    r = cliente.post("/recomendacoes", json={"alunoId": 1, "cursosConcluidos": -1, "cursosConquistados": 0})
    assert r.status_code == 422


def test_assistente_responde_com_fonte():
    r = cliente.post("/assistente/perguntas", json={"pergunta": "Como viro Premium?"})
    assert r.status_code == 200
    assert "Premium" in r.json()["resposta"]
    assert r.json()["fontes"][0].startswith("regras-de-gamificacao.md")


def test_assistente_sem_contexto_nao_inventa():
    resposta = rag.responder("receita de bolo de cenoura")
    assert resposta["fontes"] == []
    assert "Nao encontrei" in resposta["resposta"]


def test_metricas_expostas_para_prometheus():
    cliente.post("/recomendacoes", json={"alunoId": 1, "cursosConcluidos": 0, "cursosConquistados": 0})
    r = cliente.get("/metrics/")
    assert r.status_code == 200
    assert "ia_requisicoes_total" in r.text


def test_latencia_de_lab_configuravel():
    assert cliente.put("/lab/latencia?ms=5").json() == {"latencia_ms": 5}
    assert cliente.put("/lab/latencia?ms=0").json() == {"latencia_ms": 0}
