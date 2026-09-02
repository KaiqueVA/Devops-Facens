# language: pt
Funcionalidade: Teto de horas por categoria
  Como coordenador responsável pela integralização
  Quero que cada categoria respeite seu teto e que o total não passe de 200 horas
  Para que o cálculo das horas complementares siga o regulamento

  Contexto:
    Dado o catálogo de categorias padrão da faculdade
    E um aluno chamado "Carla" que ingressou em "2023-01-01"
    E que a data de hoje é "2024-06-01"

  Cenário: Horas aprovadas dentro do teto da categoria são contabilizadas integralmente
    Dado que o aluno teve 60 horas aprovadas na categoria "ENSINO"
    Quando o sistema calcula o resumo de horas do aluno
    Então o total de horas contabilizadas é 60
    E o total de horas excedentes é 0

  Cenário: Horas que ultrapassam o teto da categoria viram excedente
    Dado que o aluno teve 100 horas aprovadas na categoria "EVENTO"
    Quando o sistema calcula o resumo de horas do aluno
    Então a categoria "EVENTO" contabiliza 60 horas
    E o total de horas contabilizadas é 60
    E o total de horas excedentes é 40

  Esquema do Cenário: Teto aplicado por categoria
    Dado que o aluno teve <enviadas> horas aprovadas na categoria "<categoria>"
    Quando o sistema calcula o resumo de horas do aluno
    Então a categoria "<categoria>" contabiliza <contabilizadas> horas
    E o total de horas excedentes é <excedentes>

    Exemplos:
      | categoria | enviadas | contabilizadas | excedentes |
      | ENSINO    | 50       | 50             | 0          |
      | ENSINO    | 80       | 80             | 0          |
      | ENSINO    | 120      | 80             | 40         |
      | EVENTO    | 90       | 60             | 30         |

  Cenário: Total geral nunca passa de 200 horas
    Dado que o aluno teve 80 horas aprovadas na categoria "ENSINO"
    E que o aluno teve 80 horas aprovadas na categoria "PESQUISA"
    E que o aluno teve 80 horas aprovadas na categoria "EXTENSAO"
    Quando o sistema calcula o resumo de horas do aluno
    Então o total de horas contabilizadas é 200
    E o total de horas excedentes é 40

  Cenário: Certificado ainda em análise não é contabilizado
    Dado que o aluno submeteu 40 horas na categoria "ENSINO" sem aprovação
    Quando o sistema calcula o resumo de horas do aluno
    Então o total de horas contabilizadas é 0
