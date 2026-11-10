"""RF03 - Recomendacao de cursos.

"Modelo" v1 baseado em regras ponderadas (o ciclo de vida de ML e o mesmo de um
modelo treinado: versao propria, deploy proprio, sem redeploy do Core).
Substituir por um modelo treinado nao muda o contrato HTTP.
"""
from dataclasses import dataclass

MODELO_VERSAO = "v1-regras-ponderadas"


@dataclass(frozen=True)
class Curso:
    codigo: str
    titulo: str
    nivel: int        # 1 = introdutorio ... 4 = avancado
    trilha: str
    premium: bool = False


CATALOGO = [
    Curso("JAVA-101", "Java Basico", 1, "backend"),
    Curso("GIT-101", "Git e GitHub", 1, "devops"),
    Curso("SQL-101", "SQL Essencial", 1, "dados"),
    Curso("SPRING-201", "Spring Boot", 2, "backend"),
    Curso("DOCKER-201", "Docker na Pratica", 2, "devops"),
    Curso("PY-201", "Python para Dados", 2, "dados"),
    Curso("TEST-202", "Testes Automatizados (TDD/BDD)", 2, "qualidade"),
    Curso("ARQ-301", "Arquitetura de Software", 3, "backend"),
    Curso("CICD-301", "CI/CD com Jenkins", 3, "devops"),
    Curso("ML-301", "Machine Learning Aplicado", 3, "dados"),
    Curso("K8S-401", "Kubernetes", 4, "devops", premium=True),
    Curso("LLM-401", "LLMs e RAG", 4, "dados", premium=True),
]


def nivel_do_aluno(cursos_conquistados: int) -> int:
    if cursos_conquistados >= 12:
        return 4
    if cursos_conquistados >= 6:
        return 3
    if cursos_conquistados >= 2:
        return 2
    return 1


def recomendar(plano: str, cursos_concluidos: int, cursos_conquistados: int, quantidade: int = 3):
    nivel = nivel_do_aluno(cursos_conquistados)
    aproveitamento = cursos_conquistados / cursos_concluidos if cursos_concluidos else 0.0
    # aproveitamento alto empurra para o proximo nivel; baixo consolida o atual
    alvo = nivel + (1 if aproveitamento >= 0.8 and cursos_concluidos >= 2 else 0)

    candidatos = []
    for curso in CATALOGO:
        if curso.premium and plano != "PREMIUM":
            continue
        distancia = abs(curso.nivel - alvo)
        score = round(max(0.0, 1.0 - 0.35 * distancia), 2)
        if score == 0:
            continue
        motivo = (f"nivel {curso.nivel} alinhado ao seu progresso" if distancia == 0
                  else f"nivel {curso.nivel}, proximo do seu nivel atual ({nivel})")
        candidatos.append({"curso": curso.codigo, "titulo": curso.titulo, "trilha": curso.trilha,
                           "score": score, "motivo": motivo})
    candidatos.sort(key=lambda c: (-c["score"], c["curso"]))
    return candidatos[:max(1, min(quantidade, 10))]
# ajuste de versao do modelo
