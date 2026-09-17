# Decisions

Este documento registra funcionalidades e abordagens deliberadamente simplificadas ou não implementadas durante o desenvolvimento do SRM Credit Engine.

As decisões foram tomadas considerando o escopo Júnior do desafio, priorizando corretude do domínio, clareza da implementação e tempo disponível.

---

## 1. Cedente representado diretamente no `Receivable`

O desafio exige que o extrato de liquidações permita filtro por cedente, mas não define um domínio próprio para empresas cedentes.

Foi decidido não criar uma entidade de cedente (`Assignor`).

O cedente é representado diretamente no `Receivable` através de um identificador textual.

**Motivo:** atender ao requisito do extrato sem criar uma nova entidade, relacionamento e fluxo de cadastro que não são necessários para o escopo atual.

---

## 2. H2 como banco padrão para execução local

O projeto utiliza H2 em memória como configuração padrão.

O PostgreSQL permanece disponível através do profile `postgres`.

**Motivo:** permitir que o avaliador clone o projeto e execute o backend imediatamente, sem precisar instalar ou configurar um banco externo.

A utilização de PostgreSQL continua disponível para execução com um banco relacional real.

---

## 3. Sem paginação server-side no extrato

O endpoint de consulta de liquidações não possui paginação server-side.

**Motivo:** o desafio especifica uma listagem simples para o nível Júnior. Paginação server-side e filtros dinâmicos no grid aparecem como requisito adicional de nível Pleno. :contentReference[oaicite:2]{index=2}

---

## 4. Consulta do extrato utilizando JPA/JPQL

O extrato utiliza uma consulta simples sobre JPA/JPQL, sem implementação de query builder ou SQL nativo otimizado.

**Motivo:** o desafio apresenta query builder ou SQL nativo otimizado como um diferencial de nível Pleno+, não como requisito para a implementação Júnior.

A implementação atual prioriza legibilidade e simplicidade, mantendo os filtros necessários para o requisito funcional.

---

## 5. Cadastro manual de taxas de câmbio

Não foi implementada integração com um provedor externo de câmbio.

As taxas são cadastradas manualmente através da API e possuem `effectiveAt`.

**Motivo:** o desafio permite explicitamente uma atualização manual ou uma integração mockada. Foi escolhida a alternativa manual para evitar adicionar dependências externas e manter o comportamento determinístico durante os testes.

---
## 6. Sem UUID público para identificação de recebíveis

Os endpoints utilizam o identificador `Long` atualmente persistido para referência aos recebíveis.

**Motivo:** a implementação priorizou a simplicidade do escopo atual. A adoção de UUID pode ser considerada futuramente caso exista necessidade de expor identificadores públicos. No momento, o identificador `Long` é suficiente para o desafio técnico.