# Sistema de Horas Complementares — ATDD (BDD + TDD)

Projeto da disciplina de **Engenharia de Software** que demonstra **ATDD
(Acceptance Test Driven Development)** na prática: cada regra de negócio nasce de
um cenário de aceitação escrito em Gherkin, é levada ao vermelho com testes
automatizados **antes** da implementação e só então é implementada e refatorada.

A avaliação exige **evidência do processo** (ciclo Red → Green → Blue), não apenas
o código final. Essa evidência está em:

- os **commits** do repositório (3 por User Story, na ordem `red` → `green` → `blue`);
- [`docs/evidencias.md`](docs/evidencias.md) — saídas de terminal do RED e do GREEN de cada feature;
- a seção [Evidências do ciclo TDD](#evidências-do-ciclo-tdd) no final deste arquivo.

---

## 1. Domínio

Sistema de **Horas Complementares** de uma faculdade:

- O **aluno** submete certificados das atividades que realizou.
- Todo certificado nasce **`EM_ANALISE`** e **não soma horas**.
- O **coordenador** aprova ou reprova cada certificado.
- Horas **aprovadas** somam até a **integralização de 200 horas**, respeitando o
  **teto de cada categoria**.

### Categorias e tetos

| Categoria  | Teto de horas |
|------------|---------------|
| `ENSINO`   | 80            |
| `PESQUISA` | 80            |
| `EXTENSAO` | 80            |
| `EVENTO`   | 60            |
| **Total exigido para integralizar** | **200** |

### Entidades

- **`Aluno`** — nome e data de ingresso.
- **`Categoria`** — nome (único, catálogo) e teto de horas.
- **`Certificado`** — aluno, categoria, título, carga horária, data e
  `StatusCertificado` (`EM_ANALISE`, `APROVADO`, `REPROVADO`).
- **`HistoricoValidacao`** — registro imutável de cada aprovação/reprovação.

### Regras de negócio puras (100 % cobertas por teste de unidade)

| Classe (pacote `domain`)   | Responsabilidade |
|----------------------------|------------------|
| `CertificadoValidator`     | carga horária (1..200) e data (não futura / não anterior ao ingresso) |
| `Justificativa`            | objeto de valor: justificativa de reprovação com no mínimo 10 caracteres |
| `Certificado` (comportamento) | transição de status: só `EM_ANALISE` valida; `APROVADO` é imutável |
| `CalculadoraDeHoras`       | teto por categoria (excedente não soma) e limite geral de 200 h |
| `ContribuicaoCategoria`    | objeto de valor: aplica `min(horas, teto)` de uma categoria |

Nenhuma dessas classes tem anotação Spring — por isso é possível exigir **100 % de
cobertura de linha e branch** nos pacotes `domain` e `service`.

---

## 2. User Stories e critérios de aceitação

Uma feature BDD por integrante (`src/test/resources/features/*.feature`).

### US-01 — Submissão de certificado &nbsp;·&nbsp; _Kaique Vecchia Alves_

`us01_submissao_certificado.feature`

- Certificado válido nasce **`EM_ANALISE`** e **não soma horas**.
- Carga horária **inteira, > 0 e ≤ 200**.
- Data **não futura** e **não anterior ao ingresso** do aluno.
- Mesmo **título + mesma data do mesmo aluno** = duplicado, rejeitado.
- A **categoria precisa existir no catálogo**.

### US-02 — Validação pelo coordenador &nbsp;·&nbsp; _Micael Teodoro de Almeida_

`us02_validacao_coordenador.feature`

- Só certificado **`EM_ANALISE`** muda de status.
- **Reprovar exige justificativa** com no mínimo **10 caracteres**.
- Certificado **`APROVADO` é imutável**.
- Cada validação gera um **`HistoricoValidacao`**.

### US-03 — Teto por categoria &nbsp;·&nbsp; _Nathalia Atamanchuk Baleeiro_

`us03_teto_por_categoria.feature`

- Horas que **ultrapassam o teto da categoria** viram **excedente** e **não somam**.
- O **total geral nunca passa de 200 h**.
- Certificado ainda em análise **não é contabilizado**.

> **Observação de escopo:** o trabalho foi entregue com **3 User Stories** (uma por
> integrante do grupo). A US de "Progresso e integralização" (percentual, status
> `INTEGRALIZADO`, emissão de declaração) ficou fora do escopo; ainda assim o
> endpoint `GET /api/alunos/{id}/horas` já devolve `percentualProgresso` e
> `totalExigido` (200) calculados a partir do resumo.

---

## 3. O que é ATDD / BDD / TDD

| Sigla | O quê | Onde aparece aqui |
|-------|-------|-------------------|
| **BDD** (Behavior Driven Development) | Descreve o comportamento esperado em linguagem de negócio (`Dado / Quando / Então`), legível por não-programadores. | Arquivos `.feature` em Gherkin **português**, com cenário feliz, de borda e de erro, usando **Esquema do Cenário** + **Exemplos**. |
| **ATDD** (Acceptance Test Driven Development) | O time define **junto** o critério de aceitação **antes** de codar; esse critério vira um teste automatizado que guia a implementação. | Cada feature é escrita e ligada a _step definitions_ **antes** do código de produção. O `.feature` é o contrato. |
| **TDD** (Test Driven Development) | Ciclo curto **Red → Green → Refactor** no nível de unidade. | Testes JUnit 5 + AssertJ (e Mockito nos serviços) escritos antes da implementação; refatoração com a suíte verde. |

### Ciclo aplicado a cada feature

1. **RED** — escreve-se o `.feature`, as _step definitions_ e os testes de unidade.
   `mvn test` **falha** (a saída é registrada em `docs/evidencias.md`).
   → commit `red(usNN): ...`
2. **GREEN** — implementação **mínima** para a suíte passar. `mvn test` **verde**.
   → commit `green(usNN): ...`
3. **BLUE** — refatoração (extrair classe/objeto de valor) **mantendo o verde**.
   → commit `blue(usNN): ...`

---

## 4. Stack

- **Java 17**, **Spring Boot 3.3.4**, **Maven**
- Spring Web, Spring Data JPA, **H2** em memória, Bean Validation
- **Cucumber 7** (`cucumber-java`, `cucumber-spring`, `cucumber-junit-platform-engine`)
- **JUnit 5**, **AssertJ**, Mockito
- **JaCoCo** (`prepare-agent`, `report`, `check`)

### Arquitetura em camadas

```
controller  →  service  →  repository  →  (banco H2)
                  ↓
               domain   (regras de negócio puras, sem Spring)
```

- Exceções de negócio próprias (`NegocioException` e subclasses) tratadas de forma
  central no `RestExceptionHandler` (`@RestControllerAdvice`).
- `config`, `dto` e a classe `main` ficam **fora** da regra de cobertura.

---

## 5. Como rodar

O projeto exige **JDK 17**. Há um **Maven Wrapper** (`./mvnw`) — não é preciso ter o
Maven instalado.

```bash
# subir a aplicação (http://localhost:8080)
./mvnw spring-boot:run

# rodar toda a suíte + cobertura + regra de 100% em domain/service
./mvnw clean verify
```

Se o `JAVA_HOME` do shell não apontar para o JDK 17:

```bash
JAVA_HOME=/caminho/para/jdk-17 ./mvnw clean verify
```

- Console do H2: `http://localhost:8080/h2-console`
  (JDBC URL `jdbc:h2:mem:horascomplementares`, usuário `sa`, sem senha).
- O catálogo de categorias é carregado por `src/main/resources/data.sql`.
- Relatório de cobertura HTML: `target/site/jacoco/index.html`.

---

## 6. Endpoints

Base: `/api`

| Método | Rota | Descrição | Sucesso |
|--------|------|-----------|---------|
| `POST` | `/api/alunos` | Cadastra um aluno `{ "nome", "dataIngresso" }` | `201` |
| `GET`  | `/api/alunos` | Lista alunos | `200` |
| `GET`  | `/api/alunos/{id}` | Busca um aluno | `200` |
| `GET`  | `/api/categorias` | Catálogo de categorias com seus tetos | `200` |
| `POST` | `/api/certificados` | Submete certificado `{ "alunoId", "categoria", "titulo", "cargaHoraria", "data" }` — nasce `EM_ANALISE` | `201` |
| `GET`  | `/api/alunos/{id}/certificados` | Certificados do aluno | `200` |
| `POST` | `/api/certificados/{id}/aprovacao` | Coordenador aprova | `200` |
| `POST` | `/api/certificados/{id}/reprovacao` | Coordenador reprova `{ "justificativa" }` (≥ 10 caracteres) | `200` |
| `GET`  | `/api/alunos/{id}/horas` | Resumo de horas: contabilizadas, excedentes, progresso e detalhe por categoria | `200` |

### Erros (via `@RestControllerAdvice`)

| Situação | Status |
|----------|--------|
| Aluno / categoria / certificado inexistente | `404 Not Found` |
| Transição de status inválida / certificado `APROVADO` imutável | `409 Conflict` |
| Carga horária, data, justificativa ou duplicidade inválidas | `422 Unprocessable Entity` |
| Corpo da requisição inválido (Bean Validation) | `400 Bad Request` |

Corpo do erro: `{ "timestamp", "status", "erro", "mensagem" }`.

### Exemplo rápido

```bash
curl -s -XPOST localhost:8080/api/alunos \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","dataIngresso":"2023-01-01"}'

curl -s -XPOST localhost:8080/api/certificados \
  -H 'Content-Type: application/json' \
  -d '{"alunoId":1,"categoria":"EVENTO","titulo":"Palestra","cargaHoraria":90,"data":"2024-03-01"}'

curl -s -XPOST localhost:8080/api/certificados/1/aprovacao
curl -s localhost:8080/api/alunos/1/horas
# -> totalContabilizado 60, totalExcedente 30 (teto de EVENTO = 60)
```

---

## 7. Cobertura (JaCoCo)

`./mvnw clean verify` executa **103 testes** (75 unidade/serviço + 28 cenários
Gherkin) e aplica a regra de cobertura.

| Pacote | Linhas | Branches |
|--------|--------|----------|
| `br.facens.horascomplementares.domain` | **110 / 110 — 100 %** | **18 / 18 — 100 %** |
| `br.facens.horascomplementares.domain.exception` | **20 / 20 — 100 %** | — |
| `br.facens.horascomplementares.service` | **54 / 54 — 100 %** | **2 / 2 — 100 %** |

Regra `jacoco:check` (falha o build se não atingir):
`LINE = 1.00` e `BRANCH = 1.00` nos pacotes `domain` e `service`
(exclui `config`, `dto` e `HorasComplementaresApplication`).

```
[INFO] --- jacoco:0.8.12:check (jacoco-check) ---
[INFO] All coverage checks have been met.
[INFO] BUILD SUCCESS
[INFO] Tests run: 103, Failures: 0, Errors: 0, Skipped: 0
```

---

## 8. Evidências do ciclo TDD

Saída de `git log --oneline --pretty=format:"%h %an %s"` (ordem cronológica real
— cada US tem um autor diferente, informado via `git commit --author`):

```
b6b4bce Kaique Vecchia Alves chore: camada REST (controllers + RestControllerAdvice) e servicos de consulta
f1385a3 Nathalia Atamanchuk Baleeiro blue(us03): move regra de teto para ContribuicaoCategoria
44c8518 Nathalia Atamanchuk Baleeiro green(us03): implementa teto por categoria e limite geral de 200h
65af4d1 Nathalia Atamanchuk Baleeiro red(us03): cenarios de teto por categoria falhando
e83e1ab Micael Teodoro de Almeida blue(us02): extrai objeto de valor Justificativa
0f526ec Micael Teodoro de Almeida green(us02): implementa transicao de status e regra de justificativa
6f01b68 Micael Teodoro de Almeida red(us02): cenarios de validacao pelo coordenador falhando
6b318ad Kaique Vecchia Alves blue(us01): extrai CertificadoValidator
0ecff93 Kaique Vecchia Alves green(us01): implementa validacao de submissao de certificado
4edf8d4 Kaique Vecchia Alves red(us01): cenario de submissao de certificado falhando
733844e Kaique Vecchia Alves chore: estrutura do projeto
```

| US | RED | GREEN | BLUE |
|----|-----|-------|------|
| US-01 | `4edf8d4` | `0ecff93` | `6b318ad` (extrai `CertificadoValidator`) |
| US-02 | `6f01b68` | `0f526ec` | `e83e1ab` (extrai `Justificativa`) |
| US-03 | `65af4d1` | `44c8518` | `f1385a3` (move regra de teto p/ `ContribuicaoCategoria`) |

> Os hashes acima são os do repositório no momento da entrega; rode
> `git log --oneline --pretty=format:"%h %an %s"` para conferir.
