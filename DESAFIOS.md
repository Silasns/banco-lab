# Trilha de desafios

Você vai transformar o `conta-service` de "só abre conta" em um mini core bancário distribuído. Cada desafio pede que você **pesquise, implemente e depois explique**. Nenhum traz a solução.

## Como usar

- **Siga a ordem.** Os desafios se apoiam uns nos outros.
- Cada desafio tem:
  - **Missão**: o que construir.
  - **Pronto quando**: como saber que terminou.
  - **Pesquise**: os termos para você buscar. A ideia é ir atrás, não receber pronto.
  - **Perguntas de prova**: responda em voz alta ou por escrito, sem olhar. Se travar, volte ao código que você acabou de escrever: a resposta está nele.
- Itens marcados **[núcleo]** são o mínimo. Itens **[extra]** só se sobrar tempo.
- Depois de cada desafio rode `.\mvnw.cmd verify`. O `ArquiteturaHexagonalTest` tem que continuar verde. Se ele quebrar, não o desabilite: leia a mensagem, ela diz qual linha furou o hexágono.
- Faça um commit por desafio (`git init` na raiz). Em entrevista, conseguir mostrar a evolução vale muito.

**Travou?** Me mande o trecho de código e peça uma dica, não a resposta. Quando terminar um desafio, me mande o código para revisão como se fosse um pull request, e peça para eu te sabatinar com as perguntas de prova.

## Plano para menos de uma semana

Com esse prazo não dá para fazer tudo a fundo. A prioridade é esta:

| Dia | Desafios | Foco |
|---|---|---|
| 1 | 0, 1 e 2 | REST e hexagonal (seu ponto fraco declarado: gaste tempo aqui) |
| 2 | 3 | SOLID, herança, composição, polimorfismo, decorator |
| 3 | 4 | Lambdas, streams, threads, concorrência, garbage collector |
| 4 | 5 | Sistemas distribuídos: timeout, retry, circuit breaker, idempotência |
| 5 | 6 e 7 (só núcleo) | Actuator, logs, rastreamento; eventos, CQRS, event sourcing |
| 6 | 8 (teórico) e revisão | Docker, pipeline, Argo, Azure; refazer todas as perguntas de prova |

Se o tempo apertar, corte nesta ordem: extras do 7, prática do 8, extras do 4. **Não corte** 1, 2, 3 e 5.

---

## Desafio 0 — Aquecimento (1 hora)

**Missão**

1. Rode os dois serviços e faça todas as chamadas do `requests.http`.
2. Leia `docs/hexagonal.md` seguindo o código.
3. Coloque um breakpoint no `ContaController.abrir` e siga a requisição com o depurador até o `INSERT` no banco. Anote por quais classes passou, na ordem.
4. Quebre a arquitetura de propósito: adicione `@Service` (do Spring) na classe `AbrirContaService` e rode os testes. Leia a mensagem do ArchUnit. Desfaça.
5. Ligue o modo caos (`PUT /caos?taxaErro=1` na porta 8081) e tente abrir uma conta. Depois use `latenciaMs=10000`. Anote o que acontece com o `conta-service` em cada caso. Você vai consertar isso no Desafio 5.

**Pronto quando** você consegue desenhar no papel, sem olhar, o caminho de `POST /contas` com o nome de cada classe e dizer qual é porta e qual é adaptador.

**Pesquise:** inversão de controle, injeção de dependência, Spring Framework vs Spring Boot vs "Spring" (o ecossistema), Jakarta CDI.

**Perguntas de prova**

- Qual a diferença entre **Spring**, **Spring Framework** e **Spring Boot**? O que cada um entrega?
- O que é inversão de controle? E injeção de dependência? São a mesma coisa?
- Por que injeção por construtor é preferida à injeção por campo (`@Autowired` no atributo)?
- **CDI** e o container do Spring resolvem o mesmo problema. Quais os equivalentes de `@Inject`, `@ApplicationScoped`, `@Produces` e `@Qualifier` no Spring?
- O `AbrirContaService` não tem `@Service`. Como ele vira um bean? Por que foi feito assim?
- Qual o escopo padrão de um bean no Spring? Que cuidado isso exige com atributos mutáveis?

