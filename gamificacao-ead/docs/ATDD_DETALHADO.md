# Prática ATDD – Gamificação para Engajamento de Educação Continuada

**Disciplina:** Testes de Software · **Equipe:** 3 integrantes
**Roteiro:** User Story → BDD (Cucumber) → TDD Red/Green/Blue (JUnit) → Jacoco 100%
**Domínio:** plataforma de cursos online/EAD por assinatura, com regras de gamificação

---

## 0. Regras de negócio extraídas do case

| RN | Regra | Onde vive no código |
|---|---|---|
| RN1 | Assinatura básica dá acesso a um conjunto de cursos | `Aluno.CURSOS_DA_ASSINATURA_BASICA` |
| RN2 | Curso terminado **com média acima de 7,0** dá direito a mais **3 cursos** | `Aluno.concluirCurso` |
| RN3 | Quem escreve mais tópicos **e** ajuda outros com comentários ganha **1 curso** no fim do mês | `GamificacaoService.premiarDestaqueDoForum` |
| RN4 | Ao conquistar **12 cursos**, o plano vira **Premium**, com **voucher** de projetos reais e **3 moedas** | `Aluno.promoverSeAtingiuAMeta` |
| RN5 | Moedas podem virar **conhecimento** (novos cursos), ser **acumuladas** ou viradas em **criptomoeda** | `Aluno.converterMoedas` + `DestinoMoeda` |

**Decisões de modelagem** (declare-as na apresentação, são o ponto que o professor cobra):

- "Conquistar um curso" = concluir **com média acima de 7,0**. Cursos concluídos com média igual ou
  inferior a 7,0 contam em `cursosConcluidos`, mas **não** em `cursosConquistados` e não liberam bônus.
  Por isso a promoção a Premium usa `cursosConquistados >= 12`.
- "Acima de 7,0" é comparação **estrita**: média exatamente 7,0 **não** libera os 3 cursos.
  Existe teste específico para essa fronteira.
- Para concluir um curso o aluno precisa ter saldo em `cursosDisponiveis` — é o que amarra
  a assinatura à gamificação e transforma o bônus em algo observável.
- `ACUMULO` é uma conversão válida cujo efeito é **manter** o saldo de moedas: o aluno declara
  que não vai gastar agora. O teste prova que nada muda (nem moedas, nem cursos, nem cripto).

---

## 1. User Stories (uma por integrante)

| # | Integrante | User Story |
|---|---|---|
| US1 | Integrante 1 | **COMO** aluno assinante da plataforma **QUERO** concluir um curso com bom aproveitamento **PARA** liberar novos cursos sem pagar nada a mais. |
| US2 | Integrante 2 | **COMO** aluno participante do fórum **QUERO** ter minhas contribuições reconhecidas **PARA** ganhar um curso ao final do mês. |
| US3 | Integrante 3 | **COMO** aluno **QUERO** evoluir para o plano Premium ao conquistar 12 cursos **PARA** receber voucher de projetos reais e moedas conversíveis em conhecimento, acúmulo ou criptomoeda. |

## 2. User Story escolhida pelo grupo

**US3 – Evoluir para Premium e usar as moedas.**

Justificativa: é a US com mais regras de negócio testáveis do case. Ela depende do acúmulo de
cursos conquistados (RN2), dispara três efeitos simultâneos na promoção (plano, voucher e moedas —
RN4) e ainda abre três caminhos distintos de conversão (RN5). Regra rica gera BDD com efeito
observável e TDD com asserts significativos — diferente de uma US que só faria CRUD.

## 3. BDD – um cenário por integrante (+ complementares)

### Cenário 1 – Integrante 1 (caminho feliz)
```gherkin
Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponíveis
Quando o aluno conclui o curso "Java Básico" com média 8,5
Então o aluno deve ter 7 cursos disponíveis
E o aluno deve ter 1 curso conquistado
```

### Cenário 2 – Integrante 2 (validação de dados)
```gherkin
Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponíveis
Quando o aluno tenta concluir o curso "Java Básico" com média 11,0
Então a operação deve ser recusada, com a mensagem "Media deve estar entre 0,0 e 10,0"
E o aluno deve ter 5 cursos disponíveis
```

### Cenário 3 – Integrante 3 (regra de negócio)
```gherkin
Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponíveis
E o aluno já conquistou 11 cursos com média acima de 7,0
Quando o aluno conclui o curso "Curso 12" com média 9,0
Então o plano do aluno deve ser "PREMIUM"
E o aluno deve possuir voucher para projetos reais
E o saldo de moedas do aluno deve ser 3
```

### Cenário 4 – complementar (prêmio mensal do fórum)
```gherkin
Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponíveis
E o aluno registrou 10 tópicos e 9 comentários no fórum
E o aluno "joao@teste.com" registrou 10 tópicos e 1 comentário no fórum
Quando a plataforma premia o destaque do fórum no fechamento do mês
Então o aluno "maria@teste.com" deve ter 6 cursos disponíveis
E a participação de "maria@teste.com" no fórum deve ser zerada
```

### Cenário 5 – complementar (conversão de moedas)
```gherkin
Dado um aluno "maria@teste.com" já promovido ao plano "PREMIUM"
Quando o aluno converte 3 moedas em "CONHECIMENTO"
Então o saldo de moedas do aluno deve ser 0
E o aluno deve ter 32 cursos disponíveis
```

> Os 32 do cenário 5 não são mágica: 5 da assinatura + 12 conclusões × (−1 curso gasto + 3 de bônus)
> = 29, mais as 3 moedas convertidas em curso. Deixe essa conta visível na apresentação.

## 4. Cada BDD representa uma funcionalidade válida

| Cenário | Regra do case | Por que é válido |
|---|---|---|
| 1 | RN2 – média acima de 7,0 libera 3 cursos | Fluxo principal: o aproveitamento vira benefício mensurável no saldo |
| 2 | RN2, pelo lado negativo | Média fora da faixa é estado inválido do domínio, não erro de tela; prova que o estado do aluno não é corrompido |
| 3 | RN4 – 12 cursos promovem a Premium | Regra com **três** efeitos observáveis de uma vez (plano, voucher e moedas) |
| 4 | RN3 – destaque do fórum ganha um curso | Prova a regra completa: mais tópicos **e** ter ajudado outros (o desempate exclui quem não comentou) |
| 5 | RN5 – moeda convertida em conhecimento | Fecha o ciclo da gamificação: benefício volta a virar curso |

