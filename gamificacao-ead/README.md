# Gamificação para Engajamento de Educação Continuada

Prática de **ATDD** — Disciplina de Testes de Software.
Roteiro do quadro: **User Story → BDD (Cucumber) → TDD → Jacoco**.

| | |
|---|---|
| **Stack** | Java 17 · Spring Boot 3.3.4 · Maven |
| **Dependências pedidas** | Spring Web · Spring Data JPA · H2 (em memória) · Cucumber |
| **Testes** | JUnit 5 · Mockito · Cucumber 7 · Jacoco (100%) |

## Equipe

| Integrante | RA | User Story | Cenário BDD | Teste de unidade |
|---|---|---|---|---|
| Kaique Vecchia Alves | 235446 | US1 | Cenário 1 | `deveLiberarTresCursosAoConcluirComMediaAcimaDeSete` |
| Micael Teodoro de Almeida | 234941 | US2 | Cenário 4 | `devePremiarODestaqueDoForum` |
| Nathalia Atamanchuk Baleeiro | 235215 | US3 | Cenário 3 | `deveVirarPremiumAoConquistarDozeCursos` |

---

## 1. O case

> Uma plataforma vende cursos online e EAD no modelo de assinaturas. O aluno paga um valor mensal
> e tem acesso a um conjunto de cursos na assinatura básica. A cada curso terminado com média acima
> de 7,0, o aluno tem direito à realização de mais 3 cursos. O aluno que escrever mais tópicos no
> fórum e ajudar outros participantes com seus comentários ganha um curso no final do mês. Quando o
> aluno conquistar 12 cursos, seu plano passa a ser "Premium" e ele passa a receber voucher para
> participar de projetos reais e 3 moedas, que podem ser convertidas em conhecimento (novos cursos),
> acumuladas ou recebidas por criptomoeda.

### Regras de negócio mapeadas

| RN | Regra | Onde vive no código |
|---|---|---|
| RN1 | Assinatura básica dá acesso a um conjunto de cursos | `Aluno.CURSOS_DA_ASSINATURA_BASICA` |
| RN2 | Curso terminado **com média acima de 7,0** dá direito a mais **3 cursos** | `Aluno.concluirCurso` |
| RN3 | Quem escreve mais tópicos **e** ajuda outros com comentários ganha **1 curso** no fim do mês | `GamificacaoService.premiarDestaqueDoForum` |
| RN4 | Ao conquistar **12 cursos**, o plano vira **Premium**, com **voucher** e **3 moedas** | `Aluno.promoverSeAtingiuAMeta` |
| RN5 | Moedas viram **conhecimento**, ficam **acumuladas** ou viram **criptomoeda** | `Aluno.converterMoedas` |

### Decisões de modelagem

O enunciado é ambíguo em três pontos. As escolhas do grupo foram:

1. **"Conquistar um curso" = concluir com média acima de 7,0.** Cursos concluídos com média igual
   ou inferior contam em `cursosConcluidos`, mas não em `cursosConquistados`. A promoção a Premium
   usa `cursosConquistados >= 12`.
2. **"Acima de 7,0" é comparação estrita**: média exatamente 7,0 **não** libera os 3 cursos.
   Existe teste específico para essa fronteira (`CT-02`).
3. **`ACUMULO` é uma conversão cujo efeito é manter o saldo** — o aluno declara que não vai gastar
   agora. O teste `CT-15` prova que nada muda: nem moedas, nem cursos, nem cripto.

---

## 2. User Stories (item 1 e 2 do quadro)

| # | Integrante | User Story |
|---|---|---|
| US1 | Kaique Vecchia Alves | **COMO** aluno assinante da plataforma **QUERO** concluir um curso com bom aproveitamento **PARA** liberar novos cursos sem pagar nada a mais. |
| US2 | Micael Teodoro de Almeida | **COMO** aluno participante do fórum **QUERO** ter minhas contribuições reconhecidas **PARA** ganhar um curso ao final do mês. |
| US3 | Nathalia Atamanchuk Baleeiro | **COMO** aluno **QUERO** evoluir para o plano Premium ao conquistar 12 cursos **PARA** receber voucher de projetos reais e moedas conversíveis em conhecimento, acúmulo ou criptomoeda. |

