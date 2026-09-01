# language: pt
Funcionalidade: Submissão de certificado de atividade complementar
  Como aluno da faculdade
  Quero submeter certificados das atividades que realizei
  Para que eles sejam analisados e contabilizados nas minhas horas complementares

  Contexto:
    Dado o catálogo de categorias padrão da faculdade
    E um aluno chamado "Ana" que ingressou em "2023-01-01"
    E que a data de hoje é "2024-06-01"

  Cenário: Certificado válido nasce em análise e ainda não soma horas
    Quando o aluno submete um certificado "Curso de Java" da categoria "ENSINO" com 40 horas na data "2024-03-10"
    Então o certificado é registrado com status "EM_ANALISE"
    E o total de horas aprovadas do aluno é 0

  Cenário: Data igual à data de ingresso é aceita (borda)
    Quando o aluno submete um certificado "Semana de Recepção" da categoria "EVENTO" com 8 horas na data "2023-01-01"
    Então o certificado é registrado com status "EM_ANALISE"

  Cenário: Data igual ao dia de hoje é aceita (borda)
    Quando o aluno submete um certificado "Palestra de Hoje" da categoria "EVENTO" com 2 horas na data "2024-06-01"
    Então o certificado é registrado com status "EM_ANALISE"

  Cenário: Carga horária de exatamente 200 horas é aceita (borda)
    Quando o aluno submete um certificado "Projeto Extenso" da categoria "EXTENSAO" com 200 horas na data "2024-02-01"
    Então o certificado é registrado com status "EM_ANALISE"

  Esquema do Cenário: Carga horária fora da faixa permitida é rejeitada
    Quando o aluno submete um certificado "Atividade X" da categoria "ENSINO" com <horas> horas na data "2024-03-10"
    Então a submissão é rejeitada com a mensagem contendo "Carga horaria invalida"

    Exemplos:
      | horas |
      | 0     |
      | -5    |
      | 201   |
      | 500   |

  Cenário: Data futura é rejeitada
    Quando o aluno submete um certificado "Evento Futuro" da categoria "EVENTO" com 10 horas na data "2024-12-31"
    Então a submissão é rejeitada com a mensagem contendo "futuro"

  Cenário: Data anterior ao ingresso do aluno é rejeitada
    Quando o aluno submete um certificado "Curso Antigo" da categoria "ENSINO" com 10 horas na data "2022-10-10"
    Então a submissão é rejeitada com a mensagem contendo "ingresso"

  Cenário: Certificado com mesmo título e mesma data do mesmo aluno é duplicado
    Dado que o aluno já submeteu um certificado "Curso de Java" da categoria "ENSINO" com 40 horas na data "2024-03-10"
    Quando o aluno submete um certificado "Curso de Java" da categoria "ENSINO" com 40 horas na data "2024-03-10"
    Então a submissão é rejeitada com a mensagem contendo "duplicado"

  Cenário: Categoria que não existe no catálogo é rejeitada
    Quando o aluno submete um certificado "Atividade Estranha" da categoria "MONITORIA_EXTRA" com 10 horas na data "2024-03-10"
    Então a submissão é rejeitada com a mensagem contendo "Categoria"