---

## 5. Projeto Spring Boot

Criado no [start.spring.io](https://start.spring.io): **Maven · Java 17 · Spring Boot 3.3.x**
Dependências: **Spring Web**, **Spring Data JPA**, **H2 Database**.
O **Cucumber** é adicionado à mão no `pom.xml` (`cucumber-java`, `cucumber-spring`,
`cucumber-junit-platform-engine`, `junit-platform-suite`).

### Estrutura
```
gamificacao-ead/
├── pom.xml
└── src
    ├── main/java/br/facens/gamificacao/
    │   ├── GamificacaoApplication.java
    │   └── aluno/
    │       ├── domain/      Aluno, PlanoAssinatura, DestinoMoeda, GamificacaoException
    │       ├── repository/  AlunoRepository
    │       ├── service/     GamificacaoService
    │       └── web/         AlunoController
    ├── main/resources/application.properties
    └── test
        ├── java/br/facens/gamificacao/
        │   ├── cucumber/  RunCucumberTest, CucumberSpringConfiguration, GamificacaoSteps
        │   └── aluno/     AlunoTest, EnumsTest, GamificacaoServiceTest, AlunoControllerTest
        └── resources
            ├── features/gamificacao.feature
            └── junit-platform.properties
```

---

## 6. TDD – os três passos

### 6.1 RED – o teste que falha (1º passo do TDD)

Escreva **só o teste**, antes de existir a classe `Aluno`. Ele não compila / falha —
é exatamente essa a evidência pedida.

```java
@Test
public void deveLiberarTresCursosAoConcluirComMediaAcimaDeSete() {
    Aluno aluno = new Aluno("maria@teste.com");        // classe ainda não existe -> RED

    aluno.concluirCurso("Java Basico", new BigDecimal("8.5"));

    assertEquals(7, aluno.getCursosDisponiveis());     // 5 - 1 gasto + 3 de bonus
    assertEquals(1, aluno.getCursosConquistados());
    assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
}
```

> Tire o print do teste vermelho **antes** de criar as classes.

### 6.2 GREEN – fazer o teste passar

Criar as classes de domínio com o mínimo necessário e rodar `mvn test` até ficar verde.

### 6.3 BLUE – refatorar

1. As validações espalhadas (e-mail, nome do curso, faixa da média, saldo, moedas) viraram uma
   exceção de domínio única, `GamificacaoException`, com mensagens padronizadas — o controller
   traduz todas para HTTP 400 num único `@ExceptionHandler`.
2. A promoção a Premium saiu de `concluirCurso` e virou o método privado `promoverSeAtingiuAMeta()`,
   chamado só quando o curso é de fato conquistado. O efeito colateral triplo (plano + voucher +
   moedas) passou a ter um lugar só, impossível de esquecer.
3. Os números mágicos (7,0 · 3 cursos · 12 cursos · 3 moedas) viraram constantes públicas na
   entidade. O teste passou a citar a regra (`Aluno.CURSOS_PARA_PREMIUM`) em vez do literal.
4. O ranking do fórum saiu do laço `for` com variável "melhor até agora" e virou um `Stream` com
   `filter(Aluno::ajudouOutrosParticipantes)` + `Comparator.comparingInt(...).thenComparingInt(...)`.
   O critério de desempate ficou explícito no código.

Comportamento não mudou e os testes continuaram verdes depois da refatoração.

---

## 7. Código-fonte

### 7.1 pom.xml

**`pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>br.facens</groupId>
    <artifactId>gamificacao-ead</artifactId>
    <version>1.0.0</version>
    <name>gamificacao-ead</name>
    <description>Pratica ATDD - User Story, BDD (Cucumber) e TDD (JUnit + Jacoco)</description>

    <properties>
        <java.version>17</java.version>
        <cucumber.version>7.18.1</cucumber.version>
        <jacoco.version>0.8.12</jacoco.version>
    </properties>

    <dependencies>
        <!-- Spring Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Banco H2 em memoria -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JUnit 5 / Mockito / Spring Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Cucumber (BDD) -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-spring</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit-platform-engine</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-suite</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- Jacoco: instrumenta, gera o relatorio e REPROVA o build abaixo de 100% -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
                <configuration>
                    <excludes>
                        <!-- so o metodo main, sem regra de negocio -->
                        <exclude>br/facens/gamificacao/GamificacaoApplication.class</exclude>
                    </excludes>
                </configuration>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals><goal>prepare-agent</goal></goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals><goal>report</goal></goals>
                    </execution>
                    <execution>
                        <id>check</id>
                        <phase>verify</phase>
                        <goals><goal>check</goal></goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>1.00</minimum>
                                        </limit>
                                        <limit>
                                            <counter>BRANCH</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>1.00</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### 7.2 Configuração

**`src/main/resources/application.properties`**

```properties
spring.datasource.url=jdbc:h2:mem:gamificacao
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

**`src/test/resources/junit-platform.properties`**

```properties
cucumber.glue=br.facens.gamificacao.cucumber
cucumber.plugin=pretty, html:target/cucumber-report.html
cucumber.publish.quiet=true
```

### 7.3 Domínio

**`src/main/java/br/facens/gamificacao/GamificacaoApplication.java`**

```java
package br.facens.gamificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GamificacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamificacaoApplication.class, args);
    }
}
```

**`src/main/java/br/facens/gamificacao/aluno/domain/PlanoAssinatura.java`**

```java
package br.facens.gamificacao.aluno.domain;

public enum PlanoAssinatura {
    BASICO,
    PREMIUM
}
```

**`src/main/java/br/facens/gamificacao/aluno/domain/DestinoMoeda.java`**

```java
package br.facens.gamificacao.aluno.domain;

public enum DestinoMoeda {
    CONHECIMENTO,
    ACUMULO,
    CRIPTOMOEDA
}
```

**`src/main/java/br/facens/gamificacao/aluno/domain/GamificacaoException.java`**

```java
package br.facens.gamificacao.aluno.domain;

public class GamificacaoException extends RuntimeException {

    public GamificacaoException(String mensagem) {
        super(mensagem);
    }
}
```

**`src/main/java/br/facens/gamificacao/aluno/domain/Aluno.java`**

```java
package br.facens.gamificacao.aluno.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Aluno {

    public static final BigDecimal MEDIA_MINIMA = new BigDecimal("7.0");
    public static final BigDecimal MEDIA_MAXIMA = new BigDecimal("10.0");
    public static final int CURSOS_DA_ASSINATURA_BASICA = 5;
    public static final int CURSOS_LIBERADOS_POR_APROVACAO = 3;
    public static final int CURSOS_PARA_PREMIUM = 12;
    public static final int MOEDAS_DA_PROMOCAO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private PlanoAssinatura plano;

    private int cursosDisponiveis;
    private int cursosConcluidos;
    private int cursosConquistados;
    private int moedas;
    private int moedasEmCripto;
    private int topicosNoForum;
    private int comentariosNoForum;
    private boolean voucherProjetoReal;

    protected Aluno() {
        // exigido pelo JPA
    }

    public Aluno(String email) {
        if (email == null || email.isBlank()) {
            throw new GamificacaoException("E-mail do aluno e obrigatorio");
        }
        this.email = email;
        this.plano = PlanoAssinatura.BASICO;
        this.cursosDisponiveis = CURSOS_DA_ASSINATURA_BASICA;
    }

    /** RN2: curso terminado com media acima de 7,0 da direito a mais 3 cursos. */
    public void concluirCurso(String curso, BigDecimal media) {
        if (curso == null || curso.isBlank()) {
            throw new GamificacaoException("Nome do curso e obrigatorio");
        }
        if (media == null
                || media.compareTo(BigDecimal.ZERO) < 0
                || media.compareTo(MEDIA_MAXIMA) > 0) {
            throw new GamificacaoException("Media deve estar entre 0,0 e 10,0");
        }
        if (cursosDisponiveis <= 0) {
            throw new GamificacaoException("Aluno nao possui curso disponivel na assinatura");
        }

        this.cursosDisponiveis--;
        this.cursosConcluidos++;

        if (media.compareTo(MEDIA_MINIMA) > 0) {
            this.cursosConquistados++;
            this.cursosDisponiveis += CURSOS_LIBERADOS_POR_APROVACAO;
            promoverSeAtingiuAMeta();
        }
    }

    /** RN4: 12 cursos conquistados -> Premium, com voucher e 3 moedas. */
    private void promoverSeAtingiuAMeta() {
        if (plano == PlanoAssinatura.BASICO && cursosConquistados >= CURSOS_PARA_PREMIUM) {
            this.plano = PlanoAssinatura.PREMIUM;
            this.voucherProjetoReal = true;
            this.moedas += MOEDAS_DA_PROMOCAO;
        }
    }

    public void participarDoForum(int topicos, int comentarios) {
        if (topicos < 0 || comentarios < 0) {
            throw new GamificacaoException("Participacao no forum nao pode ser negativa");
        }
        this.topicosNoForum += topicos;
        this.comentariosNoForum += comentarios;
    }

    /** RN3: so concorre ao premio quem ajudou outros participantes com comentarios. */
    public boolean ajudouOutrosParticipantes() {
        return comentariosNoForum > 0;
    }

    public void receberPremioDoForum() {
        this.cursosDisponiveis++;
        this.topicosNoForum = 0;
        this.comentariosNoForum = 0;
    }

    /** RN5: moeda vira conhecimento, fica acumulada ou vira criptomoeda. */
    public void converterMoedas(int quantidade, DestinoMoeda destino) {
        if (plano != PlanoAssinatura.PREMIUM) {
            throw new GamificacaoException("Apenas aluno Premium possui moedas para converter");
        }
        if (destino == null) {
            throw new GamificacaoException("Destino da moeda e obrigatorio");
        }
        if (quantidade <= 0) {
            throw new GamificacaoException("Quantidade de moedas deve ser maior que zero");
        }
        if (quantidade > moedas) {
            throw new GamificacaoException("Saldo de moedas insuficiente");
        }

        if (destino == DestinoMoeda.CONHECIMENTO) {
            this.moedas -= quantidade;
            this.cursosDisponiveis += quantidade;
        } else if (destino == DestinoMoeda.CRIPTOMOEDA) {
            this.moedas -= quantidade;
            this.moedasEmCripto += quantidade;
        }
        // ACUMULO: o saldo permanece com o aluno
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public PlanoAssinatura getPlano() {
        return plano;
    }

    public int getCursosDisponiveis() {
        return cursosDisponiveis;
    }

    public int getCursosConcluidos() {
        return cursosConcluidos;
    }

    public int getCursosConquistados() {
        return cursosConquistados;
    }

    public int getMoedas() {
        return moedas;
    }

    public int getMoedasEmCripto() {
        return moedasEmCripto;
    }

    public int getTopicosNoForum() {
        return topicosNoForum;
    }

    public int getComentariosNoForum() {
        return comentariosNoForum;
    }

    public boolean isVoucherProjetoReal() {
        return voucherProjetoReal;
    }
}
```

### 7.4 Repositório, serviço e controller

**`src/main/java/br/facens/gamificacao/aluno/repository/AlunoRepository.java`**

```java
package br.facens.gamificacao.aluno.repository;

import br.facens.gamificacao.aluno.domain.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
}
```

**`src/main/java/br/facens/gamificacao/aluno/service/GamificacaoService.java`**

```java
package br.facens.gamificacao.aluno.service;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.repository.AlunoRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class GamificacaoService {

    private final AlunoRepository repository;

    public GamificacaoService(AlunoRepository repository) {
        this.repository = repository;
    }

    public Aluno matricular(String email) {
        return repository.save(new Aluno(email));
    }

    public Aluno concluirCurso(Long id, String curso, BigDecimal media) {
        Aluno aluno = buscar(id);
        aluno.concluirCurso(curso, media);
        return repository.save(aluno);
    }

    public Aluno participarDoForum(Long id, int topicos, int comentarios) {
        Aluno aluno = buscar(id);
        aluno.participarDoForum(topicos, comentarios);
        return repository.save(aluno);
    }

    public Aluno converterMoedas(Long id, int quantidade, DestinoMoeda destino) {
        Aluno aluno = buscar(id);
        aluno.converterMoedas(quantidade, destino);
        return repository.save(aluno);
    }

    /** RN3: no fechamento do mes, mais topicos vence; empate decide por comentarios. */
    public Aluno premiarDestaqueDoForum() {
        Aluno destaque = repository.findAll().stream()
                .filter(Aluno::ajudouOutrosParticipantes)
                .max(Comparator.comparingInt(Aluno::getTopicosNoForum)
                        .thenComparingInt(Aluno::getComentariosNoForum))
                .orElseThrow(() ->
                        new GamificacaoException("Nenhum aluno elegivel ao premio do forum"));

        destaque.receberPremioDoForum();
        return repository.save(destaque);
    }

    public Aluno buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new GamificacaoException("Aluno nao encontrado: " + id));
    }
}
```

**`src/main/java/br/facens/gamificacao/aluno/web/AlunoController.java`**

```java
package br.facens.gamificacao.aluno.web;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.service.GamificacaoService;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final GamificacaoService service;

    public AlunoController(GamificacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Aluno> matricular(@RequestParam String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.matricular(email));
    }

    @PostMapping("/{id}/conclusoes")
    public ResponseEntity<Aluno> concluirCurso(@PathVariable Long id,
                                               @RequestParam String curso,
                                               @RequestParam BigDecimal media) {
        return ResponseEntity.ok(service.concluirCurso(id, curso, media));
    }

    @PostMapping("/{id}/forum")
    public ResponseEntity<Aluno> participarDoForum(@PathVariable Long id,
                                                   @RequestParam int topicos,
                                                   @RequestParam int comentarios) {
        return ResponseEntity.ok(service.participarDoForum(id, topicos, comentarios));
    }

    @PostMapping("/{id}/moedas")
    public ResponseEntity<Aluno> converterMoedas(@PathVariable Long id,
                                                 @RequestParam int quantidade,
                                                 @RequestParam DestinoMoeda destino) {
        return ResponseEntity.ok(service.converterMoedas(id, quantidade, destino));
    }

    @PostMapping("/forum/premiacao")
    public ResponseEntity<Aluno> premiarDestaqueDoForum() {
        return ResponseEntity.ok(service.premiarDestaqueDoForum());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @ExceptionHandler(GamificacaoException.class)
    public ResponseEntity<String> tratarRegraViolada(GamificacaoException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }
}
```

### 7.5 BDD – feature e steps

**`src/test/resources/features/gamificacao.feature`**

```gherkin
# language: pt
Funcionalidade: Gamificacao para engajamento de educacao continuada

  Cenario: Concluir curso com media acima de 7,0 libera tres cursos
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    Quando o aluno conclui o curso "Java Basico" com media 8.5
    Entao o aluno deve ter 7 cursos disponiveis
    E o aluno deve ter 1 curso conquistado

  Cenario: Media fora da faixa e recusada
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    Quando o aluno tenta concluir o curso "Java Basico" com media 11.0
    Entao a operacao deve ser recusada com a mensagem "Media deve estar entre 0,0 e 10,0"
    E o aluno deve ter 5 cursos disponiveis

  Cenario: Conquistar 12 cursos promove o aluno a Premium
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    E o aluno ja conquistou 11 cursos com media acima de 7,0
    Quando o aluno conclui o curso "Curso 12" com media 9.0
    Entao o plano do aluno deve ser "PREMIUM"
    E o aluno deve possuir voucher para projetos reais
    E o saldo de moedas do aluno deve ser 3

  Cenario: Destaque do forum ganha um curso no fim do mes
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    E o aluno registrou 10 topicos e 9 comentarios no forum
    E existe o aluno "joao@teste.com" com 10 topicos e 1 comentario no forum
    Quando a plataforma premia o destaque do forum
    Entao o aluno deve ter 6 cursos disponiveis
    E a participacao do aluno no forum deve estar zerada

  Cenario: Aluno Premium converte moedas em conhecimento
    Dado um aluno "maria@teste.com" ja promovido ao plano "PREMIUM"
    Quando o aluno converte 3 moedas em "CONHECIMENTO"
    Entao o saldo de moedas do aluno deve ser 0
    E o aluno deve ter 32 cursos disponiveis
```

**`src/test/java/br/facens/gamificacao/cucumber/RunCucumberTest.java`**

```java
package br.facens.gamificacao.cucumber;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.facens.gamificacao.cucumber")
public class RunCucumberTest {
}
```

**`src/test/java/br/facens/gamificacao/cucumber/CucumberSpringConfiguration.java`**

```java
package br.facens.gamificacao.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest
public class CucumberSpringConfiguration {
}
```

**`src/test/java/br/facens/gamificacao/cucumber/GamificacaoSteps.java`**

```java
package br.facens.gamificacao.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.domain.PlanoAssinatura;
import br.facens.gamificacao.aluno.repository.AlunoRepository;
import br.facens.gamificacao.aluno.service.GamificacaoService;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;

public class GamificacaoSteps {

    @Autowired
    private GamificacaoService service;

    @Autowired
    private AlunoRepository repository;

    private Aluno aluno;
    private GamificacaoException erro;

    @Before
    public void limparBase() {
        repository.deleteAll();
        aluno = null;
        erro = null;
    }

    @Dado("um aluno {string} no plano {string} com {int} cursos disponiveis")
    public void umAlunoNoPlanoComCursos(String email, String plano, int cursos) {
        aluno = service.matricular(email);
        assertEquals(PlanoAssinatura.valueOf(plano), aluno.getPlano());
        assertEquals(cursos, aluno.getCursosDisponiveis());
    }

    @Dado("o aluno ja conquistou {int} cursos com media acima de 7,0")
    public void oAlunoJaConquistou(int quantidade) {
        for (int i = 1; i <= quantidade; i++) {
            aluno = service.concluirCurso(aluno.getId(), "Curso " + i, new BigDecimal("8.0"));
        }
        assertEquals(quantidade, aluno.getCursosConquistados());
    }

    @Dado("um aluno {string} ja promovido ao plano {string}")
    public void umAlunoJaPromovido(String email, String plano) {
        aluno = service.matricular(email);
        for (int i = 1; i <= Aluno.CURSOS_PARA_PREMIUM; i++) {
            aluno = service.concluirCurso(aluno.getId(), "Curso " + i, new BigDecimal("8.0"));
        }
        assertEquals(PlanoAssinatura.valueOf(plano), aluno.getPlano());
    }

    @Dado("o aluno registrou {int} topicos e {int} comentarios no forum")
    public void oAlunoRegistrouParticipacao(int topicos, int comentarios) {
        aluno = service.participarDoForum(aluno.getId(), topicos, comentarios);
    }

    @Dado("existe o aluno {string} com {int} topicos e {int} comentario no forum")
    public void existeOutroAluno(String email, int topicos, int comentarios) {
        Aluno outro = service.matricular(email);
        service.participarDoForum(outro.getId(), topicos, comentarios);
    }

    @Quando("o aluno conclui o curso {string} com media {double}")
    public void oAlunoConcluiOCurso(String curso, double media) {
        aluno = service.concluirCurso(aluno.getId(), curso, BigDecimal.valueOf(media));
    }

    @Quando("o aluno tenta concluir o curso {string} com media {double}")
    public void oAlunoTentaConcluirOCurso(String curso, double media) {
        try {
            aluno = service.concluirCurso(aluno.getId(), curso, BigDecimal.valueOf(media));
        } catch (GamificacaoException e) {
            erro = e;
        }
    }

    @Quando("o aluno converte {int} moedas em {string}")
    public void oAlunoConverteMoedas(int quantidade, String destino) {
        aluno = service.converterMoedas(aluno.getId(), quantidade, DestinoMoeda.valueOf(destino));
    }

    @Quando("a plataforma premia o destaque do forum")
    public void aPlataformaPremiaODestaque() {
        service.premiarDestaqueDoForum();
        aluno = service.buscar(aluno.getId());
    }

    @Entao("o aluno deve ter {int} cursos disponiveis")
    public void oAlunoDeveTerCursosDisponiveis(int esperado) {
        assertEquals(esperado, service.buscar(aluno.getId()).getCursosDisponiveis());
    }

    @Entao("o aluno deve ter {int} curso conquistado")
    public void oAlunoDeveTerCursoConquistado(int esperado) {
        assertEquals(esperado, aluno.getCursosConquistados());
    }

    @Entao("a operacao deve ser recusada com a mensagem {string}")
    public void aOperacaoDeveSerRecusada(String mensagem) {
        assertNotNull(erro);
        assertEquals(mensagem, erro.getMessage());
    }

    @Entao("o plano do aluno deve ser {string}")
    public void oPlanoDoAlunoDeveSer(String plano) {
        assertEquals(PlanoAssinatura.valueOf(plano), aluno.getPlano());
    }

    @Entao("o aluno deve possuir voucher para projetos reais")
    public void oAlunoDevePossuirVoucher() {
        assertTrue(aluno.isVoucherProjetoReal());
    }

    @Entao("o saldo de moedas do aluno deve ser {int}")
    public void oSaldoDeMoedasDeveSer(int esperado) {
        assertEquals(esperado, aluno.getMoedas());
    }

    @Entao("a participacao do aluno no forum deve estar zerada")
    public void aParticipacaoDeveEstarZerada() {
        assertEquals(0, aluno.getTopicosNoForum());
        assertEquals(0, aluno.getComentariosNoForum());
    }
}
```

### 7.6 Testes de unidade

**`src/test/java/br/facens/gamificacao/aluno/domain/AlunoTest.java`**

```java
package br.facens.gamificacao.aluno.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlunoTest {

    private static final BigDecimal APROVADO = new BigDecimal("8.5");

    private Aluno alunoPremium() {
        Aluno aluno = new Aluno("maria@teste.com");
        for (int i = 1; i <= Aluno.CURSOS_PARA_PREMIUM; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("8.0"));
        }
        return aluno;
    }

    @Test
    @DisplayName("CT-01 - deve liberar 3 cursos ao concluir com media acima de 7,0")
    void deveLiberarTresCursosAoConcluirComMediaAcimaDeSete() {
        Aluno aluno = new Aluno("maria@teste.com");
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA, aluno.getCursosDisponiveis());

        aluno.concluirCurso("Java Basico", APROVADO);

        assertEquals(7, aluno.getCursosDisponiveis());
        assertEquals(1, aluno.getCursosConquistados());
        assertEquals(1, aluno.getCursosConcluidos());
        assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
        assertEquals("maria@teste.com", aluno.getEmail());
        assertFalse(aluno.isVoucherProjetoReal());
    }

    @Test
    @DisplayName("CT-02 - media exatamente 7,0 nao libera cursos extras")
    void mediaExatamenteSeteNaoLiberaCursos() {
        Aluno aluno = new Aluno("maria@teste.com");

        aluno.concluirCurso("Scrum", new BigDecimal("7.0"));

        assertEquals(4, aluno.getCursosDisponiveis());
        assertEquals(0, aluno.getCursosConquistados());
        assertEquals(1, aluno.getCursosConcluidos());
    }

    @Test
    @DisplayName("CT-03 - nome do curso e obrigatorio")
    void nomeDoCursoEObrigatorio() {
        Aluno aluno = new Aluno("maria@teste.com");

        assertEquals("Nome do curso e obrigatorio",
                assertThrows(GamificacaoException.class,
                        () -> aluno.concluirCurso(null, APROVADO)).getMessage());
        assertEquals("Nome do curso e obrigatorio",
                assertThrows(GamificacaoException.class,
                        () -> aluno.concluirCurso("   ", APROVADO)).getMessage());
    }

    @Test
    @DisplayName("CT-04 - media deve estar entre 0,0 e 10,0")
    void mediaForaDaFaixaERecusada() {
        Aluno aluno = new Aluno("maria@teste.com");
        String esperada = "Media deve estar entre 0,0 e 10,0";

        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.concluirCurso("Java", null)).getMessage());
        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.concluirCurso("Java", new BigDecimal("-1.0"))).getMessage());
        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.concluirCurso("Java", new BigDecimal("11.0"))).getMessage());

        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA, aluno.getCursosDisponiveis());
        assertEquals(0, aluno.getCursosConcluidos());
    }

    @Test
    @DisplayName("CT-05 - nao deve concluir curso sem saldo na assinatura")
    void naoDeveConcluirSemSaldo() {
        Aluno aluno = new Aluno("maria@teste.com");
        for (int i = 1; i <= Aluno.CURSOS_DA_ASSINATURA_BASICA; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("6.0"));
        }
        assertEquals(0, aluno.getCursosDisponiveis());

        assertEquals("Aluno nao possui curso disponivel na assinatura",
                assertThrows(GamificacaoException.class,
                        () -> aluno.concluirCurso("Curso 6", APROVADO)).getMessage());
    }

    @Test
    @DisplayName("CT-06 - e-mail do aluno e obrigatorio")
    void emailEObrigatorio() {
        assertEquals("E-mail do aluno e obrigatorio",
                assertThrows(GamificacaoException.class, () -> new Aluno(null)).getMessage());
        assertEquals("E-mail do aluno e obrigatorio",
                assertThrows(GamificacaoException.class, () -> new Aluno(" ")).getMessage());
    }

    @Test
    @DisplayName("CT-07 - ao conquistar 12 cursos vira Premium com voucher e 3 moedas")
    void deveVirarPremiumAoConquistarDozeCursos() {
        Aluno aluno = new Aluno("maria@teste.com");
        for (int i = 1; i <= 11; i++) {
            aluno.concluirCurso("Curso " + i, new BigDecimal("8.0"));
        }
        assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
        assertEquals(0, aluno.getMoedas());

        aluno.concluirCurso("Curso 12", new BigDecimal("9.0"));

        assertEquals(PlanoAssinatura.PREMIUM, aluno.getPlano());
        assertTrue(aluno.isVoucherProjetoReal());
        assertEquals(Aluno.MOEDAS_DA_PROMOCAO, aluno.getMoedas());
        assertEquals(12, aluno.getCursosConquistados());
        assertEquals(29, aluno.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-08 - aluno ja Premium nao recebe a promocao duas vezes")
    void naoDevePromoverDuasVezes() {
        Aluno aluno = alunoPremium();

        aluno.concluirCurso("Curso 13", new BigDecimal("10.0"));

        assertEquals(Aluno.MOEDAS_DA_PROMOCAO, aluno.getMoedas());
        assertEquals(13, aluno.getCursosConquistados());
        assertEquals(PlanoAssinatura.PREMIUM, aluno.getPlano());
    }

    @Test
    @DisplayName("CT-09 - aluno Basico nao possui moedas para converter")
    void alunoBasicoNaoConverteMoedas() {
        Aluno aluno = new Aluno("maria@teste.com");

        assertEquals("Apenas aluno Premium possui moedas para converter",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(1, DestinoMoeda.CONHECIMENTO)).getMessage());
    }

    @Test
    @DisplayName("CT-10 - destino da moeda e obrigatorio")
    void destinoDaMoedaEObrigatorio() {
        Aluno aluno = alunoPremium();

        assertEquals("Destino da moeda e obrigatorio",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(1, null)).getMessage());
    }

    @Test
    @DisplayName("CT-11 - quantidade de moedas deve ser maior que zero")
    void quantidadeDeMoedasDeveSerPositiva() {
        Aluno aluno = alunoPremium();

        assertEquals("Quantidade de moedas deve ser maior que zero",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(0, DestinoMoeda.ACUMULO)).getMessage());
    }

    @Test
    @DisplayName("CT-12 - nao deve converter mais moedas do que possui")
    void naoDeveConverterAlemDoSaldo() {
        Aluno aluno = alunoPremium();

        assertEquals("Saldo de moedas insuficiente",
                assertThrows(GamificacaoException.class,
                        () -> aluno.converterMoedas(4, DestinoMoeda.CONHECIMENTO)).getMessage());
    }

    @Test
    @DisplayName("CT-13 - deve converter moedas em conhecimento (novos cursos)")
    void deveConverterMoedasEmConhecimento() {
        Aluno aluno = alunoPremium();
        int cursosAntes = aluno.getCursosDisponiveis();

        aluno.converterMoedas(3, DestinoMoeda.CONHECIMENTO);

        assertEquals(0, aluno.getMoedas());
        assertEquals(cursosAntes + 3, aluno.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-14 - deve converter moedas em criptomoeda")
    void deveConverterMoedasEmCripto() {
        Aluno aluno = alunoPremium();

        aluno.converterMoedas(2, DestinoMoeda.CRIPTOMOEDA);

        assertEquals(1, aluno.getMoedas());
        assertEquals(2, aluno.getMoedasEmCripto());
    }

    @Test
    @DisplayName("CT-15 - acumular mantem o saldo de moedas com o aluno")
    void deveAcumularMoedas() {
        Aluno aluno = alunoPremium();
        int cursosAntes = aluno.getCursosDisponiveis();

        aluno.converterMoedas(3, DestinoMoeda.ACUMULO);

        assertEquals(3, aluno.getMoedas());
        assertEquals(0, aluno.getMoedasEmCripto());
        assertEquals(cursosAntes, aluno.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-16 - participacao no forum nao pode ser negativa")
    void participacaoNegativaERecusada() {
        Aluno aluno = new Aluno("maria@teste.com");
        String esperada = "Participacao no forum nao pode ser negativa";

        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.participarDoForum(-1, 0)).getMessage());
        assertEquals(esperada, assertThrows(GamificacaoException.class,
                () -> aluno.participarDoForum(0, -1)).getMessage());
    }

    @Test
    @DisplayName("CT-17 - participacao no forum acumula e identifica quem ajudou outros")
    void participacaoAcumula() {
        Aluno aluno = new Aluno("maria@teste.com");
        assertFalse(aluno.ajudouOutrosParticipantes());

        aluno.participarDoForum(4, 6);
        aluno.participarDoForum(1, 2);

        assertEquals(5, aluno.getTopicosNoForum());
        assertEquals(8, aluno.getComentariosNoForum());
        assertTrue(aluno.ajudouOutrosParticipantes());
    }

    @Test
    @DisplayName("CT-18 - premio do forum da 1 curso e zera a participacao do mes")
    void premioDoForumDaUmCurso() {
        Aluno aluno = new Aluno("maria@teste.com");
        aluno.participarDoForum(10, 9);

        aluno.receberPremioDoForum();

        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA + 1, aluno.getCursosDisponiveis());
        assertEquals(0, aluno.getTopicosNoForum());
        assertEquals(0, aluno.getComentariosNoForum());
    }

    @Test
    @DisplayName("CT-19 - construtor protegido exigido pelo JPA")
    void construtorDoJpa() {
        Aluno aluno = new Aluno();

        assertNotNull(aluno);
        assertNull(aluno.getId());
        assertNull(aluno.getEmail());
        assertNull(aluno.getPlano());
    }
}
```

**`src/test/java/br/facens/gamificacao/aluno/domain/EnumsTest.java`**

```java
package br.facens.gamificacao.aluno.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnumsTest {

    @Test
    @DisplayName("CT-ENUM-01 - PlanoAssinatura tem BASICO e PREMIUM")
    void planoAssinatura() {
        assertEquals(2, PlanoAssinatura.values().length);
        assertEquals(PlanoAssinatura.BASICO, PlanoAssinatura.valueOf("BASICO"));
        assertEquals(PlanoAssinatura.PREMIUM, PlanoAssinatura.valueOf("PREMIUM"));
    }

    @Test
    @DisplayName("CT-ENUM-02 - DestinoMoeda tem os tres destinos do case")
    void destinoMoeda() {
        assertEquals(3, DestinoMoeda.values().length);
        assertEquals(DestinoMoeda.CONHECIMENTO, DestinoMoeda.valueOf("CONHECIMENTO"));
        assertEquals(DestinoMoeda.ACUMULO, DestinoMoeda.valueOf("ACUMULO"));
        assertEquals(DestinoMoeda.CRIPTOMOEDA, DestinoMoeda.valueOf("CRIPTOMOEDA"));
    }
}
```

**`src/test/java/br/facens/gamificacao/aluno/service/GamificacaoServiceTest.java`**

```java
package br.facens.gamificacao.aluno.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.domain.PlanoAssinatura;
import br.facens.gamificacao.aluno.repository.AlunoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GamificacaoServiceTest {

    @Mock
    private AlunoRepository repository;

    private GamificacaoService service;

    @BeforeEach
    void setUp() {
        service = new GamificacaoService(repository);
        org.mockito.Mockito.lenient()
                .when(repository.save(any(Aluno.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Aluno comForum(String email, int topicos, int comentarios) {
        Aluno aluno = new Aluno(email);
        aluno.participarDoForum(topicos, comentarios);
        return aluno;
    }

    @Test
    @DisplayName("CT-SRV-01 - deve matricular aluno no plano Basico")
    void deveMatricular() {
        Aluno aluno = service.matricular("maria@teste.com");

        assertEquals(PlanoAssinatura.BASICO, aluno.getPlano());
        assertEquals("maria@teste.com", aluno.getEmail());
    }

    @Test
    @DisplayName("CT-SRV-02 - deve concluir curso e liberar o bonus")
    void deveConcluirCurso() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Aluno("maria@teste.com")));

        Aluno aluno = service.concluirCurso(1L, "Java Basico", new BigDecimal("8.5"));

        assertEquals(7, aluno.getCursosDisponiveis());
        assertEquals(1, aluno.getCursosConquistados());
    }

    @Test
    @DisplayName("CT-SRV-03 - deve registrar participacao no forum")
    void deveRegistrarParticipacao() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Aluno("maria@teste.com")));

        Aluno aluno = service.participarDoForum(1L, 3, 4);

        assertEquals(3, aluno.getTopicosNoForum());
        assertEquals(4, aluno.getComentariosNoForum());
    }

    @Test
    @DisplayName("CT-SRV-04 - deve converter moedas de aluno Premium")
    void deveConverterMoedas() {
        Aluno premium = new Aluno("maria@teste.com");
        for (int i = 1; i <= Aluno.CURSOS_PARA_PREMIUM; i++) {
            premium.concluirCurso("Curso " + i, new BigDecimal("8.0"));
        }
        when(repository.findById(1L)).thenReturn(Optional.of(premium));

        Aluno aluno = service.converterMoedas(1L, 3, DestinoMoeda.CRIPTOMOEDA);

        assertEquals(0, aluno.getMoedas());
        assertEquals(3, aluno.getMoedasEmCripto());
    }

    @Test
    @DisplayName("CT-SRV-05 - premio do forum vai para quem tem mais topicos, desempatando por comentarios")
    void devePremiarODestaqueDoForum() {
        Aluno poucosTopicos = comForum("um@teste.com", 4, 20);
        Aluno empatadoFraco = comForum("dois@teste.com", 10, 1);
        Aluno destaque = comForum("tres@teste.com", 10, 9);
        when(repository.findAll())
                .thenReturn(List.of(poucosTopicos, empatadoFraco, destaque));

        Aluno premiado = service.premiarDestaqueDoForum();

        assertEquals("tres@teste.com", premiado.getEmail());
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA + 1, premiado.getCursosDisponiveis());
        assertEquals(Aluno.CURSOS_DA_ASSINATURA_BASICA, empatadoFraco.getCursosDisponiveis());
    }

    @Test
    @DisplayName("CT-SRV-06 - quem nao ajudou outros participantes nao concorre ao premio")
    void naoDevePremiarQuemNaoComentou() {
        when(repository.findAll()).thenReturn(List.of(comForum("solo@teste.com", 30, 0)));

        assertEquals("Nenhum aluno elegivel ao premio do forum",
                assertThrows(GamificacaoException.class,
                        () -> service.premiarDestaqueDoForum()).getMessage());
    }

    @Test
    @DisplayName("CT-SRV-07 - deve buscar aluno existente")
    void deveBuscarAluno() {
        Aluno aluno = new Aluno("maria@teste.com");
        when(repository.findById(1L)).thenReturn(Optional.of(aluno));

        assertEquals(aluno, service.buscar(1L));
    }

    @Test
    @DisplayName("CT-SRV-08 - deve falhar ao buscar aluno inexistente")
    void deveFalharAoBuscarAlunoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertEquals("Aluno nao encontrado: 99",
                assertThrows(GamificacaoException.class, () -> service.buscar(99L)).getMessage());
    }
}
```

**`src/test/java/br/facens/gamificacao/aluno/web/AlunoControllerTest.java`**

```java
package br.facens.gamificacao.aluno.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.service.GamificacaoService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AlunoControllerTest {

    @Mock
    private GamificacaoService service;

    private AlunoController controller;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        controller = new AlunoController(service);
        aluno = new Aluno("maria@teste.com");
    }

    @Test
    @DisplayName("CT-API-01 - POST /alunos retorna 201")
    void matricularRetorna201() {
        when(service.matricular(anyString())).thenReturn(aluno);

        assertEquals(HttpStatus.CREATED, controller.matricular("maria@teste.com").getStatusCode());
    }

    @Test
    @DisplayName("CT-API-02 - POST /alunos/{id}/conclusoes retorna 200")
    void concluirCursoRetorna200() {
        when(service.concluirCurso(anyLong(), anyString(), any(BigDecimal.class))).thenReturn(aluno);

        assertEquals(HttpStatus.OK, controller
                .concluirCurso(1L, "Java Basico", new BigDecimal("8.5"))
                .getStatusCode());
    }

    @Test
    @DisplayName("CT-API-03 - forum, moedas, premiacao e busca retornam 200")
    void demaisEndpointsRetornam200() {
        when(service.participarDoForum(anyLong(), anyInt(), anyInt())).thenReturn(aluno);
        when(service.converterMoedas(anyLong(), anyInt(), any(DestinoMoeda.class))).thenReturn(aluno);
        when(service.premiarDestaqueDoForum()).thenReturn(aluno);
        when(service.buscar(1L)).thenReturn(aluno);

        assertEquals(HttpStatus.OK, controller.participarDoForum(1L, 3, 4).getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.converterMoedas(1L, 1, DestinoMoeda.CONHECIMENTO).getStatusCode());
        assertEquals(HttpStatus.OK, controller.premiarDestaqueDoForum().getStatusCode());
        assertEquals(HttpStatus.OK, controller.buscar(1L).getStatusCode());
    }

    @Test
    @DisplayName("CT-API-04 - regra de negocio violada retorna 400 com a mensagem")
    void erroDeNegocioRetorna400() {
        var resposta = controller.tratarRegraViolada(
                new GamificacaoException("Media deve estar entre 0,0 e 10,0"));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("Media deve estar entre 0,0 e 10,0", resposta.getBody());
    }
}
```

---

## 8. Jacoco – cobertura 100%

```bash
mvn clean test      # roda JUnit + Cucumber e gera o relatório
mvn clean verify    # idem, e REPROVA o build se a cobertura ficar abaixo de 100%
```

Relatório: **`target/site/jacoco/index.html`** — abra no navegador e tire o print com tudo verde.

Como o 100% foi alcançado:

- Todo `if` tem teste do caso verdadeiro **e** do falso. Nas condições compostas, cada operando
  é exercitado isoladamente — na validação da média, um teste para `null`, um para negativo e
  um para acima de 10,0, além do caminho válido.
- A promoção a Premium (`plano == BASICO && conquistados >= 12`) tem os dois lados de cada operando:
  CT-07 cobre o `true/true`, os cursos 1 a 11 cobrem o segundo operando falso e CT-08 (13º curso,
  aluno já Premium) cobre o primeiro falso.
- Os três valores de `DestinoMoeda` têm teste próprio (CT-13, CT-14, CT-15), fechando o
  `if / else if` sem deixar ramo órfão.
- O construtor protegido exigido pelo JPA é exercitado por CT-19, que fica no mesmo pacote.
- `values()` e `valueOf()` dos enums têm teste próprio (`EnumsTest`) — é o que costuma derrubar o 100%.
- `GamificacaoApplication` (só o método `main`, sem regra de negócio) está excluída no `pom.xml`.

### Rastreabilidade cenário → teste → assertiva

| Cenário BDD | Teste de unidade | Assertiva que comprova o critério de aceite |
|---|---|---|
| 1 – Conclusão libera 3 cursos | `deveLiberarTresCursosAoConcluirComMediaAcimaDeSete` | `assertEquals(7, aluno.getCursosDisponiveis())` |
| 2 – Média fora da faixa | `mediaForaDaFaixaERecusada` | `assertEquals("Media deve estar entre 0,0 e 10,0", erro.getMessage())` |
| 3 – Promoção a Premium | `deveVirarPremiumAoConquistarDozeCursos` | `assertEquals(PlanoAssinatura.PREMIUM, aluno.getPlano())` + voucher + 3 moedas |
| 4 – Destaque do fórum | `devePremiarODestaqueDoForum` | `assertEquals("tres@teste.com", premiado.getEmail())` e `+1` curso |
| 5 – Moeda vira conhecimento | `deveConverterMoedasEmConhecimento` | `assertEquals(0, aluno.getMoedas())` e cursos `+3` |

---

## 9. Entrega e avaliação

| Entregável | Evidência |
|---|---|
| Product Backlog + BDD + TDD | planilha ATDD preenchida |
| Projeto Spring Boot | código-fonte / repositório Git |
| RED | print do teste falhando antes das classes existirem |
| GREEN | print do `mvn test` com tudo verde |
| BLUE | descrição das refatorações (item 6.3) + testes ainda verdes |
| Jacoco | print de `target/site/jacoco/index.html` com 100% |
| Cucumber | `target/cucumber-report.html` com os 5 cenários passando |

**Nota (quadro):** equipe 3,0 pontos + individual 4,0 pontos.
Divisão individual sugerida: cada integrante apresenta a **sua US**, o **seu cenário BDD** e o
**teste de unidade correspondente** — Integrante 1 → `deveLiberarTresCursosAoConcluirComMediaAcimaDeSete`
e `mediaExatamenteSeteNaoLiberaCursos`; Integrante 2 → `devePremiarODestaqueDoForum` e
`naoDevePremiarQuemNaoComentou`; Integrante 3 → `deveVirarPremiumAoConquistarDozeCursos` e
`deveConverterMoedasEmConhecimento`.

### Comandos rápidos
```bash
mvn clean test                   # unidade + BDD
mvn test -Dtest=RunCucumberTest  # só os cenários BDD
mvn clean verify                 # falha o build se a cobertura cair abaixo de 100%
mvn spring-boot:run              # sobe a API em http://localhost:8080
```

### Chamadas de exemplo

```bash
curl -X POST "http://localhost:8080/alunos?email=maria@teste.com"
curl -X POST "http://localhost:8080/alunos/1/conclusoes?curso=Java%20Basico&media=8.5"
curl -X POST "http://localhost:8080/alunos/1/forum?topicos=10&comentarios=9"
curl -X POST "http://localhost:8080/alunos/forum/premiacao"
curl -X POST "http://localhost:8080/alunos/1/moedas?quantidade=3&destino=CONHECIMENTO"
curl "http://localhost:8080/alunos/1"
```
