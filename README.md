# SRM Credit Engine

Sistema para precificação e liquidação de recebíveis em múltiplas moedas, desenvolvido como parte do desafio técnico da SRM Asset.

O sistema contempla o ciclo principal de um recebível: cadastro, precificação, consulta de câmbio, liquidação e consulta do extrato de liquidações.

---

## Stack

### Backend

- Java 21
- Spring Boot
- Maven

### Database

- PostgreSQL
- H2

### Frontend

- Angular
- TypeScript
- RxJS

### Documentação

- OpenAPI / Swagger

### Decisão sobre a stack:

**Java 21 + Spring Boot** foram escolhidos pela tipagem forte e pelo ecossistema maduro para aplicações transacionais, além de facilitarem a separação entre API, regras de negócio e persistência.

**PostgreSQL** foi escolhido como banco relacional por atender aos requisitos de integridade referencial e transações necessárias para o fluxo de liquidação.

**Angular + TypeScript** permitem manter tipagem estática também no frontend e uma separação clara entre apresentação, estado e comunicação com a API.

O projeto utiliza **H2** como banco principal para facilitar a execução local sem a necessidade de configurar um banco externo.

---

## Como executar

### 1. Backend

Entre na pasta:

```bash
cd back-end
```

Por padrão, a aplicação utiliza H2 em memória e pode ser iniciada diretamente:

```bash
./mvnw spring-boot:run
```

A API estará disponível em:

```text
http://localhost:8080
```

### Executando com PostgreSQL

Para executar utilizando PostgreSQL, use o profile `postgres` e informe as variáveis de ambiente do banco:

```bash
DB_NAME=srm_credit_engine \
DB_USERNAME=meu_usuario \
DB_PASSWORD=minha_senha \
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

As variáveis são utilizadas para configurar a conexão com o banco PostgreSQL:

- `DB_NAME`: nome do banco de dados;
- `DB_USERNAME`: usuário do banco;
- `DB_PASSWORD`: senha do banco.

Nesse caso, é necessário ter um PostgreSQL disponível e criar o banco:

```sql
CREATE DATABASE srm_credit_engine;
```

O H2 é utilizado por padrão para simplificar a execução do projeto. O PostgreSQL permanece disponível como alternativa para execução com banco relacional externo.

---

### 2. Frontend

Entre na pasta:

```bash
cd front-end
```

Instale as dependências:

```bash
npm install
```

Inicie a aplicação:

```bash
npm run start
```

A aplicação estará disponível em:

```text
http://localhost:4200
```

---

## API

A documentação da API está disponível através do Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Design

O backend utiliza uma arquitetura em camadas, separando API (Controller), aplicação/regra de negócio (Services) e persistência (Repository).

O cálculo de precificação utiliza o padrão **Strategy** como o desafio solicita, permitindo que a regra de spread varie de acordo com o tipo do recebível sem concentrar todas as regras em condicionais.

As regras financeiras, premissas de negócio, precisão numérica e modelo de dados estão documentados em [`SPEC.md`](./SPEC.md).

Decisões de escopo e funcionalidades deliberadamente simplificadas estão registradas em [`DECISIONS.md`](./DECISIONS.md).

---

## Golden Cases

O motor de precificação deve reproduzir os casos de referência do desafio ao centavo:

| #  | Tipo                | Valor de face |   Prazo | Moeda pgto. | Câmbio (BRL/USD) | Valor presente esperado |     Deságio |
| -- | ------------------- | ------------: | ------: | ----------- | ---------------: | ----------------------: | ----------: |
| C1 | Duplicata Mercantil | R$ 100.000,00 | 3 meses | BRL         |                — |        **R$ 92.859,94** | R$ 7.140,06 |
| C2 | Cheque Pré-datado   |  R$ 25.000,00 | 2 meses | BRL         |                — |        **R$ 23.337,77** | R$ 1.662,23 |
| C3 | Duplicata Mercantil | R$ 100.000,00 | 3 meses | USD         |           5,4321 |       **US$ 17.094,67** | R$ 7.140,06 |

Os três casos possuem cobertura automatizada.

---

## Testes

Para executar os testes do backend:

```bash
cd back-end
./mvnw test
```

---

## Documentação adicional

* [`SPEC.md`](./SPEC.md) — premissas de negócio, precisão numérica, regras de câmbio e modelo de dados.
* [`DECISIONS.md`](./DECISIONS.md) — decisões de escopo e funcionalidades não implementadas.
* [`REVIEW.md`](./REVIEW.md) — code review reverso do desafio.
* [`AI_USAGE.md`](./AI_USAGE.md) — uso de IA durante o desenvolvimento, erros identificados e decisões mantidas sob responsabilidade do desenvolvedor.
