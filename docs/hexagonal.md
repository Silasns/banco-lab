# Arquitetura hexagonal, explicada em cima deste projeto

Leia com o código do `conta-service` aberto ao lado.

## A ideia em uma frase

**A regra de negócio fica no centro e não depende de nada. Tudo que é tecnologia (HTTP, banco, outros sistemas) fica na borda e depende do centro.**

O nome "hexágono" não significa nada: não há seis lados. O nome original é *Ports and Adapters* (portas e adaptadores), e esse sim descreve a ideia.

## A analogia da tomada

Pense na sua aplicação como um aparelho com tomadas.

- A **porta** é o formato da tomada: uma interface Java definida pela aplicação, dizendo "é assim que se conversa comigo".
- O **adaptador** é o que se encaixa na tomada: uma classe que traduz entre o mundo de fora (JSON, SQL, REST) e essa interface.

O aparelho não sabe o que está plugado. Você troca H2 por SQL Server, ou REST por fila, trocando o adaptador. O aparelho não muda.

## Dois tipos de porta

Esta é a parte que mais confunde, então guarde assim: **quem começa a conversa?**

| | Porta de entrada (*driving*, *in*) | Porta de saída (*driven*, *out*) |
|---|---|---|
| Quem começa a conversa | O mundo de fora chama a aplicação | A aplicação chama o mundo de fora |
| Quem **define** a interface | A aplicação | A aplicação |
| Quem **implementa** a interface | A aplicação (o caso de uso) | O adaptador |
| Quem **usa** a interface | O adaptador | A aplicação (o caso de uso) |
| Exemplo aqui | `AbrirContaUseCase` | `ContaRepository`, `AntifraudePort` |
| Adaptador aqui | `ContaController` | `ContaPersistenceAdapter`, `AntifraudeRestAdapter` |

Repare na linha "quem define": **as duas interfaces moram dentro do hexágono**. É isso que inverte a dependência. O adaptador JPA conhece a aplicação; a aplicação não conhece o JPA.

## O desenho

```
                 ADAPTADORES DE ENTRADA                          ADAPTADORES DE SAÍDA
                                        ┌──────────────────────────┐
                                        │        APLICAÇÃO         │
  HTTP ──► ContaController ──usa──► [AbrirContaUseCase]            │
                                        │         ▲                │
                                        │         │ implementa     │
                                        │  AbrirContaService       │
                                        │     │         │          │
                                        │     │ usa     │ usa      │
                                        │     ▼         ▼          │
                                        │  DOMÍNIO   [ContaRepository] ◄──implementa── ContaPersistenceAdapter ──► banco
                                        │  Conta     [AntifraudePort]  ◄──implementa── AntifraudeRestAdapter  ──► outro serviço
                                        │  Cpf                     │
                                        └──────────────────────────┘
                        [entre colchetes] = porta (interface)
```

**A regra de dependência:** todas as setas de "conhece / importa" apontam para dentro.

- `domain` não importa nada além de `java.*`.
- `application` importa só `domain` (e ela mesma).
- `adapter` importa `application` e `domain`.
- Um adaptador nunca importa outro adaptador.

O `ArquiteturaHexagonalTest` verifica exatamente essas quatro frases.

## Seguindo uma requisição

`POST /contas` com `{"titular": "Maria Silva", "cpf": "529.982.247-25"}`. Abra cada arquivo na ordem:

1. **`adapter/in/web/ContaController`** recebe o HTTP. O Spring converte o JSON em `AbrirContaRequest` e valida o formato (`@NotBlank`). O controller monta um `AbrirContaCommand` e chama a porta `AbrirContaUseCase`. Ele não sabe qual classe está do outro lado.
2. **`application/service/AbrirContaService`** é quem está do outro lado. Ele orquestra:
   - cria um `Cpf` (se for inválido, o próprio `Cpf` lança exceção);
   - pergunta à porta `ContaRepository` se o CPF já existe;
   - pergunta à porta `AntifraudePort` se o CPF é aprovado;
   - pede ao domínio para criar a conta (`Conta.abrir`);
   - pede à porta `ContaRepository` para salvar.
