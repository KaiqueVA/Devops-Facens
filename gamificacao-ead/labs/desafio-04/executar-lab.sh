#!/usr/bin/env bash
# Desafio 4 - interacao sincrona x assincrona; consumidor (provedor de notificacao) indisponivel.
#
# Parte A (decisao adotada: outbox assincrono)
#   docker compose up -d app notificacao-stub
#   bash labs/desafio-04/executar-lab.sh
# Parte B (alternativa rejeitada: notificacao sincrona dentro da conclusao)
#   suba o Core com EVENTOS_ENTREGA=sincrona  (ex.: EVENTOS_ENTREGA=sincrona docker compose up -d app)
#   bash labs/desafio-04/executar-lab.sh sincrono
set -uo pipefail
# Windows/Git Bash: "python3" pode ser so o atalho da Microsoft Store; nesse caso usa "python"
if ! python3 -c "" >/dev/null 2>&1; then python3() { python "$@"; }; fi
CORE="${CORE:-http://localhost:8080}"
STUB="${STUB:-http://localhost:8090}"
STUB_PARAR="${STUB_PARAR:-docker compose stop notificacao-stub}"
STUB_SUBIR="${STUB_SUBIR:-docker compose start notificacao-stub}"
MODO="${1:-assincrono}"

chamar() {
  local s
  s=$(curl -s -o /tmp/lab4.json -w "%{http_code} %{time_total}" -X "$1" "$CORE$2" -H 'Content-Type: application/json' ${3:+-d "$3"})
  echo "\$ $1 $2 ${3:-} ->  HTTP ${s% *} em ${s#* }s"; cut -c1-240 /tmp/lab4.json; echo
}
outbox() {
  curl -s "$CORE/eventos/outbox" | python3 -c '
import json,sys
d=json.load(sys.stdin)
print("  por assinante/status:", json.dumps(d["porAssinante"]))
for e in d["recentes"][:6]:
    print("  #%-3s %-24s %-17s tentativas=%s %s" % (e["id"], e["assinante"], e["status"], e["tentativas"], (e["ultimoErro"] or "")[:70]))'
}
passo() { echo; echo "=== $* ==="; }
novo_aluno() { curl -s -X POST "$CORE/alunos" -H 'Content-Type: application/json' -d "{\"email\":\"$1\"}" | sed -E 's/.*"id":([0-9]+).*/\1/'; }

if [ "$MODO" = "sincrono" ]; then
  passo "B. ALTERNATIVA REJEITADA: Core em modo sincrono (EVENTOS_ENTREGA=sincrona)"
  ID=$(novo_aluno "sincrono$RANDOM@facens.br")
  chamar POST "/alunos/$ID/conclusoes" '{"curso":"Java","media":9}'
  passo "B. provedor de notificacao PARADO"
  eval "$STUB_PARAR" >/dev/null 2>&1; sleep 2
  chamar POST "/alunos/$ID/conclusoes" '{"curso":"Spring","media":9}'
  echo "--- estado do aluno depois da falha (a 2a conclusao foi perdida):"
  chamar GET "/alunos/$ID"
  eval "$STUB_SUBIR" >/dev/null 2>&1
  exit 0
fi

passo "1. Interacao SINCRONA: conclusao responde na hora com a recompensa ja calculada"
ID=$(novo_aluno "lab4$RANDOM@facens.br")
chamar POST "/alunos/$ID/conclusoes" '{"curso":"Java","media":9}'

passo "2. Interacao ASSINCRONA: notificacao e analytics chegam pelo outbox (relay a cada 2 s)"
sleep 4
outbox
echo "  provedor recebeu: $(curl -s "$STUB/notificacoes" | python3 -c 'import json,sys; print([n["titulo"] for n in json.load(sys.stdin)])')"

passo "3. Consumidor INDISPONIVEL ($STUB_PARAR)"
eval "$STUB_PARAR" >/dev/null 2>&1; sleep 2
for curso in Spring Docker Git; do chamar POST "/alunos/$ID/conclusoes" "{\"curso\":\"$curso\",\"media\":8.5}"; done
sleep 8
outbox
echo "--- analytics (outro assinante do mesmo evento) segue em dia:"
chamar GET /analytics/resumo

passo "4. Consumidor VOLTA ($STUB_SUBIR): retentativas com backoff entregam o que ficou retido"
eval "$STUB_SUBIR" >/dev/null 2>&1
INICIO=$(date +%s); P=?
for i in $(seq 1 90); do
  P=$(curl -s "$CORE/eventos/outbox" | python3 -c 'import json,sys; print(json.load(sys.stdin)["porAssinante"].get("notificacao.conclusao",{}).get("PENDENTE",0))')
  [ "$P" = "0" ] && break; sleep 1
done
echo "(notificacoes pendentes: $P, $(( $(date +%s) - INICIO ))s depois que o provedor voltou)"
outbox
echo "  provedor recebeu (sem duplicatas):"
curl -s "$STUB/notificacoes" | python3 -c 'import json,sys
for n in json.load(sys.stdin): print("   -", n["titulo"], "| duplicadas:", n["duplicadas"])'