---

## Desafio 1 — REST: path param e query param

**Missão [núcleo]**

Implemente a consulta de contas, respeitando o hexágono (siga a "receita" no fim do `docs/hexagonal.md`):

- `GET /contas/{id}` devolve uma conta. `404` se não existir, `400` se o id não for um UUID.
- `GET /contas` lista as contas, com filtros **opcionais e combináveis**: `?titular=` (contém, sem diferenciar maiúsculas) e `?status=`. `400` se o status não existir.

**Pronto quando** você remover os `@Disabled("Desafio 1")` de `ContaApiTest` e todos passarem, com o ArchUnit verde.

**Missão [extra]**

- Paginação e ordenação: `?pagina=0&tamanho=20&ordenarPor=titular`. Cuidado para o tipo `Pageable` do Spring não vazar para dentro do hexágono.
- `PATCH /contas/{id}/bloqueio` para bloquear uma conta. Decida e justifique o verbo e a URL.

**Pesquise:** `@PathVariable`, `@RequestParam` (`required`, `defaultValue`), RFC 9457 Problem Details, métodos HTTP seguros e idempotentes, modelo de maturidade de Richardson.

**Perguntas de prova**

- Quando usar path param e quando usar query param? Por que `GET /contas/{id}` e não `GET /contas?id=`?
- Quais verbos HTTP são **idempotentes**? Quais são **seguros**? Por que isso importa em sistemas distribuídos?
- Diferença entre `400`, `404`, `409` e `422`. E entre `401` e `403`.
- `PUT` vs `PATCH`. `POST` pode ser idempotente?
- Onde você colocou a regra "conta não encontrada"? Quem decide que isso vira `404`?
- Para filtrar, você criou métodos novos na porta `ContaRepository`. Eles falam em `Conta`/`StatusConta` ou em tipos do Spring Data? Por quê?

---

## Desafio 2 — Hexagonal de verdade: depositar e sacar

Aqui você constrói casos de uso inteiros sozinho. É o desafio mais importante para a sua dificuldade com hexagonal: faça sem pressa.

**Missão [núcleo]**

- `POST /contas/{id}/depositos` e `POST /contas/{id}/saques`, com corpo `{"valor": 150.00}`. Devolvem a conta com o saldo novo.
- Regras (elas pertencem ao **domínio**, escreva o teste de domínio primeiro):
  - valor tem que ser maior que zero e ter no máximo 2 casas decimais;
  - só conta `ATIVA` movimenta;
  - saque não pode deixar o saldo negativo.
- Cada regra violada tem sua própria exceção de negócio e vira `422`.
- Teste os dois casos de uso **sem Spring**, com adaptadores falsos, como em `AbrirContaServiceTest`.

**Pronto quando** há testes de domínio, de caso de uso e de API passando, o ArchUnit está verde, e nenhum `if` de regra de negócio está no controller ou no adaptador de persistência.

**Missão [extra]**

- Crie o value object `Dinheiro` para substituir o `BigDecimal` solto (escala, arredondamento, proibição de negativo em um lugar só).
- Sacar lê e depois grava. Onde deve ficar a transação de banco, já que `application` não pode usar `@Transactional` do Spring? Pesquise as alternativas e escolha uma. (Guarde essa pergunta: ela volta no Desafio 3, com decorator.)

**Pesquise:** ports and adapters (Alistair Cockburn), driving vs driven adapters, value object, modelo de domínio anêmico vs rico, arquitetura em camadas vs hexagonal vs clean vs onion.

**Perguntas de prova**

- Explique arquitetura hexagonal em um minuto, para alguém que só conhece controller → service → repository.
- O que é uma porta? O que é um adaptador? Dê um exemplo de cada tipo neste projeto.
- Por que a interface `ContaRepository` fica no pacote `application` e não junto do JPA?
- Por que existem `Conta` e `ContaJpaEntity` separadas? Qual o custo disso? Quando você **não** usaria hexagonal?
- Seu método `sacar` está na `Conta` ou no service? O que é um modelo anêmico?
- Por que usar `BigDecimal` e nunca `double` para dinheiro?
- Se amanhã o depósito também chegar por uma fila de mensagens, o que muda no código? O que **não** muda?

