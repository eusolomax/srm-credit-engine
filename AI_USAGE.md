# AI Usage

A IA foi utilizada como ferramenta de apoio durante o desenvolvimento do SRM Credit Engine, principalmente para implementação, revisão e exploração de alternativas técnicas.

A utilização da IA não substituiu a tomada de decisões sobre regras de negócio, arquitetura, escopo ou revisão final das implementações.

## Decisões não delegadas à IA

### Definição e validação da SPEC

A `SPEC.md` foi revisada antes e durante a implementação. As premissas e ambiguidades foram analisadas e validadas em relação ao projeto planejado, garantindo que as decisões adotadas fossem coerentes com o domínio e com o escopo do desafio.

A IA foi utilizada para auxiliar na implementação das regras definidas, mas a decisão sobre quais premissas seriam adotadas permaneceu sob minha responsabilidade.

### Commits e Pull Requests

A criação e organização dos commits e Pull Requests não foram delegadas à IA.

Mantive sob minha responsabilidade a revisão das implementações antes de realizar commits e publicar alterações no GitHub, além da definição de como as funcionalidades seriam divididas e apresentadas.

Essa abordagem também permitiu manter commits menores e relacionados a uma funcionalidade ou alteração específica.

### Decisões de arquitetura e escopo

As decisões arquiteturais foram revisadas e definidas por mim antes de serem aplicadas ao projeto.

Um exemplo foi a decisão de não criar uma entidade específica para `Assignor`. Como o desafio exige a utilização do cedente no extrato, mas não define um domínio próprio para essa entidade, optei por representá-lo diretamente no `Receivable`, evitando adicionar complexidade desnecessária ao escopo Júnior.

Outra decisão foi utilizar H2 como banco padrão para execução local. O objetivo foi permitir que o projeto seja executado rapidamente sem exigir a configuração de um banco externo, mantendo PostgreSQL como alternativa para quem desejar executar o projeto com um banco relacional externo.

---
## Specs e prompts estratégicos

A IA foi utilizada principalmente através de prompts orientados por especificação, nos quais o comportamento esperado, as restrições de escopo e os critérios de aceitação eram definidos antes da implementação.

### 1. Scaffolding inicial do backend

Prompt para estruturar o domínio e a arquitetura inicial do projeto:

Implementar a estrutura inicial do SRM Credit Engine em Spring Boot, seguindo arquitetura em camadas, criando os domínios de Receivable, ExchangeRate e Settlement, seus repositories, services e controllers conforme o escopo da SPEC. Utilizar BigDecimal para valores financeiros, JPA para persistência relacional e manter as regras de negócio fora dos controllers.



### 2. Refatoração do Pricing para centralizar o cálculo

Esse foi um dos principais prompts de refactoring:

Mover a responsabilidade de cálculo de pricing que está espalhada entre PricingController e SettlementService para o PricingService. O serviço deve receber os parâmetros necessários, executar o cálculo completo e retornar um PricingResult, permitindo que simulação e liquidação reutilizem a mesma regra de negócio sem duplicação.

### 3. Idempotência da liquidação

Este foi outro prompt importante:

Implementar idempotência no endpoint de liquidação. A requisição deve receber uma idempotencyKey; repetir a mesma chave para o mesmo recebível deve retornar a liquidação já existente sem criar outro registro. Reutilizar a mesma chave para outro recebível deve ser rejeitado. Criar constraint única e testes cobrindo retry do mesmo request e uso incorreto da chave.


---


## Casos concretos em que a IA errou:

### 1. Arredondamento prematuro no Pricing Engine
Durante a implementação do Pricing Engine, a IA gerou as estratégias de precificação arredondando o valor presente para duas casas decimais diretamente dentro da `PricingStrategy`.

durante a revisão eu vi que esse comportamento poderia gerar perda de precisão no cenário em que o recebível estivesse em uma moeda diferente de BRL, como no case C3, pois o valor presente ainda seria utilizado posteriormente na conversão cambial.

então eu alterei a implementação para que a estratégia mantenha a maior precisão possível e o arredondamento para duas casas seja realizado somente após todas as operações financeiras necessárias, exceto a conversão cambial como definido no desafio técnico.

Esse caso foi identificado por revisão manual do código e pela análise dos golden cases, reforçando a necessidade de validar o código gerado pela IA em vez de apenas aceitar a implementação gerada.

```java
    public BigDecimal calculatePresentValue(BigDecimal faceValue, int termMonths) {
        BigDecimal monthlyRate = BigDecimal.ONE.add(BASE_RATE).add(DUPLICATA_SPREAD);
        BigDecimal discountFactor = monthlyRate.pow(termMonths);
        BigDecimal presentValue = faceValue.divide(discountFactor, MathContext.DECIMAL128);

        // PROBLEMA: Aqui ele arredonda o resultado monetário final para duas casas decimais.
        return presentValue.setScale(2, RoundingMode.HALF_EVEN);
    }
```


### 2. Taxa base definida diretamente na estratégia
Durante a implementação do Pricing Engine, a IA definiu a taxa base diretamente nas estratégias de precificação como uma constante:

```java
private static final BigDecimal BASE_RATE = new BigDecimal("0.0100");
```

Porém, na `SPEC.md` foi definido que a taxa base possui 1,00% a.m. como valor padrão, mas pode ser alterada por configuração no `application.properties`.

Durante a revisão, identifiquei a inconsistência entre a implementação gerada e a decisão documentada na especificação. A implementação foi então ajustada para que a taxa base seja obtida por configuração do Spring, mantendo um único valor compartilhado pelas estratégias.

Esse caso reforçou a necessidade de revisar o código gerado pela IA e compará-lo com as decisões e regras definidas na especificação, em vez de assumir que a implementação gerada estava automaticamente alinhada ao projeto.

### 3. Consulta de taxa de câmbio

Durante a implementação do `ExchangeRateRepository`, a IA utilizou uma query nativa recebendo `CurrencyCode` diretamente como parâmetro:

```java
Optional<ExchangeRate> findLatestValidRate(
    CurrencyCode fromCurrency,
    CurrencyCode toCurrency,
    Instant timestamp
);
```

Embora a entidade utilize `@Enumerated(EnumType.STRING)`, a query nativa acabou enviando o enum para o PostgreSQL como valor numérico. Como as colunas `from_currency` e `to_currency` são `VARCHAR`, a consulta falhou com um erro de incompatibilidade de tipos:

```text
operator does not exist: character varying = smallint
```

O problema foi identificado ao executar a liquidação utilizando câmbio, quando o PostgreSQL rejeitou a query.

Durante a revisão, a implementação foi ajustada para que o repository recebesse `String` e o service convertesse explicitamente os enums para seus nomes:

```java
exchangeRateRepository.findLatestValidRate(
    fromCurrency.name(),
    toCurrency.name(),
    timestamp
);
```