**US escolhida pelo grupo: US3.** É a que concentra mais regras testáveis: depende do acúmulo de
cursos conquistados (RN2), dispara três efeitos simultâneos na promoção (plano, voucher e moedas —
RN4) e ainda abre três caminhos distintos de conversão (RN5). Regra rica gera BDD com efeito
observável e TDD com asserts significativos.

---

## 3. BDD (itens 3 e 5 do quadro)

Arquivo: [`src/test/resources/features/gamificacao.feature`](src/test/resources/features/gamificacao.feature)

São **5 cenários**: 3 titulares (um por integrante, como pede o quadro) e 2 complementares do grupo,
necessários porque o case tem cinco regras de negócio distintas.

| Cenário | Integrante | RN | Por que é uma funcionalidade válida |
|---|---|---|---|
| 1 – Conclusão libera 3 cursos | Kaique | RN2 | Fluxo principal: o aproveitamento vira benefício mensurável no saldo |
| 2 – Média fora da faixa é recusada | Micael | RN2 (negativo) | Estado inválido do domínio, não erro de tela; prova que o aluno não é corrompido |
| 3 – 12 cursos promovem a Premium | Nathalia | RN4 | Regra com **três** efeitos observáveis de uma vez |
| 4 – Destaque do fórum ganha um curso | grupo | RN3 | Prova a regra completa: mais tópicos **e** ter ajudado outros |
| 5 – Moeda vira conhecimento | grupo | RN5 | Fecha o ciclo: o benefício volta a virar curso |

```gherkin
Cenario: Conquistar 12 cursos promove o aluno a Premium
  Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
  E o aluno ja conquistou 11 cursos com media acima de 7,0
  Quando o aluno conclui o curso "Curso 12" com media 9.0
  Entao o plano do aluno deve ser "PREMIUM"
  E o aluno deve possuir voucher para projetos reais
  E o saldo de moedas do aluno deve ser 3
```

> O cenário 5 espera **32** cursos disponíveis. A conta: 5 da assinatura + 12 conclusões ×
> (−1 gasto + 3 de bônus) = 29, mais as 3 moedas convertidas em curso.

---

## 4. TDD — BLUE (refatoração)

Refatorações aplicadas com a suíte de testes verde, sem alterar comportamento:

1. Validações espalhadas (e-mail, nome do curso, faixa da média, saldo, moedas) viraram **uma**
   exceção de domínio, `GamificacaoException`, com mensagens padronizadas. O controller traduz
   todas para HTTP 400 num único `@ExceptionHandler`.
2. A promoção a Premium saiu de `concluirCurso` e virou o método privado `promoverSeAtingiuAMeta()`,
   chamado só quando o curso é de fato conquistado. O efeito colateral triplo (plano + voucher +
   moedas) passou a ter um lugar só, impossível de esquecer.
3. Os números mágicos (7,0 · 3 cursos · 12 cursos · 3 moedas) viraram **constantes públicas** na
   entidade. Os testes passaram a citar a regra (`Aluno.CURSOS_PARA_PREMIUM`) em vez do literal.
4. O ranking do fórum saiu do laço `for` com variável "melhor até agora" e virou um `Stream` com
   `filter(Aluno::ajudouOutrosParticipantes)` +
   `Comparator.comparingInt(...).thenComparingInt(...)` — o critério de desempate ficou explícito.

Comportamento não mudou e os testes continuaram verdes depois da refatoração.

---

## 5. Como rodar

```bash
mvn clean test        # testes de unidade + BDD, gera o relatório Jacoco
mvn clean verify      # idem, e REPROVA o build se a cobertura ficar abaixo de 100%
mvn spring-boot:run   # sobe a API em http://localhost:8080
```

Relatórios gerados:

| Relatório | Caminho |
|---|---|
| Cucumber (BDD) | `target/cucumber-report.html` |
| Jacoco (cobertura) | `target/site/jacoco/index.html` |

### Endpoints

```bash
curl -X POST "http://localhost:8080/alunos?email=maria@teste.com"
curl -X POST "http://localhost:8080/alunos/1/conclusoes?curso=Java%20Basico&media=8.5"
curl -X POST "http://localhost:8080/alunos/1/forum?topicos=10&comentarios=9"
curl -X POST "http://localhost:8080/alunos/forum/premiacao"
curl -X POST "http://localhost:8080/alunos/1/moedas?quantidade=3&destino=CONHECIMENTO"
curl "http://localhost:8080/alunos/1"
```