---

## Desafio 3 — SOLID, herança, composição, polimorfismo e decorator

O pacote `legado` tem a classe `CalculadoraDeTarifasLegada`. Ela funciona e tem testes, mas é um catálogo de problemas. Você vai refatorá-la em passos, levando a lógica para dentro do hexágono (sugestão: `domain/tarifa`). A cada passo os mesmos cenários de `CalculadoraDeTarifasLegadaTest` precisam continuar valendo (adapte os testes à nova API, mantendo os valores).

**Missão [núcleo]**

1. **Diagnóstico.** Antes de mexer, liste por escrito cada problema que encontrar e qual letra do SOLID ele viola. Só depois continue.
2. **S — responsabilidade única.** A classe calcula, formata texto, guarda histórico em lista estática e imprime no console. Separe. Quantos "motivos para mudar" ela tinha?
3. **O — aberto/fechado + polimorfismo.** Elimine a cadeia de `if/else` por tipo de conta e operação. Critério: criar a "conta universitária" (TED a R$ 3,00, o resto grátis) deve exigir **apenas arquivos novos**, sem editar nenhuma classe de tarifa existente. Use `enum` no lugar das `String`.
4. **Classe abstrata vs interface.** Corrente, poupança e salário compartilham comportamento (arredondamento, tarifa nunca negativa). Implemente duas versões e fique com uma: (a) classe abstrata com o que é comum, (b) interface com composição. Escreva em um comentário por que escolheu.
5. **L — substituição de Liskov.** Hoje "conta salário + TED" lança `UnsupportedOperationException`: quem chama precisa conhecer o tipo para não tomar exceção. Redesenhe para que qualquer política de tarifa possa ser usada no lugar de outra sem surpresa.
6. **Decorator.** O desconto VIP é um `boolean` no meio do cálculo. Transforme em um decorator que embrulha **qualquer** política de tarifa. Depois crie um segundo decorator (por exemplo, tarifa promocional com teto de R$ 5,00) e combine os dois. A ordem de empilhar muda o resultado? Teste.
7. **I — segregação de interfaces.** Depois dos desafios 1 e 2 sua porta `ContaRepository` cresceu. O caso de uso de consulta precisa enxergar `salvar`? Quebre em portas menores e faça cada caso de uso depender só do que usa.
8. **D — inversão de dependência.** Não há o que implementar: aponte, no código que já existe, três lugares onde o princípio está aplicado e explique a "inversão" em cada um.
9. **Composição vs herança.** Adicione o tipo de conta (`CORRENTE`, `POUPANCA`, `SALARIO`) à abertura de conta e cobre a tarifa no saque. Modele o tipo por **composição** (a `Conta` *tem* um tipo/política), não por herança (`ContaCorrente extends Conta`). Escreva o que daria errado com herança quando um cliente migrar de salário para corrente.

**Pronto quando** a classe legada foi apagada, os cenários de tarifa continuam passando, a conta universitária entrou só com arquivos novos, e o saque cobra tarifa conforme o tipo.

**Missão [extra]**

- **Decorator em uma porta.** Crie `ContaRepositoryComLog`, que implementa a porta, embrulha o adaptador real e registra tempo de cada chamada. Monte na `CasosDeUsoConfig`. Depois use a mesma ideia para resolver a transação do Desafio 2: um decorator transacional do caso de uso, que vive fora do hexágono.
- Compare o seu decorator com o que o Spring AOP faz com `@Transactional`. O que é um proxy?

**Pesquise:** SOLID (Robert C. Martin), padrões Strategy, Decorator e Template Method, "composition over inheritance", `sealed interface` e `record` do Java 17+, proxy dinâmico e Spring AOP.

**Perguntas de prova**

