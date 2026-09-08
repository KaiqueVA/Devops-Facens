# language: pt
Funcionalidade: Gamificacao para engajamento de educacao continuada

  Como aluno da plataforma de cursos online por assinatura
  Quero ser recompensado pelo meu aproveitamento e pela minha participacao
  Para evoluir ao plano Premium e converter minhas moedas

  # Cenario 1 - Integrante 1 - caminho feliz (RN2)
  Cenario: Concluir curso com media acima de 7,0 libera tres cursos
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    Quando o aluno conclui o curso "Java Basico" com media 8.5
    Entao o aluno deve ter 7 cursos disponiveis
    E o aluno deve ter 1 curso conquistado

  # Cenario 2 - Integrante 2 - validacao de dados (RN2 pelo lado negativo)
  Cenario: Media fora da faixa e recusada e nao altera o estado do aluno
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    Quando o aluno tenta concluir o curso "Java Basico" com media 11.0
    Entao a operacao deve ser recusada com a mensagem "Media deve estar entre 0,0 e 10,0"
    E o aluno deve ter 5 cursos disponiveis

  # Cenario 3 - Integrante 3 - regra de negocio (RN4)
  Cenario: Conquistar 12 cursos promove o aluno a Premium
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    E o aluno ja conquistou 11 cursos com media acima de 7,0
    Quando o aluno conclui o curso "Curso 12" com media 9.0
    Entao o plano do aluno deve ser "PREMIUM"
    E o aluno deve possuir voucher para projetos reais
    E o saldo de moedas do aluno deve ser 3

  # Cenario 4 - complementar do grupo (RN3)
  Cenario: Destaque do forum ganha um curso no fim do mes
    Dado um aluno "maria@teste.com" no plano "BASICO" com 5 cursos disponiveis
    E o aluno registrou 10 topicos e 9 comentarios no forum
    E existe o aluno "joao@teste.com" com 10 topicos e 1 comentarios no forum
    Quando a plataforma premia o destaque do forum
    Entao o aluno deve ter 6 cursos disponiveis
    E a participacao do aluno no forum deve estar zerada

  # Cenario 5 - complementar do grupo (RN5)
  Cenario: Aluno Premium converte moedas em conhecimento
    Dado um aluno "maria@teste.com" ja promovido ao plano "PREMIUM"
    Quando o aluno converte 3 moedas em "CONHECIMENTO"
    Entao o saldo de moedas do aluno deve ser 0
    E o aluno deve ter 32 cursos disponiveis
