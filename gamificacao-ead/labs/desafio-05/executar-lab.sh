#!/usr/bin/env bash
# Desafio 5 - simulador publica no MQTT; gateway consome, traduz e entrega ao Core.
# Falhas simuladas: gateway fora, Core fora, rede do dispositivo caindo, reenvio duplicado, payload invalido.
#
#   docker compose up -d app mosquitto iot-gateway
#   bash labs/desafio-05/executar-lab.sh
set -uo pipefail
# Windows/Git Bash: "python3" pode ser so o atalho da Microsoft Store; nesse caso usa "python"
if ! python3 -c "" >/dev/null 2>&1; then python3() { python "$@"; }; fi
CORE="${CORE:-http://localhost:8080}"
SIM="${SIM:-docker compose run --rm --no-deps iot-gateway python -u simulador.py --host mosquitto}"
GW_PARAR="${GW_PARAR:-docker compose stop iot-gateway}"
GW_SUBIR="${GW_SUBIR:-docker compose start iot-gateway}"
CORE_PARAR="${CORE_PARAR:-docker compose stop app}"
CORE_SUBIR="${CORE_SUBIR:-docker compose start app}"
DLQ_SUB="${DLQ_SUB:-docker compose exec -T mosquitto mosquitto_sub -t lab/dlq -C 1 -W 15}"

passo() { echo; echo "=== $* ==="; }
resumo() { echo "  Core /iot/eventos/resumo: $(curl -s "$CORE/iot/eventos/resumo")"; }
esperar_core() { for i in $(seq 1 90); do curl -s -o /dev/null "$CORE/iot/eventos/resumo" && return; sleep 1; done; }
esperar_armazenados() { for i in $(seq 1 60); do
  n=$(curl -s "$CORE/iot/eventos/resumo" | python3 -c 'import json,sys; print(json.load(sys.stdin)["armazenados"])' 2>/dev/null)
  [ "$n" = "$1" ] && return; sleep 1; done; }

passo "0. Aluno que vai usar a bancada"
ALUNO=$(curl -s -X POST "$CORE/alunos" -H 'Content-Type: application/json' -d "{\"email\":\"lab5$RANDOM@facens.br\"}" | sed -E 's/.*"id":([0-9]+).*/\1/')
echo "aluno id=$ALUNO"; resumo
BASE=$(curl -s "$CORE/iot/eventos/resumo" | python3 -c 'import json,sys; print(json.load(sys.stdin)["armazenados"])')

passo "1. Caminho feliz: dispositivo -> MQTT (QoS 1) -> gateway -> Core"
$SIM --aluno "$ALUNO" --eventos 3 | tee /tmp/lab5-sim1.txt
esperar_armazenados $((BASE + 3)); resumo

passo "2. GATEWAY FORA: o broker guarda as mensagens na sessao persistente do gateway"
eval "$GW_PARAR" >/dev/null 2>&1; sleep 1
$SIM --aluno "$ALUNO" --eventos 3
sleep 2; resumo; echo "  (nada chegou ao Core ainda)"
eval "$GW_SUBIR" >/dev/null 2>&1
esperar_armazenados $((BASE + 6)); echo "  gateway religado:"; resumo

passo "3. CORE FORA: o gateway retem (sem PUBACK) e tenta de novo com backoff"
eval "$CORE_PARAR" >/dev/null 2>&1; sleep 2
$SIM --aluno "$ALUNO" --eventos 2
sleep 6; echo "  Core religando..."
eval "$CORE_SUBIR" >/dev/null 2>&1; esperar_core
esperar_armazenados $((BASE + 8)); resumo

passo "4. REDE DO DISPOSITIVO CAI: buffer local no dispositivo e descarga ao reconectar"
$SIM --aluno "$ALUNO" --eventos 4 --queda 2:4
esperar_armazenados $((BASE + 12)); resumo

passo "5. REENVIO DUPLICADO (dispositivo nao recebeu o PUBACK e publica de novo o mesmo id)"
ID=$(grep -o '"id":"[0-9a-f]*"' /tmp/lab5-sim1.txt | head -1 | cut -d'"' -f4)
$SIM --aluno "$ALUNO" --duplicar "$ID"
sleep 3; resumo; echo "  (armazenados nao muda; duplicados +1)"

passo "6. PAYLOAD INVALIDO: o gateway nao insiste, publica na DLQ (lab/dlq)"
( $DLQ_SUB > /tmp/lab5-dlq.txt 2>&1 & ); sleep 1
$SIM --aluno "$ALUNO" --invalido
sleep 3; echo "  mensagem na DLQ: $(cat /tmp/lab5-dlq.txt)"

passo "Resultado: 12 eventos publicados com sucesso -> 12 armazenados, 0 perdidos, duplicata sem efeito"
resumo
echo "  analytics (encaminhado ao ecossistema pelo outbox): $(curl -s "$CORE/analytics/resumo")"
