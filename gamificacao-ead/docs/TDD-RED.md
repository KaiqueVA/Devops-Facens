# Passo 1 do TDD: o teste escrito para falhar

O professor pede a evidencia do **primeiro passo do TDD**: o teste que existe antes do codigo
de producao e por isso **falha**. Como o projeto ja esta completo neste repositorio, para
reproduzir o RED faca o seguinte.

## Como reproduzir o RED em 1 minuto

```bash
git checkout -b red

# remove o codigo de producao do dominio, mantendo os testes
rm -rf src/main/java/br/facens/gamificacao/aluno
rm -rf src/test/java/br/facens/gamificacao/cucumber
rm -rf src/test/java/br/facens/gamificacao/aluno/service
rm -rf src/test/java/br/facens/gamificacao/aluno/web

mvn clean test      # COMPILATION ERROR / BUILD FAILURE  <- este e o print do RED
```

Printe a saida do terminal em `docs/img/01-red.png`, volte para a `main`
(`git checkout main`) e rode `mvn clean test` de novo para o print do GREEN.

> Uma alternativa mais fiel ao processo e criar tres commits na ordem
> `RED -> GREEN -> BLUE` (ver secao "Historico de commits" no README). O historico do Git
> vira, sozinho, a evidencia de que o TDD foi seguido.

## O teste do RED (cenario 1, US3)

```java
@Test
@DisplayName("CT-01 - deve liberar 3 cursos ao concluir com media acima de 7,0")
void deveLiberarTresCursosAoConcluirComMediaAcimaDeSete() {
    Aluno aluno = new Aluno("maria@teste.com");   // a classe Aluno ainda nao existe -> RED

    aluno.concluirCurso("Java Basico", new BigDecimal("8.5"));

    assertEquals(7, aluno.getCursosDisponiveis());  // 5 da assinatura - 1 gasto + 3 de bonus
    assertEquals(1, aluno.getCursosConquistados());
    assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
}
```

Erro esperado no console:

```
[ERROR] COMPILATION ERROR :
[ERROR] .../AlunoTest.java:[33,22] cannot find symbol
  symbol:   class Aluno
  location: package br.facens.gamificacao.aluno.domain
[ERROR] BUILD FAILURE
```

Um teste que **nao compila** e uma falha valida do passo RED: ele prova que a assertiva
depende de um comportamento que ainda nao existe.
