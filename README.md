# banco-lab

Projeto de estudo para a vaga de desenvolvedor pleno (Java / Spring / arquitetura) em banco.
É um banco digital bem pequeno, com **dois serviços que conversam pela rede**. Você aprende implementando: o projeto traz um fluxo completo pronto como referência e uma trilha de desafios para você construir o resto.

| Serviço | Porta | O que é | Estado |
|---|---|---|---|
| `conta-service` | 8080 | Contas, depósitos, transferências. Feito em **arquitetura hexagonal**. | Só "abrir conta" está pronto. O resto é com você. |
| `antifraude-service` | 8081 | Simulador de um sistema de antifraude de terceiros, com "modo caos" para ficar lento ou cair de propósito. | Pronto. Não precisa mexer. |

```
   você (HTTP)                 conta-service                      antifraude-service
  ─────────────►  POST /contas ──► [ hexágono ] ──► GET /analises/{cpf} ──►  APROVADO / REPROVADO
                                       │
                                       ▼
                                   banco (H2)
```

## Por onde começar

1. Leia este arquivo e rode o projeto (abaixo).
2. Leia [`docs/hexagonal.md`](docs/hexagonal.md) com o código aberto ao lado. Ele percorre o fluxo de exemplo arquivo por arquivo.
3. Abra [`DESAFIOS.md`](DESAFIOS.md) e siga a ordem.

## Como rodar

Você precisa do **JDK 21**. Maven não precisa instalar: o projeto traz o Maven Wrapper. Docker só é necessário em desafios mais adiante.

No Windows (PowerShell), na pasta do projeto, em **dois terminais**:

```powershell
# terminal 1
.\mvnw.cmd -pl antifraude-service spring-boot:run

# terminal 2
.\mvnw.cmd -pl conta-service spring-boot:run
```

Ou abra a pasta na IDE e rode as classes `AntifraudeServiceApplication` e `ContaServiceApplication`.

Para rodar todos os testes:

```powershell
.\mvnw.cmd verify
```

## Testando na mão

O arquivo [`requests.http`](requests.http) tem as chamadas prontas (funciona no IntelliJ e no VS Code com a extensão REST Client). Pelo terminal:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/contas -ContentType "application/json" -Body '{"titular":"Maria Silva","cpf":"529.982.247-25"}'
```

CPFs válidos para usar: `529.982.247-25`, `390.533.447-05` e `111.444.777-35` (este o antifraude sempre reprova).

O que já responde hoje:

| Chamada | Resultado |
|---|---|
| `POST /contas` com CPF válido | `201 Created`, header `Location`, conta com saldo 0 |
| `POST /contas` com o mesmo CPF de novo | `409 Conflict` |
| `POST /contas` com CPF inválido ou reprovado | `422 Unprocessable Entity` |
| `POST /contas` com titular em branco | `400 Bad Request` |
| `GET /analises/{cpf}` (porta 8081) | resultado do antifraude |
| `PUT /caos?latenciaMs=3000&taxaErro=0.5` (porta 8081) | liga falhas no antifraude |
| `DELETE /caos` (porta 8081) | desliga as falhas |

Console do banco H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:contas`, usuário `sa`, sem senha).

## Mapa do código

```
conta-service/src/main/java/br/com/bancolab/conta
├── domain/                 REGRAS DE NEGÓCIO. Java puro, sem Spring.
│   ├── model/              Conta, Cpf, StatusConta
│   └── exception/          erros de negócio
├── application/            CASOS DE USO. Também sem Spring.
│   ├── port/in/            portas de entrada  (o que a aplicação oferece)
│   ├── port/out/           portas de saída    (o que a aplicação precisa)
│   └── service/            implementação dos casos de uso
├── adapter/
│   ├── in/web/             adaptador de entrada: REST (controller, DTOs, erros HTTP)
│   └── out/
│       ├── persistence/    adaptador de saída: JPA
│       └── antifraude/     adaptador de saída: cliente REST do antifraude
├── config/                 onde o Spring monta o hexágono
└── legado/                 código ruim de propósito, para o desafio de SOLID
```

Nos testes, olhe com atenção para dois arquivos:

- `AbrirContaServiceTest`: testa o caso de uso sem Spring, sem banco e sem rede. É o principal ganho prático do hexagonal.
- `ArquiteturaHexagonalTest`: as regras do hexágono escritas como teste (ArchUnit). Se um desafio seu furar a arquitetura, ele quebra e aponta a linha.

## Infraestrutura opcional

`docker-compose.yml` traz SQL Server, MongoDB e Zipkin para os desafios 6 e 7. Nada disso é necessário antes.
