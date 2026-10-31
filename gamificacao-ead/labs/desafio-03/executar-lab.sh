#!/usr/bin/env bash
# Desafio 3 - Spring Boot consumindo FastAPI; interromper o servico Python e observar a degradacao.
#
# Pre-requisito: Core em http://localhost:8080 e ia-service em http://localhost:8000
#   docker compose up -d app ia-service         (ou: mvn spring-boot:run  +  uvicorn app.main:app --port 8000)
# Uso: bash labs/desafio-03/executar-lab.sh
# Para parar/subir a IA o script usa docker compose; troque com IA_PARAR / IA_SUBIR se rodar fora do Docker.
set -uo pipefail
CORE="${CORE:-http://localhost:8080}"
IA="${IA:-http://localhost:8000}"
IA_PARAR="${IA_PARAR:-docker compose stop ia-service}"
IA_SUBIR="${IA_SUBIR:-docker compose start ia-service}"

chamar() { # metodo caminho [json]
  local saida
  if [ -n "${3:-}" ]; then
    saida=$(curl -s -o /tmp/lab3.json -w "%{http_code} %{time_total}" -X "$1" "$CORE$2" -H 'Content-Type: application/json' -d "$3")
  else
    saida=$(curl -s -o /tmp/lab3.json -w "%{http_code} %{time_total}" -X "$1" "$CORE$2")
  fi
  echo "\$ $1 $2  ->  HTTP ${saida% *}  em ${saida#* }s"
  cut -c1-260 /tmp/lab3.json; echo
}
passo() { echo; echo "=== $* ==="; }

passo "0. Preparacao: aluno com 2 cursos conquistados"
ID=$(curl -s -X POST "$CORE/alunos" -H 'Content-Type: application/json' -d '{"email":"lab3@facens.br"}' | sed -E 's/.*"id":([0-9]+).*/\1/')
curl -s -o /dev/null -X POST "$CORE/alunos/$ID/conclusoes" -H 'Content-Type: application/json' -d '{"curso":"Java","media":9}'
curl -s -o /dev/null -X POST "$CORE/alunos/$ID/conclusoes" -H 'Content-Type: application/json' -d '{"curso":"Git","media":8}'
echo "aluno id=$ID"; curl -s "$IA/health"; echo

passo "1. IA no ar"
chamar GET "/alunos/$ID/recomendacoes"
chamar POST /assistente/perguntas '{"pergunta":"Como viro Premium?"}'

passo "2. IA lenta (3,5 s): timeouts de 800 ms (recomendacao) e 3 s (assistente) protegem o Core"
curl -s -X PUT "$IA/lab/latencia?ms=3500" >/dev/null
chamar GET "/alunos/$ID/recomendacoes"
chamar POST /assistente/perguntas '{"pergunta":"Como viro Premium?"}'
curl -s -X PUT "$IA/lab/latencia?ms=0" >/dev/null

passo "3. IA PARADA ($IA_PARAR)"
eval "$IA_PARAR" >/dev/null 2>&1; sleep 2
curl -s -m 2 "$IA/health" >/dev/null && echo "ia-service ainda responde?!" || echo "ia-service fora do ar (conexao recusada)"
chamar GET "/alunos/$ID/recomendacoes"
chamar POST /assistente/perguntas '{"pergunta":"Como viro Premium?"}'
echo "--- fluxo essencial (nao depende da IA):"
chamar POST "/alunos/$ID/conclusoes" '{"curso":"Spring","media":9.5}'
chamar GET "/ranking?tamanho=3"

passo "4. IA volta ($IA_SUBIR): o Core se recupera sozinho, sem restart"
eval "$IA_SUBIR" >/dev/null 2>&1
for i in $(seq 1 30); do curl -s -o /dev/null "$IA/health" && break; sleep 1; done
chamar GET "/alunos/$ID/recomendacoes"