- Enuncie cada letra do SOLID e dê um exemplo **deste projeto** para cada uma.
- Classe abstrata vs interface: diferenças técnicas (estado, construtor, herança múltipla, métodos `default`) e quando escolher cada uma. Depois do Java 8, o que ainda só a classe abstrata faz?
- O que é polimorfismo? Diferença entre sobrescrita (override) e sobrecarga (overload). Qual é resolvida em tempo de execução?
- Herança vs composição: por que "prefira composição"? Quando herança é a escolha certa?
- Explique o Decorator. Qual a diferença para herança? E para Proxy e Strategy?
- Onde o próprio Java usa Decorator? (Dica: `java.io`.)
- Dê um exemplo de violação de Liskov que não seja o clássico quadrado/retângulo.

---

## Desafio 4 — Java por dentro: lambdas, streams, threads e garbage collector

**Missão [núcleo]**

**A. Transferência e extrato**

- `POST /transferencias` com `{"contaOrigem": "...", "contaDestino": "...", "valor": 100.00}`. Debita de uma, credita na outra. Ou acontecem as duas coisas, ou nenhuma.
- Toda movimentação (depósito, saque, transferência enviada/recebida, tarifa) gera um **lançamento** imutável: conta, tipo, valor, data/hora.
- `GET /contas/{id}/extrato?de=2026-01-01&ate=2026-01-31&tipo=SAQUE` com filtros opcionais.

**B. Lambdas e streams**

- `GET /contas/{id}/extrato/resumo` devolve: total por tipo de lançamento, maior lançamento, média dos saques e os 3 dias com mais movimentação. Faça **tudo com streams** (`groupingBy`, `summingBigDecimal` não existe: descubra como somar `BigDecimal`), sem `for`.
- Os filtros do extrato devem ser `Predicate<Lancamento>` combinados com `and()`. Nada de `if` encadeado.
- Use `Optional` na busca por id, sem `get()` e sem `isPresent()`.

**C. Concorrência: reproduza um bug de verdade**

- Escreva um teste que dispara 50 threads (`ExecutorService` + `CountDownLatch`), cada uma fazendo 20 depósitos de R$ 1,00 na **mesma** conta, via caso de uso com banco real (H2). O saldo final deveria ser R$ 1.000,00. Rode algumas vezes e veja o que acontece.
- Explique por escrito, passo a passo, como duas threads perdem um depósito (*lost update*).
- Corrija com **lock otimista** (`@Version`) e trate o conflito. Depois experimente lock pessimista e compare.
- Responda: por que `synchronized` no método **não** resolve em produção, onde o serviço roda em várias instâncias?

**D. Garbage collector na prática**

- Rode o `conta-service` com pouca memória e log de GC: `java -Xmx128m -Xlog:gc* -jar conta-service/target/conta-service-0.0.1-SNAPSHOT.jar`. Gere carga (um laço chamando o extrato) e observe as coletas: jovens vs completas, pausas.
- Crie um vazamento de propósito: um endpoint temporário que acumula objetos em uma lista `static`. Rode com `-XX:+HeapDumpOnOutOfMemoryError`, estoure a memória e abra o heap dump no VisualVM ou Eclipse MAT. Ache o culpado. Apague o endpoint.

**Pronto quando** a transferência é atômica, o resumo usa só streams, o teste de concorrência falha antes da correção e passa depois, e você viu um `OutOfMemoryError` e achou a causa em um heap dump.

**Missão [extra]**

- `POST /contas/lote` abre várias contas consultando o antifraude **em paralelo** com `CompletableFuture` e um `Executor` próprio. Com `latenciaMs=1000` no modo caos, compare 10 contas em sequência vs em paralelo.
- Troque o executor por **virtual threads** (`Executors.newVirtualThreadPerTaskExecutor()`) e ligue `spring.threads.virtual.enabled=true`. O que muda? Para que tipo de tarefa virtual thread não ajuda?
- Rode com `-XX:+UseSerialGC`, `-XX:+UseParallelGC`, `-XX:+UseG1GC` e `-XX:+UseZGC` e compare as pausas no log.