3. **`domain/model/Conta`** aplica a regra: conta nasce ativa, com saldo zero, e precisa de titular.
4. **`adapter/out/antifraude/AntifraudeRestAdapter`** é quem de fato faz o `GET` no outro serviço e traduz o JSON de lá para o enum `ResultadoAntifraude`.
5. **`adapter/out/persistence/ContaPersistenceAdapter`** converte `Conta` em `ContaJpaEntity`, salva via Spring Data e converte de volta.
6. De volta ao **`ContaController`**, a `Conta` vira `ContaResponse` e sai como `201 Created`.

Se der erro de negócio no meio, o `ApiExceptionHandler` (também um adaptador de entrada) traduz a exceção para o status HTTP.

E quem ligou tudo isso? **`config/CasosDeUsoConfig`**. É o único lugar que conhece os dois lados: recebe os adaptadores do Spring e devolve o caso de uso montado.

## "Por que tanta classe para salvar uma conta?"

Pergunta justa, e pode cair na prova. O custo é real: há três representações da conta (`AbrirContaRequest`/`ContaResponse`, `Conta`, `ContaJpaEntity`) e conversões entre elas. O que se compra com isso:

- **Testar a regra de negócio sem infraestrutura.** Veja `AbrirContaServiceTest`: o "banco" é uma lista e o "antifraude" é uma lambda.
- **Trocar tecnologia mexendo em um pacote só.** Nos desafios você vai trocar H2 por SQL Server e adicionar circuit breaker no antifraude sem tocar em `domain` nem em `application`.
- **A API, o negócio e a tabela evoluem separados.** Renomear uma coluna não muda o JSON da API.

Para um CRUD simples, hexagonal é exagero, e dizer isso numa entrevista mostra maturidade. Em um banco, onde a regra de negócio é complexa e vive por décadas enquanto a tecnologia em volta troca, costuma compensar.

## Confusões comuns

- **"O repositório do Spring Data é a porta."** Não. `ContaJpaRepository` é detalhe do adaptador. A porta é `ContaRepository`, que fala em `Conta` e `Cpf`.
- **"Posso pôr `@Entity` na `Conta` para economizar."** Aí o domínio passa a depender de JPA e o ArchUnit quebra. Alguns times aceitam esse atalho de forma consciente; aqui, não.
- **"Validação fica onde?"** Formato do dado de entrada (campo obrigatório, tamanho) no adaptador. Regra de negócio (CPF válido, saldo suficiente) no domínio.
- **"Hexagonal é a mesma coisa que camadas?"** Em camadas (controller → service → repository) o service depende do repository concreto: a dependência aponta para o banco. No hexagonal ela aponta para o domínio.
- **"E microsserviço?"** São coisas de níveis diferentes. Hexagonal organiza **um** serviço por dentro. Microsserviços são sobre como **vários** serviços se dividem e conversam.

## Receita para criar um caso de uso novo

Você vai repetir isto em quase todo desafio. Trabalhe **de dentro para fora**:

1. **Domínio:** a regra precisa de um comportamento novo na `Conta`? Escreva o teste de domínio e depois o método.
2. **Porta de entrada:** crie a interface `...UseCase` em `application/port/in`, com seu command.
3. **Portas de saída:** o caso de uso precisa de algo novo do mundo de fora (buscar por id, publicar evento)? Adicione à porta existente ou crie outra em `application/port/out`.
4. **Caso de uso:** escreva o teste com adaptadores falsos e depois a classe em `application/service`.
5. **Adaptadores de saída:** implemente o que você adicionou nas portas.
6. **Montagem:** registre o bean em `CasosDeUsoConfig`.
7. **Adaptador de entrada:** endpoint, DTOs e tradução de exceções.
8. Rode `.\mvnw.cmd verify`. O `ArquiteturaHexagonalTest` precisa continuar verde.
