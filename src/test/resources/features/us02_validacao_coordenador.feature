# language: pt
Funcionalidade: Validação de certificado pelo coordenador
  Como coordenador do curso
  Quero aprovar ou reprovar os certificados que estão em análise
  Para que apenas atividades legítimas contem como horas complementares

  Contexto:
    Dado o catálogo de categorias padrão da faculdade
    E um aluno chamado "Bruno" que ingressou em "2023-01-01"
    E que a data de hoje é "2024-06-01"
    E um certificado "Curso de Testes" da categoria "ENSINO" com 30 horas na data "2024-03-01" em análise

  Cenário: Coordenador aprova um certificado em análise
    Quando o coordenador aprova o certificado
    Então o certificado fica com status "APROVADO"
    E é registrado um histórico de validação com resultado "APROVADO"

  Cenário: Coordenador reprova um certificado com justificativa adequada
    Quando o coordenador reprova o certificado com a justificativa "Carga horaria nao confere com o comprovante anexado"
    Então o certificado fica com status "REPROVADO"
    E é registrado um histórico de validação com resultado "REPROVADO"

  Cenário: Justificativa com exatamente 10 caracteres é aceita (borda)
    Quando o coordenador reprova o certificado com a justificativa "9876543210"
    Então o certificado fica com status "REPROVADO"

  Esquema do Cenário: Reprovação com justificativa insuficiente é rejeitada
    Quando o coordenador reprova o certificado com a justificativa "<justificativa>"
    Então a validação é rejeitada com a mensagem contendo "justificativa"
    E o certificado continua com status "EM_ANALISE"

    Exemplos:
      | justificativa |
      |               |
      | curta         |
      | 123456789     |

  Cenário: Não é possível aprovar um certificado que já está aprovado
    Dado que o coordenador já aprovou o certificado
    Quando o coordenador aprova o certificado
    Então a validação é rejeitada com a mensagem contendo "imut"

  Cenário: Não é possível reprovar um certificado que já foi reprovado
    Dado que o coordenador já reprovou o certificado com a justificativa "Documento ilegivel e sem assinatura"
    Quando o coordenador reprova o certificado com a justificativa "Nova tentativa de reprovacao do mesmo item"
    Então a validação é rejeitada com a mensagem contendo "EM_ANALISE"