**Pesquise:** interfaces funcionais (`Function`, `Predicate`, `Supplier`, `Consumer`), method reference, `Collectors`, avaliação preguiçosa de streams, `Thread` vs `Runnable` vs `Callable`, `ExecutorService`, `CompletableFuture`, `synchronized`/`volatile`/`AtomicInteger`, lock otimista e pessimista, Project Loom, heap vs stack, gerações (young/old), G1 vs ZGC, GC roots.

**Perguntas de prova**

- O que é uma expressão lambda? O que é uma interface funcional? Diferença entre lambda e classe anônima.
- Por que uma variável local usada em lambda precisa ser *effectively final*?
- Operações intermediárias vs terminais em streams. O que quer dizer que stream é *lazy*? Quando `parallelStream()` é má ideia?
- `map` vs `flatMap`. `Optional.orElse` vs `orElseGet`.
- Formas de criar uma thread. Por que usar pool em vez de `new Thread()`? Como dimensionar um pool para tarefa de CPU vs de I/O?
- `synchronized` vs `volatile` vs classes `Atomic*`. O que é condição de corrida? E deadlock: como acontece e como evitar?
- Lock otimista vs pessimista: como cada um funciona e quando usar. Qual escala melhor?
- Thread de plataforma vs virtual thread.
- Como o garbage collector decide que um objeto pode ser coletado? O que são GC roots?
- Heap vs stack. O que são as gerações e por que existem? O que é uma pausa *stop-the-world*?
- Java tem vazamento de memória? Dê dois exemplos. Como você investiga um?
- `System.gc()` garante a coleta? Diferença entre G1 e ZGC em uma frase.

---

## Desafio 5 — Sistemas distribuídos: quando o outro lado falha

No Desafio 0 você viu: se o antifraude cai, o `conta-service` devolve `500`; se fica lento, o `conta-service` fica lento junto. Em produção, isso derruba o banco em cascata. Use o **modo caos** (`PUT /caos` na porta 8081) para provocar cada cenário, antes e depois de corrigir.

Regra: tudo isto é detalhe técnico, então mora **no adaptador** (`adapter/out/antifraude`) ou em `config`. O caso de uso só conhece `AntifraudePort`.

**Missão [núcleo]** — faça nesta ordem, medindo o efeito de cada passo

1. **Timeout.** Hoje o `RestClient` espera para sempre. Configure timeout de conexão e de leitura. Teste com `latenciaMs=10000`.
2. **Tradução de erro.** Falha técnica do antifraude deve virar uma exceção que a aplicação entende (ex.: `AntifraudeIndisponivelException`), e a API responde `503`, não `500`.
3. **Retry.** Com Resilience4j, tente de novo em falhas **transitórias** (timeout, `503`), com backoff exponencial e *jitter*. Não repita em `4xx`. Teste com `taxaErro=0.5`.
4. **Circuit breaker.** Com `taxaErro=1`, depois de N falhas o circuito abre e o serviço para de chamar o antifraude, falhando rápido. Observe os três estados (`CLOSED`, `OPEN`, `HALF_OPEN`) nos logs enquanto liga e desliga o caos.
5. **Fallback: é decisão de negócio.** Com o antifraude fora, o banco (a) recusa a abertura ou (b) abre a conta como `PENDENTE_ANALISE`, sem poder movimentar, e analisa depois? Implemente a opção (b). Repare que parte disso é regra de negócio e vai para dentro do hexágono.
6. **Idempotência.** `POST /transferencias` passa a exigir o header `Idempotency-Key`. Repetir a mesma chave devolve a **mesma resposta** sem transferir de novo. Teste enviando a mesma requisição duas vezes, e também duas ao mesmo tempo.

**Pronto quando**, com o antifraude lento ou fora do ar, o `conta-service` responde em tempo curto e previsível, nunca com `500`, e uma transferência repetida não movimenta dinheiro duas vezes.

**Missão [extra]**

