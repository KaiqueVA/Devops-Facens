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
