# Desafio 1: comparação de dependências (gerado por labs/desafio-01/analisar_dependencias.py)

## Antes: AC1 (camadas)

- Classes: **13** | Pacotes: **6** | Dependencias entre pacotes: **7** | Modulos de 1o nivel: **1** (aluno)
- Dependencias ENTRE modulos de 1o nivel: **0**

| Pacote | Classes | Ce | Ca | I |
|---|---:|---:|---:|---:|
| `(raiz)` | 1 | 0 | 0 | - |
| `aluno.domain` | 4 | 0 | 4 | 0.00 |
| `aluno.dto` | 5 | 1 | 1 | 0.50 |
| `aluno.repository` | 1 | 1 | 1 | 0.50 |
| `aluno.service` | 1 | 2 | 1 | 0.67 |
| `aluno.web` | 1 | 3 | 0 | 1.00 |

| De | Para | Imports |
|---|---|---:|
| `aluno.dto` | `aluno.domain` | 3 |
| `aluno.repository` | `aluno.domain` | 1 |
| `aluno.service` | `aluno.domain` | 3 |
| `aluno.service` | `aluno.repository` | 1 |
| `aluno.web` | `aluno.domain` | 2 |
| `aluno.web` | `aluno.dto` | 5 |
| `aluno.web` | `aluno.service` | 1 |

## Alternativa A: ranking nas camadas da AC1

- Classes: **16** | Pacotes: **6** | Dependencias entre pacotes: **7** | Modulos de 1o nivel: **1** (aluno)
- Dependencias ENTRE modulos de 1o nivel: **0**

| Pacote | Classes | Ce | Ca | I |
|---|---:|---:|---:|---:|
| `(raiz)` | 1 | 0 | 0 | - |
| `aluno.domain` | 4 | 0 | 4 | 0.00 |
| `aluno.dto` | 6 | 1 | 1 | 0.50 |
| `aluno.repository` | 1 | 1 | 1 | 0.50 |
| `aluno.service` | 2 | 2 | 1 | 0.67 |
| `aluno.web` | 2 | 3 | 0 | 1.00 |

| De | Para | Imports |
|---|---|---:|
| `aluno.dto` | `aluno.domain` | 4 |
| `aluno.repository` | `aluno.domain` | 1 |
| `aluno.service` | `aluno.domain` | 4 |
| `aluno.service` | `aluno.repository` | 2 |
| `aluno.web` | `aluno.domain` | 3 |
| `aluno.web` | `aluno.dto` | 6 |
| `aluno.web` | `aluno.service` | 2 |

## Alternativa B (escolhida): monolito modular

- Classes: **19** | Pacotes: **9** | Dependencias entre pacotes: **9** | Modulos de 1o nivel: **2** (gamificacao, ranking)
- Dependencias ENTRE modulos de 1o nivel: **0**

| Pacote | Classes | Ce | Ca | I |
|---|---:|---:|---:|---:|
| `(raiz)` | 1 | 0 | 0 | - |
| `gamificacao.application` | 1 | 2 | 1 | 0.67 |
| `gamificacao.domain` | 4 | 0 | 4 | 0.00 |
| `gamificacao.infra` | 1 | 1 | 1 | 0.50 |
| `gamificacao.web` | 1 | 3 | 0 | 1.00 |
| `gamificacao.web.dto` | 5 | 1 | 1 | 0.50 |
| `ranking.application` | 4 | 0 | 2 | 0.00 |
| `ranking.infra` | 1 | 1 | 0 | 1.00 |
| `ranking.web` | 1 | 1 | 0 | 1.00 |

| De | Para | Imports |
|---|---|---:|
| `gamificacao.application` | `gamificacao.domain` | 3 |
| `gamificacao.application` | `gamificacao.infra` | 1 |
| `gamificacao.infra` | `gamificacao.domain` | 1 |
| `gamificacao.web` | `gamificacao.application` | 1 |
| `gamificacao.web` | `gamificacao.domain` | 2 |
| `gamificacao.web` | `gamificacao.web.dto` | 5 |
| `gamificacao.web.dto` | `gamificacao.domain` | 3 |
| `ranking.infra` | `ranking.application` | 2 |
| `ranking.web` | `ranking.application` | 2 |

