#!/usr/bin/env bash
# Desafio 2 - mesmo fluxo de negocio contra o Core, qualquer que seja o adapter de persistencia.
#
# 1) Suba o Core com o adapter JPA (padrao):   mvn spring-boot:run
#    ou com o adapter em memoria:               mvn spring-boot:run -Dspring-boot.run.profiles=memoria
# 2) Rode:  bash labs/desafio-02/executar-lab.sh  [http://localhost:8080]
# O resultado das chamadas deve ser IDENTICO nos dois perfis (so o ranking, que le a tabela por SQL, fica vazio em "memoria").
set -euo pipefail
URL="${1:-http://localhost:8080}"
j() { echo; echo "\$ $1 $2 ${3:-}"; if [ -n "${3:-}" ]; then curl -s -X "$1" "$URL$2" -H 'Content-Type: application/json' -d "$3"; else curl -s -X "$1" "$URL$2"; fi; echo; }

j POST /alunos '{"email":"ana@facens.br"}'
j POST /alunos '{"email":"bia@facens.br"}'
j POST /alunos/1/conclusoes '{"curso":"Java Basico","media":8.5}'
j POST /alunos/2/conclusoes '{"curso":"Java Basico","media":6.0}'
j POST /alunos/1/forum '{"topicos":5,"comentarios":3}'
j POST /alunos/2/forum '{"topicos":5,"comentarios":4}'
j POST /alunos/forum/premiacao
j GET  /alunos/2
j GET  "/ranking?pagina=0&tamanho=5"