- **Bulkhead** e **rate limiter**: limite as chamadas simultâneas ao antifraude para que ele lento não consuma todas as threads do serviço.
- Processe as contas `PENDENTE_ANALISE` com um job agendado que consulta o antifraude de novo.
- Qual a ordem correta entre retry, circuit breaker e timeout quando combinados? Descubra a ordem padrão do Resilience4j e por que ela importa.

**Pesquise:** as 8 falácias da computação distribuída, Resilience4j, padrões de estabilidade do livro *Release It!* (Michael Nygard), *retry storm*, teorema CAP, consistência eventual, padrão Saga (orquestrada vs coreografada), *transactional outbox*, entrega *at-least-once* e consumidor idempotente.

**Perguntas de prova**

- Por que uma chamada de rede é diferente de uma chamada de método? Cite três falácias da computação distribuída.
- Explique os três estados do circuit breaker. O que ele protege: quem chama, quem é chamado, ou os dois?
- Retry sem backoff e sem limite pode piorar um incidente. Como?
- Por que retry em `POST` é perigoso? O que é idempotência e como você a implementou?
- Timeout, retry, circuit breaker, bulkhead e fallback: uma frase para cada.
- A transferência debita em um serviço e credita em **outro** serviço com outro banco de dados. Como garantir consistência sem transação distribuída? (Saga, compensação, outbox.) Por que 2PC é evitado?
- Teorema CAP: em uma partição de rede, um banco escolhe consistência ou disponibilidade para o saldo? E para o extrato?
- Vantagens e custos de microsserviços frente a um monólito. Quando você **não** quebraria em microsserviços?
- Comunicação síncrona (REST) vs assíncrona (mensageria): prós e contras.

---

## Desafio 6 — Observabilidade: Actuator, logs e rastreamento

Em um sistema distribuído, "funcionou na minha máquina" não ajuda. Você precisa enxergar o que aconteceu em produção, passando por vários serviços.

**Missão [núcleo]**

**A. Spring Boot Actuator** (nos dois serviços)

- Adicione o Actuator e exponha `health`, `info`, `metrics` e `prometheus`. Explore cada endpoint.
- Habilite as *probes* `liveness` e `readiness`.
- Crie um `HealthIndicator` que reflita o estado do antifraude/circuit breaker. O antifraude fora do ar deve derrubar o `readiness` do `conta-service`? Decida e justifique.
- Crie métricas próprias: contador de contas abertas (por resultado: aprovada, reprovada, pendente) e um timer da chamada ao antifraude. Veja-as em `/actuator/metrics`.

**B. Logging**

- Logue os eventos de negócio importantes com SLF4J, nível adequado e placeholders (`{}`), nunca concatenando `String`.
- Crie um filtro que lê ou gera um **correlation id** (header `X-Correlation-Id`), coloca no `MDC` e o devolve na resposta. Propague o header na chamada ao antifraude. O mesmo id deve aparecer nos logs dos **dois** serviços.
- **CPF não pode aparecer inteiro em log.** Mascare (`***.982.247-**`). Em banco isso é requisito (LGPD), não capricho.
- Ative log estruturado em JSON (`logging.structured.format.console`).

**C. Rastreamento distribuído**

