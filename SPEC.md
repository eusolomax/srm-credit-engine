# Premissas adotadas para ambiguidades identificadas:
### Taxa Base:
* A taxa base oficial fica fixada em 1,00% a.m. (seguindo os Golden Cases).
* A taxa base possui 1,00% como valor padrão e pode ser configurada pelo **`application.properties`**
caso seja necessário alterá-la.

### Política oficial de arredondamento:
* Os cálculos intermediários serão realizados mantendo a maior precisão possível.
* O arredondamento para duas casas decimais será realizado somente no resultado monetário final.
* Será utilizado **Banker's Rounding**, por meio de `RoundingMode.HALF_EVEN`.

### Prazo de vencimento:
* O prazo será representado em meses inteiros seguindo os Golden Cases.
* Prazos fracionados não serão aceitos nesta versão.

### Taxa de câmbio em cada operação:
* As taxas de câmbio serão cadastradas manualmente por meio do endpoint `POST /exchange-rates`.
* Cada taxa possuirá uma data/hora de vigência (`effectiveAt`). Na liquidação, será utilizada a taxa mais recente cuja data de vigência seja menor ou igual ao momento da liquidação.
* As taxas históricas não poderão ser sobrescritas, permitindo preservar o histórico de cotações.
* A taxa efetivamente utilizada na liquidação será armazenada junto ao `Settlement`, garantindo a rastreabilidade da operação.
* Caso não exista uma taxa válida para o par de moedas no momento da liquidação, a operação será rejeitada, orientando o usuário a cadastrar uma nova cotação para o respectivo par de moedas.

### Moedas suportadas:
* O sistema inicialmente suportará BRL e USD, que são suficientes para os cenários apresentados nos Golden Cases.
* O sistema tem uma estrutura de algoritmo que suporta a nova implementação de moedas sem grandes modificações nos códigos

### Sobre os recebíveis:

* Os recebíveis terão inicialmente os estados:

  * `AVAILABLE`: disponível para liquidação.
  * `SETTLED`: já liquidado.
* Recebíveis vencidos não poderão ser liquidados. A operação deverá ser rejeitada caso a data de vencimento já tenha sido ultrapassada.

### Liquidações:

* Um recebível somente poderá ser liquidado uma vez. Tentativas de liquidar um recebível que já esteja no estado `SETTLED` serão rejeitadas.
* Não serão permitidas liquidações parciais nesta versão.

---

# Perguntas que eu faria ao negócio:
### Quais regras de moeda devem ser consideradas?
O valor de face dos recebíveis é sempre informado em BRL? Em caso de liquidação em moeda estrangeira, o valor presente e o deságio devem ser apresentados na moeda de pagamento?

### Qual é a fonte oficial das taxas de câmbio em produção?
O desafio permite o cadastro manual das taxas, mas em um ambiente real seria necessário definir o provedor oficial, sua confiabilidade e os requisitos de disponibilidade.

### Existe algum limite de exposição ou valor máximo para uma liquidação?
Operações acima de determinado valor exigem aprovação ou algum fluxo adicional?

### Quais informações devem ser disponibilizadas no extrato?
Além dos filtros previstos no desafio, existem outros dados ou filtros importantes para a operação diária?

### Existem requisitos de auditoria adicionais?
Além dos dados utilizados no cálculo e da taxa de câmbio, quais informações precisam ser preservadas para rastrear completamente uma operação?

---

# Decisões de precisão numérica:
Por se tratar de um sistema financeiro, valores monetários e taxas não serão representados utilizando tipos de ponto flutuante, como `double` ou `float`, devido à possibilidade de erros de precisão binária.

Será utilizado `BigDecimal` na aplicação e `NUMERIC` no banco de dados para representar valores financeiros e taxas, incluindo:

* valores monetários;
* taxas de juros;
* spreads;
* taxas de câmbio;
* resultados intermediários dos cálculos financeiros.

Os cálculos intermediários manterão a maior precisão possível, evitando arredondamentos prematuros que possam acumular diferenças ao longo da operação.

O arredondamento monetário será realizado somente no resultado final, considerando duas casas decimais e utilizando `RoundingMode.HALF_EVEN` (Banker's Rounding).

As comparações entre valores `BigDecimal` deverão considerar seu valor numérico, evitando depender da escala quando ela não for relevante para a comparação.

# Critérios de aceite próprios:
### Usabilidade

* A interface deve permitir realizar uma simulação de precificação sem conhecimento técnico.
* Os campos obrigatórios devem possuir validação e mensagens de erro claras.
* O resultado da simulação deve apresentar de forma clara o valor presente e o desconto aplicado.
* Erros retornados pela API devem ser apresentados de forma compreensível ao usuário.

### Segurança

* Dados recebidos pela API devem ser validados antes do processamento.
* Consultas ao banco devem utilizar os mecanismos do Spring Data/JPA, evitando concatenação de SQL fornecido pelo usuário.
* Operações de liquidação devem respeitar o estado do recebível, impedindo liquidações duplicadas.
* Dados sensíveis ou credenciais de serviços externos não devem ser armazenados diretamente no código-fonte.

### Desempenho

* A simulação de precificação deve ser realizada de forma síncrona, sem processamento desnecessário.
* Consultas ao banco devem retornar apenas os dados necessários para cada operação.
* Operações de liquidação devem concluir em tempo adequado para uso operacional, considerando principalmente o tempo de acesso ao banco.