Console do H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:gamificacao`, usuário `sa`).

---

## 6. Cobertura Jacoco: 100%

O `pom.xml` tem uma regra de `check` que **falha o build** se linhas ou branches ficarem abaixo
de 100%. Como o 100% foi alcançado:

- Todo `if` tem teste do caso verdadeiro **e** do falso. Nas condições compostas, cada operando é
  exercitado isoladamente — na validação da média há um teste para `null`, um para negativo, um
  para acima de 10,0, além do caminho válido.
- A promoção (`plano == BASICO && conquistados >= 12`) tem os dois lados de cada operando: `CT-07`
  cobre `true/true`, os cursos 1 a 11 cobrem o segundo operando falso e `CT-08` (13º curso, aluno
  já Premium) cobre o primeiro falso.
- Os três valores de `DestinoMoeda` têm teste próprio (`CT-13`, `CT-14`, `CT-15`), fechando o
  `if / else if` sem ramo órfão.
- O construtor protegido exigido pelo JPA é exercitado por `CT-19`, no mesmo pacote.
- `values()` e `valueOf()` dos enums têm teste próprio (`EnumsTest`) — é o que costuma derrubar o 100%.
- `GamificacaoApplication` (só o método `main`) está **excluída** no `pom.xml`.

### Rastreabilidade cenário → teste → assertiva

| Cenário BDD | Teste de unidade | Assertiva que comprova o critério de aceite |
|---|---|---|
| 1 | `deveLiberarTresCursosAoConcluirComMediaAcimaDeSete` | `assertEquals(7, aluno.getCursosDisponiveis())` |
| 2 | `mediaForaDaFaixaERecusada` | `assertEquals("Media deve estar entre 0,0 e 10,0", erro.getMessage())` |
| 3 | `deveVirarPremiumAoConquistarDozeCursos` | `assertEquals(PlanoAssinatura.PREMIUM, aluno.getPlano())` + voucher + 3 moedas |
| 4 | `devePremiarODestaqueDoForum` | `assertEquals("tres@teste.com", premiado.getEmail())` e `+1` curso |
| 5 | `deveConverterMoedasEmConhecimento` | `assertEquals(0, aluno.getMoedas())` e cursos `+3` |

---

## 7. Estrutura

```
gamificacao-ead/
├── pom.xml
├── README.md
├── docs/
│   ├── EVIDENCIAS.md          <- prints do BLUE, Cucumber e Jacoco
│   ├── PLANILHA_ATDD.xlsx     <- planilha ATDD preenchida
│   └── img/                   <- coloque os prints aqui
└── src
    ├── main/java/br/facens/gamificacao/
    │   ├── GamificacaoApplication.java
    │   └── aluno/
    │       ├── domain/      Aluno · PlanoAssinatura · DestinoMoeda · GamificacaoException
    │       ├── repository/  AlunoRepository
    │       ├── service/     GamificacaoService
    │       └── web/         AlunoController
    ├── main/resources/application.properties
    └── test
        ├── java/br/facens/gamificacao/
        │   ├── cucumber/  RunCucumberTest · CucumberSpringConfiguration · GamificacaoSteps
        │   └── aluno/     AlunoTest · EnumsTest · GamificacaoServiceTest · AlunoControllerTest
        └── resources
            ├── features/gamificacao.feature
            └── junit-platform.properties
```

---

## 8. Histórico de commits sugerido

O próprio histórico do Git vira evidência de que o ciclo foi seguido:

```bash
git init && git add pom.xml README.md .gitignore && git commit -m "chore: projeto Spring Boot com Web, JPA, H2 e Cucumber"
# adicione só os testes de dominio:
git add src/test src/main && git commit -m "feat: dominio de gamificacao com a suite de testes"
git commit --allow-empty -m "refactor(BLUE): excecao unica, constantes de regra e ranking com Stream"
git add docs && git commit -m "docs: evidencias de BDD, TDD e cobertura"
```

## 9. Checklist de entrega

- [ ] Link do repositório postado no Canvas
- [ ] `README.md` documentando US, BDD e TDD
- [ ] Prints em `docs/img/` (BLUE, Cucumber, Jacoco)
- [ ] `docs/PLANILHA_ATDD.xlsx` no repositório
- [ ] `mvn clean verify` passando localmente