- Adicione Micrometer Tracing com OpenTelemetry nos dois serviços e suba o Zipkin (`docker compose up -d zipkin`).
- Abra uma conta e encontre no Zipkin (http://localhost:9411) **um único trace** com spans dos dois serviços. Com `latenciaMs=2000`, onde o tempo aparece?
- Confira que `traceId` e `spanId` saem nos logs.

**Pronto quando** você consegue, a partir de um `X-Correlation-Id` ou `traceId`, achar os logs dos dois serviços e o trace da requisição, e nenhum CPF completo aparece em log.

**Missão [extra]: Dynatrace**

Não dá para instalar um Dynatrace corporativo em casa, então o objetivo é saber como ele se encaixa:

- Pesquise: o que é o **OneAgent** e como ele instrumenta uma JVM sem mudar código; o que é **PurePath**; o que a IA **Davis** faz; o que é um **SLO**.
- Compare as duas formas de mandar dados para o Dynatrace: OneAgent vs OpenTelemetry/Micrometer (`micrometer-registry-dynatrace`).
- Se quiser praticar: o Dynatrace tem trial gratuito. Aponte as métricas do `conta-service` para ele.

**Pesquise:** os três pilares da observabilidade, Micrometer, MDC, W3C Trace Context (header `traceparent`), OpenTelemetry, amostragem (*sampling*), APM, métricas RED e USE.

**Perguntas de prova**

- Log, métrica e trace: o que cada um responde? Dê um exemplo de pergunta que só cada um resolve.
- `traceId` vs `spanId`. Como o trace "atravessa" de um serviço para o outro?
- O que é o Spring Boot Actuator? Cite cinco endpoints e para que servem.
- Liveness vs readiness: o que o Kubernetes faz quando cada uma falha?
- Por que **não** expor todos os endpoints do Actuator em produção? Quais são perigosos?
- Níveis de log e quando usar cada um. O que é MDC e por que precisa ser limpo ao fim da requisição?
- Monitoramento vs observabilidade.
- O que o Dynatrace faz e o que é o OneAgent?

---

## Desafio 7 — Eventos, CQRS, event sourcing, SQL e NoSQL

**Missão [núcleo]**

**A. Eventos de domínio**

- Crie os eventos `ContaAberta`, `DepositoRealizado`, `SaqueRealizado` e `TransferenciaRealizada` (records imutáveis, no passado, no domínio).
- Crie a porta de saída `PublicadorDeEventos` e um primeiro adaptador simples, em memória (`ApplicationEventPublisher` do Spring).

**B. CQRS**

- Hoje o extrato é calculado em cima do modelo de escrita. Separe: os **comandos** (depositar, sacar, transferir) continuam como estão; as **consultas** de extrato passam a ler de um **modelo de leitura** próprio, uma projeção atualizada por quem escuta os eventos.
- Guarde a projeção de forma desnormalizada, pronta para a tela: um documento por conta com os lançamentos dentro. Sem Docker, use uma tabela separada; com Docker, use o MongoDB (extra).
- Torne o listener assíncrono (`@Async`) e observe: logo após um depósito, o extrato pode ainda não mostrar o lançamento. Isso é **consistência eventual**. Como a tela do app deveria lidar com isso?

**C. Event sourcing (mini)**

- Crie um *event store*: tabela só de inserção com `conta_id`, `sequencia`, `tipo`, `payload` (JSON) e `ocorrido_em`. Grave nela todo evento da conta.
- Implemente `Conta.reconstituirDe(List<EventoDeConta>)`, que recria o estado **reaplicando os eventos**, sem ler a tabela `conta`.
- Teste: após várias movimentações, o saldo reconstruído pelos eventos é igual ao saldo salvo.
- Use os eventos para responder algo que a tabela `conta` não sabe: "qual era o saldo desta conta em tal data e hora?"

**Pronto quando** escrita e leitura do extrato usam modelos diferentes, e você consegue reconstruir uma conta só a partir dos eventos.

**Missão [extra]**

- **SQL Server.** Suba com `docker compose up -d sqlserver`, crie um perfil `sqlserver` no Spring, adicione o driver `mssql-jdbc` e troque `ddl-auto` por migrações **Flyway**. Quantos arquivos de `domain` e `application` você alterou? (Deveria ser zero.)
- **MongoDB.** Mova o modelo de leitura para o Mongo (`docker compose up -d mongo`) como um segundo adaptador da mesma porta.
- Troque o adaptador de eventos em memória por um broker (Kafka ou RabbitMQ) e mova a projeção para um terceiro serviço.
- **Outbox:** como garantir que salvar a conta e publicar o evento aconteçam os dois, ou nenhum?
- Snapshots: contas com milhões de eventos demoram a reconstruir. Como resolver?

**Pesquise:** CQRS (Greg Young, Martin Fowler), event sourcing, projeção/read model, consistência eventual, ACID vs BASE, tipos de NoSQL (documento, chave-valor, colunar, grafo), *transactional outbox*, snapshot, índices clustered e non-clustered e níveis de isolamento no SQL Server.

**Perguntas de prova**

- O que é CQRS? Que problema resolve e que complexidade traz?
- O que é event sourcing? Dá para ter CQRS sem event sourcing? E o contrário?
- Por que event sourcing combina com sistemas financeiros? Cite desvantagens.
- Diferença entre evento e comando.
- SQL vs NoSQL: modelo de dados, esquema, transações (ACID vs BASE), joins, escalabilidade. Quando escolher cada um?
- Cite os quatro tipos de NoSQL com um exemplo de uso de cada.
- Por que o saldo de um banco costuma ficar em banco relacional? O que você colocaria em NoSQL?
- O que significa cada letra de ACID? O que são níveis de isolamento?
- Um evento pode ser entregue duas vezes. Como o consumidor se protege?

---

## Desafio 8 — Entrega: Docker, pipeline, Argo e Azure

Com menos de uma semana, trate este desafio como **majoritariamente teórico**: faça o núcleo e garanta as perguntas de prova.

**Missão [núcleo]**

- **Docker.** Escreva um `Dockerfile` *multi-stage* para cada serviço (build com JDK, execução com JRE, usuário não-root). Adicione os dois ao `docker-compose.yml` e faça o `conta-service` achar o antifraude pelo nome do serviço, via variável de ambiente.
- **Pipeline.** Escreva um `bitbucket-pipelines.yml` (ou um Bamboo Spec em YAML) com os estágios: compilar → testes → análise SonarQube com *quality gate* → SAST (Fortify) → build e push da imagem → deploy em homologação → DAST. Não precisa executar: precisa saber explicar por que cada estágio existe e nessa ordem.
- **Kubernetes + Argo CD.** Em uma pasta `deploy/`, escreva os manifestos do `conta-service`: `Deployment` (com as probes de liveness/readiness do Actuator, limites de CPU e memória), `Service` e `ConfigMap`. Escreva também o manifesto `Application` do Argo CD apontando para essa pasta.
- **Azure.** Faça uma tabela mapeando cada peça deste projeto para um serviço do Azure: onde rodam os contêineres, onde ficam as imagens, o banco SQL Server, os segredos, a mensageria, os logs e métricas.

**Missão [extra]**

- Suba um SonarQube local (`docker run -p 9000:9000 sonarqube:community`), adicione JaCoCo e analise o projeto. Corrija os *code smells* que ele apontar.
- Instale `kind` ou `minikube` + Argo CD e faça o deploy de verdade. Mude o número de réplicas com um commit e veja o Argo sincronizar.
- Pesquise **Argo Rollouts**: como seria um deploy canário do `conta-service` usando as métricas do Desafio 6 para decidir o avanço?

**Pesquise:** imagem vs contêiner, camadas de imagem, GitOps, Argo CD (sync, drift, self-heal) vs Argo Rollouts vs Argo Workflows, quality gate, SAST vs DAST vs SCA, OWASP Top 10, AKS, ACR, Azure SQL, Key Vault, Service Bus, Application Insights.

**Perguntas de prova**

- Imagem vs contêiner. Contêiner vs máquina virtual. Por que build multi-stage?
- Integração contínua vs entrega contínua vs deploy contínuo.
- O que o SonarQube mede? O que é um *quality gate*?
- **SAST** (Fortify) vs **DAST**: o que cada um analisa, em que momento do pipeline, e que tipo de falha cada um encontra que o outro não vê?
- O que é GitOps? Como o Argo CD difere de um pipeline que roda `kubectl apply`?
- Deploy *rolling* vs *blue-green* vs canário.
- Onde ficam as senhas da aplicação em produção? Por que nunca no repositório?
- Cite três itens do OWASP Top 10 e como este projeto se protegeria de cada um.

---

## Depois da prova

O que ficou de fora de propósito, por causa do prazo e do foco em Java: o frontend em **Angular** (um microfrontend de extrato consumindo esta API), um **BFF em Node.js/TypeScript** entre o Angular e os serviços, e autenticação com OAuth2/JWT. Quando quiser, eu monto a próxima trilha em cima deste mesmo projeto.
