# Evidências do ciclo ATDD (Red → Green → Blue)

Este documento reúne as saídas de terminal que comprovam o processo. Para cada
User Story há a saída do `mvn test` **falhando** (RED, testes escritos antes da
implementação) e depois **passando** (GREEN, implementação mínima).

O comando usado foi `mvn test` (Maven 3.9.9, Temurin/OpenJDK 17).

---

## US-01 — Submissão de certificado (autor: Kaique Vecchia Alves)

### RED — `mvn test` (feature + steps + testes de unidade escritos, sem implementação)

Arquivos escritos primeiro:
- `src/test/resources/features/us01_submissao_certificado.feature`
- `src/test/java/.../steps/SubmissaoCertificadoSteps.java`
- `src/test/java/.../service/CertificadoServiceTest.java`

`CertificadoService.submeter(...)` existe apenas como esqueleto:
`throw new UnsupportedOperationException("submeter ainda nao implementado");`

```
[ERROR] Tests run: 13, Failures: 8, Errors: 5  -- in CertificadoServiceTest
[ERROR] Tests run: 12, Failures: 0, Errors: 12 -- in RunCucumberTest
[INFO] Results:
[ERROR] Tests run: 25, Failures: 8, Errors: 17, Skipped: 0
[INFO] BUILD FAILURE

[ERROR]   CertificadoServiceTest.cargaHorariaZeroEhRejeitada:87
      Expecting actual throwable to be an instance of:
        br.facens.horascomplementares.domain.exception.CargaHorariaInvalidaException
      but was:
        java.lang.UnsupportedOperationException: submeter ainda nao implementado
[ERROR]   CertificadoServiceTest.dataFuturaEhRejeitada:119  (mesma causa)
[ERROR]   CertificadoServiceTest.certificadoDuplicadoEhRejeitado:148  (mesma causa)
[ERROR]   Submissão de certificado ... .Certificado válido nasce em análise  <<< ERROR!
      java.lang.UnsupportedOperationException: submeter ainda nao implementado
```

> RED confirmado: 25 testes executados, 0 passando na regra de negócio.

### GREEN — `mvn test` (implementação mínima de `CertificadoService.submeter`)

Implementação inline no serviço: busca aluno e categoria, valida carga horária
(1..200), valida data (não futura / não anterior ao ingresso), checa duplicidade
e persiste com status `EM_ANALISE`.

```
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 -- in CertificadoServiceTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 -- in RunCucumberTest
[INFO] Results:
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> GREEN confirmado: 25 testes passando (13 unidade + 12 cenários Gherkin da US-01).

### BLUE — refatoração `extrai CertificadoValidator`

As validações de carga horária e data saíram do serviço para a classe de domínio
pura `CertificadoValidator` (sem anotação Spring), com teste de unidade dedicado
`CertificadoValidatorTest` + `EntidadesDominioTest`. Testes seguem verdes.

```
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS   (mvn clean verify — JaCoCo 100% linha/branch em domain e service)
```

---

## US-02 — Validação pelo coordenador (autor: Micael Teodoro de Almeida)

### RED — `mvn test`

Escritos primeiro: `us02_validacao_coordenador.feature`, `ValidacaoCoordenadorSteps`,
`ValidacaoServiceTest`, `CertificadoTransicaoTest`. Métodos `Certificado.aprovar`,
`Certificado.reprovar` e `ValidacaoService.aprovar/reprovar` apenas como esqueleto
(`UnsupportedOperationException`).

```
[INFO]  CertificadoServiceTest        Tests run: 13, Failures: 0, Errors: 0   (US-01 continua verde)
[ERROR] ValidacaoServiceTest          Tests run: 6,  Failures: 3, Errors: 3
[ERROR] CertificadoTransicaoTest      Tests run: 12, Failures: 6, Errors: 6
[ERROR] RunCucumberTest               Tests run: 20, Failures: 0, Errors: 8   (8 cenarios da US-02)
[ERROR] Tests run: 62, Failures: 9, Errors: 17, Skipped: 0
[INFO] BUILD FAILURE

