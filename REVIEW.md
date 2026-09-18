# Code Review Reverso (Anexo A)

## 1. SQL Injection (Crítico)
```typescript
const receivable = await db.queryOne(
  `SELECT * FROM receivables WHERE id = ${receivableId}`
);
````

```typescript
await db.query(
  `INSERT INTO settlements (receivable_id, amount, currency)
   VALUES (${receivableId}, ${finalAmount.toFixed(2)}, '${currency}')`
);
```

`receivableId` e `currency` vêm diretamente da requisição e são concatenados no SQL. Uma requisição maliciosa pode manipular as queries e acessar ou alterar dados indevidamente.

Em um sistema financeiro, isso pode comprometer a confidencialidade e a integridade da base.

**Correção proposta:** utilizar queries parametrizadas (prepared statements)

---

## 2. Cálculo financeiro incorreto e uso de `number` (Crítico)

```typescript
const BASE_RATE = 1.0;

const spread =
  receivable.type === "DUPLICATA" ? 1.5 : 2.5;

const presentValue =
  receivable.face_value /
  Math.pow(1 + BASE_RATE + spread, receivable.term);
```

**Impacto em produção:** as taxas estão representadas com a escala incorreta. Pela regra do domínio:\
`1%` = `0.01` \
`1.5%` = `0.015`\
`2.5%` = `0.025`

Além disso, o cálculo utiliza `number`, que não é adequado para cálculos financeiros que precisam de precisão decimal. 

**Correção proposta:**

representar valores monetários em uma estrutura decimal apropriada para JavaScript/TypeScript, como a biblioteca decimal.js ou big.js, evitando number nos cálculos financeiros. As taxas também devem ser representadas como decimais (0.01, 0.015, 0.025).

---

## 3. Falta de transação e tratamento incorreto de erros (Crítico)

```typescript
try {
  await db.query(`INSERT INTO settlements ...`);
  await db.query(`UPDATE receivables SET status = 'SETTLED' ...`);
} catch (e) {
  // se falhar aqui, o insert já rodou, então segue o jogo
}

res.status(200).json({ ok: true, amount: finalAmount.toFixed(2) });
```

**Impacto em produção:** a criação do settlement e a atualização do recebível não são garantidas como uma única operação.

Se o `INSERT` funcionar e o `UPDATE` falhar, o banco pode ficar inconsistente: o settlement existe, mas o recebível continua com outro estado.

Além disso, a exceção é ignorada e o endpoint retorna sucesso mesmo quando a liquidação pode ter falhado.

**Correção proposta:** executar as operações dentro de uma única transação. Se qualquer etapa falhar, toda a operação deve sofrer rollback e o erro deve ser propagado para que a API retorne uma resposta de erro apropriada.

---

## 4. Ausência de idempotência (Crítico)

O endpoint não recebe nem verifica uma chave de idempotência.

**Impacto em produção:** uma mesma solicitação pode ser reenviada por retry de rede, timeout ou duplo clique e gerar mais de uma liquidação para o mesmo recebível.

Isso pode resultar em duplicidade financeira.

**Correção proposta:** receber uma `idempotencyKey` na requisição e armazená-la junto ao settlement.

A mesma chave para o mesmo recebível deve retornar a liquidação já existente. Uma chave já utilizada para outro recebível deve ser rejeitada.

Também deve existir uma constraint de unicidade no banco para reforçar essa garantia.

---

## 5. Ausência de validação do recebível (Alto)

```typescript
const receivable = await db.queryOne(...);
```

Após a consulta, o código já acessa:

```typescript
receivable.type
```

sem validar adequadamente o resultado da consulta ou o estado do recebível.

Também não existe validação para garantir que o recebível:

* realmente existe;
* está disponível para liquidação;
* ainda não foi liquidado;
* não está vencido.

**Impacto em produção:** recebíveis inexistentes podem gerar erros não tratados, enquanto recebíveis que não deveriam mais ser liquidados podem ser processados indevidamente.

**Correção proposta:** validar a existência do recebível e suas condições de liquidação antes de executar o cálculo.

---

## 6. Uso incorreto da taxa de câmbio (Alto)

```typescript
const rate = await fxService.getLatestRate("USD");
```

A consulta considera apenas `USD` e busca genericamente a última taxa.

Porém, uma taxa de câmbio depende do par de moedas e possui uma vigência.

**Impacto em produção:** o sistema pode utilizar uma cotação que não era válida no momento da liquidação, causando um valor financeiro incorreto e dificultando a auditoria da operação.

**Correção proposta:** buscar a taxa para o par correto de moedas e considerar sua vigência.

Para uma liquidação, deve ser utilizada a taxa mais recente cuja `effectiveAt` seja menor ou igual ao momento da operação.

A taxa efetivamente utilizada também deve ser armazenada no `Settlement` para preservar o histórico da liquidação.

---

## Resumo de severidade

| # | Problema                                           | Severidade |
| - | -------------------------------------------------- | ---------- |
| 1 | SQL Injection                                      | Crítico    |
| 2 | Cálculo financeiro incorreto e uso de `number`     | Crítico    |
| 3 | Falta de transação e tratamento incorreto de erros | Crítico    |
| 4 | Ausência de idempotência                           | Crítico    |
| 5 | Ausência de validação do recebível                 | Alto       |
| 6 | Uso incorreto da taxa de câmbio                    | Alto       |
