#!/usr/bin/env bash
# Desafio 6 - provocar uma condicao observavel e registrar a evidencia no Prometheus/Grafana.
# Condicao: ia-service PARADO com trafego de recomendacoes -> ComponenteFora + IaDegradada FIRING -> religa -> resolvidos.
#
#   docker compose up -d          (stack completa, inclui prometheus e grafana)
#   bash labs/desafio-06/executar-lab.sh
# Durante a parte 3, abra o Grafana (http://localhost:3000, dashboard "Arquitetura B1") e tire os prints.
set -uo pipefail
# Windows/Git Bash: "python3" pode ser so o atalho da Microsoft Store; nesse caso usa "python"
if ! python3 -c "" >/dev/null 2>&1; then python3() { python "$@"; }; fi
CORE="${CORE:-http://localhost:8080}"
PROM="${PROM:-http://localhost:9090}"
IA_PARAR="${IA_PARAR:-docker compose stop ia-service}"
IA_SUBIR="${IA_SUBIR:-docker compose start ia-service}"
ESPERADOS="${ALERTAS_ESPERADOS:-ComponenteFora IaDegradada}"
TRAFEGO_PID=""

passo() { echo; echo "=== $(date +%H:%M:%S) $* ==="; }
alertas() {
  curl -s "$PROM/api/v1/alerts" | python3 -c '
import json,sys
a=json.load(sys.stdin)["data"]["alerts"]
if not a: print("  (nenhum alerta ativo)")
for x in a: print("  %-26s %-8s job=%-12s %s" % (x["labels"]["alertname"], x["state"].upper(), x["labels"].get("job","-"), x["annotations"].get("adr","")))'
}
ativos() { curl -s "$PROM/api/v1/alerts" | python3 -c 'import json,sys; print(" ".join(sorted({x["labels"]["alertname"] for x in json.load(sys.stdin)["data"]["alerts"] if x["state"]=="firing"})))'; }
trafego() { while true; do curl -s -o /dev/null "$CORE/alunos/$ALUNO/recomendacoes"; curl -s -o /dev/null "$CORE/ranking?tamanho=10"; sleep 0.5; done; }
parar_trafego() { [ -n "$TRAFEGO_PID" ] && kill "$TRAFEGO_PID" 2>/dev/null; }
trap parar_trafego EXIT

passo "1. Alvos do Prometheus (um por componente)"
curl -s "$PROM/api/v1/targets" | python3 -c '
import json,sys
for t in json.load(sys.stdin)["data"]["activeTargets"]: print("  %-12s %-5s %s" % (t["labels"]["job"], t["health"], t["scrapeUrl"]))'

passo "2. Trafego normal (recomendacoes + ranking a cada 0,5 s)"
ALUNO=$(curl -s -X POST "$CORE/alunos" -H 'Content-Type: application/json' -d "{\"email\":\"lab6$RANDOM@facens.br\"}" | sed -E 's/.*"id":([0-9]+).*/\1/')
trafego & TRAFEGO_PID=$!
sleep 30; alertas

passo "3. PROVOCANDO: ia-service parado ($IA_PARAR) com trafego continuo"
eval "$IA_PARAR" >/dev/null 2>&1
for i in $(seq 1 24); do
  sleep 10; F=$(ativos); echo "  +$((i*10))s firing: ${F:-nenhum}"
  ok=1; for e in $ESPERADOS; do [[ " $F " == *" $e "* ]] || ok=0; done; [ $ok = 1 ] && break
done
alertas
echo ">>> TIRE OS PRINTS AGORA: Grafana (paineis ADR-003 em vermelho) e Prometheus > Alerts (FIRING)"
sleep "${PAUSA_PRINT:-20}"

passo "4. RECUPERANDO: ia-service de volta ($IA_SUBIR)"
eval "$IA_SUBIR" >/dev/null 2>&1
for i in $(seq 1 30); do
  sleep 10; F=$(ativos); echo "  +$((i*10))s firing: ${F:-nenhum}"; [ -z "$F" ] && break
done
alertas
echo ">>> TIRE OS PRINTS: alertas resolvidos e paineis voltando ao verde"