java.lang.UnsupportedOperationException: aprovar ainda nao implementado
java.lang.UnsupportedOperationException: reprovar ainda nao implementado
```

> RED confirmado: regras de transição de status e de justificativa ainda não existem.

### GREEN — `mvn test`

`Certificado.aprovar()` / `reprovar(justificativa)` com guarda de status
(`EM_ANALISE` obrigatório, `APROVADO` imutável) e mínimo de 10 caracteres na
justificativa. `ValidacaoService` orquestra e grava `HistoricoValidacao`.

```
[INFO] ValidacaoServiceTest      Tests run: 6,  Failures: 0, Errors: 0
[INFO] CertificadoTransicaoTest  Tests run: 12, Failures: 0, Errors: 0
[INFO] RunCucumberTest           Tests run: 20, Failures: 0, Errors: 0
[INFO] Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> GREEN confirmado: 65 testes passando (20 cenários Gherkin — US-01 + US-02).

### BLUE — refatoração `extrai Justificativa`

Objeto de valor `Justificativa` (domínio puro) encapsula a regra de tamanho
mínimo; `Certificado.reprovar` delega a validação a ele. Novos `JustificativaTest`
e cobertura de `HistoricoValidacao`.

```
[INFO] Tests run: 74, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS   (mvn clean verify — JaCoCo 100% linha/branch em domain e service)
```

---

## US-03 — Teto por categoria (autor: Nathalia Atamanchuk Baleeiro)

### RED — `mvn test`

Escritos primeiro: `us03_teto_por_categoria.feature`, `TetoCategoriaSteps`,
`CalculadoraDeHorasTest`, `HorasServiceTest`. `CalculadoraDeHoras.calcular` e
`HorasService.resumoDoAluno` apenas como esqueleto (`UnsupportedOperationException`).

```
[INFO]  CertificadoServiceTest / ValidacaoServiceTest / ...   verdes (US-01 e US-02)
[ERROR] CalculadoraDeHorasTest   Tests run: 7,  Failures: 0, Errors: 7
[ERROR] HorasServiceTest         Tests run: 2,  Failures: 0, Errors: 2
[ERROR] RunCucumberTest          Tests run: 28, Failures: 0, Errors: 8   (8 cenarios da US-03)
[ERROR] Tests run: 84, Failures: 0, Errors: 17, Skipped: 0
[INFO] BUILD FAILURE

CalculadoraDeHorasTest.totalGeralNuncaPassaDe200 » UnsupportedOperation calcular ainda nao implementado
```

> RED confirmado: cálculo de teto por categoria e limite geral de 200h ainda não existem.

### GREEN — `mvn test`

`CalculadoraDeHoras.calcular` agrupa os certificados aprovados por categoria,
aplica `min(horas, teto)` em cada uma e `min(total, 200)` no geral. `HorasService`
carrega apenas os `APROVADO` do aluno e delega ao domínio.

```
[INFO] CalculadoraDeHorasTest  Tests run: 7,  Failures: 0, Errors: 0
[INFO] HorasServiceTest        Tests run: 2,  Failures: 0, Errors: 0
[INFO] RunCucumberTest         Tests run: 28, Failures: 0, Errors: 0
[INFO] Tests run: 91, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> GREEN confirmado: 91 testes passando (28 cenários Gherkin — US-01 + US-02 + US-03).

### BLUE — refatoração `move regra de teto para ContribuicaoCategoria`

A aplicação do teto virou a fábrica `ContribuicaoCategoria.aplicarTeto(categoria, horas)`;
`CalculadoraDeHoras` ficou como orquestração (agrupamento + somatórios). Novo
`ContribuicaoCategoriaTest`.

```
[INFO] Tests run: 93, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS   (mvn clean verify — JaCoCo 100% linha/branch em domain e service)
```
