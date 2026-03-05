# serilogj

_serilogj_ é uma biblioteca de logging estruturado para Java, baseada no [Serilog para .NET](https://serilog.net). Ela usa templates de mensagem com placeholders nomeados (ex: `"Olá {Nome}"`) e suporta desestruturação de objetos com o prefixo `@` (ex: `{@usuario}`). Funciona muito bem com o [Seq](https://getseq.net) para busca em logs.

**Compatível com JDK 8, 11, 17, 21 e 25.** Java puro, sem dependências externas.

## Como Começar

### Maven

```xml
<dependency>
  <groupId>org.serilogj</groupId>
  <artifactId>serilogj</artifactId>
  <version>0.6.1</version>
</dependency>
```

### Configuração Básica

```java
import serilogj.Log;
import serilogj.LoggerConfiguration;
import serilogj.events.LogEventLevel;
import static serilogj.sinks.coloredconsole.ColoredConsoleSinkConfigurator.*;
import static serilogj.sinks.rollingfile.RollingFileSinkConfigurator.*;
import static serilogj.sinks.seq.SeqSinkConfigurator.*;

Log.setLogger(new LoggerConfiguration()
    .setMinimumLevel(LogEventLevel.Verbose)
    .writeTo(coloredConsole())
    .writeTo(rollingFile("logs/app-{Date}.log"), LogEventLevel.Information)
    .writeTo(seq("http://localhost:5341/"))
    .createLogger());
```

### Registrando Logs

```java
Log.information("Processando {Quantidade} registros", registros.length);
Log.warning("Pouco espaco em disco: {EspacoMB} MB restantes", espacoMB);
Log.error(excecao, "Falha ao processar pedido {PedidoId}", pedidoId);
```

### Niveis de Log

Seis niveis, do menos ao mais severo:

```java
Log.verbose("Informacao detalhada de rastreamento");
Log.debug("Diagnostico interno");
Log.information("Eventos normais de operacao");
Log.warning("Algo inesperado aconteceu");
Log.error(ex, "Uma operacao falhou");
Log.fatal(ex, "A aplicacao nao pode continuar");
```

### Desestruturacao de Objetos

Use `@` para desestruturar objetos em suas propriedades:

```java
Usuario usuario = new Usuario();
usuario.setNome("joao");

// Escalar: registra usuario.toString()
Log.information("Ola {usuario}", usuario);

// Desestruturado: registra todas as propriedades publicas como dados estruturados
Log.information("Ola {@usuario}", usuario);
```

### Contexto de Origem

Adiciona a classe que fez a chamada ao evento de log:

```java
ILogger logger = Log.forContext(MeuServico.class);
logger.information("Servico iniciado");
// Resultado: ... [Information] Servico iniciado {SourceContext: "com.exemplo.MeuServico"}
```

## Sinks (Destinos de Log)

Sinks sao os destinos para onde os logs sao enviados.

### Console Colorido

Saida no console com cores ANSI:

```java
import static serilogj.sinks.coloredconsole.ColoredConsoleSinkConfigurator.*;

.writeTo(coloredConsole())
.writeTo(coloredConsole("{Timestamp:yyyy-MM-dd HH:mm:ss} [{Level}] {Message}{NewLine}{Exception}"))
```

### Console Simples

Saida no console sem cores:

```java
import static serilogj.sinks.console.ConsoleSinkConfigurator.*;

.writeTo(console())
.writeTo(console("{Timestamp:HH:mm:ss} [{Level}] {Message}{NewLine}{Exception}"))
```

### Arquivo Rotativo

Rotacao diaria de arquivos de log:

```java
import static serilogj.sinks.rollingfile.RollingFileSinkConfigurator.*;

.writeTo(rollingFile("logs/app-{Date}.log"))
```

### Seq

Envia eventos estruturados para um servidor [Seq](https://getseq.net):

```java
import static serilogj.sinks.seq.SeqSinkConfigurator.*;

.writeTo(seq("http://localhost:5341/"))
.writeTo(seq("http://localhost:5341/", "sua-api-key"))
```

### Wrapper Assincrono

Envolve qualquer sink para escrita assincrona (nao bloqueia a thread principal):

```java
import serilogj.sinks.async.AsyncWrapperSink;

ILogEventSink sinkInterno = coloredConsole();
.writeTo(new AsyncWrapperSink(sinkInterno))       // buffer padrao: 10000
.writeTo(new AsyncWrapperSink(sinkInterno, 5000))  // buffer customizado
```

### Nivel Minimo por Sink

Cada sink pode ter seu proprio nivel minimo:

```java
.writeTo(coloredConsole(), LogEventLevel.Verbose)              // console recebe tudo
.writeTo(rollingFile("app-{Date}.log"), LogEventLevel.Warning) // arquivo so warnings+
```

## Enrichers (Enriquecedores)

Adicionam informacoes extras a todos os eventos de log:

```java
import serilogj.core.enrichers.*;

Log.setLogger(new LoggerConfiguration()
    .with(new ThreadIdEnricher())
    .with(new ThreadNameEnricher())
    .with(new ProcessIdEnricher())
    .with(new MachineNameEnricher())
    .writeTo(coloredConsole())
    .createLogger());
```

| Enricher | Propriedade | Valor |
|----------|-------------|-------|
| `ThreadIdEnricher` | `ThreadId` | ID da thread atual |
| `ThreadNameEnricher` | `ThreadName` | Nome da thread atual |
| `ProcessIdEnricher` | `ProcessId` | PID do processo JVM |
| `MachineNameEnricher` | `MachineName` | Nome da maquina |

## Override de Nivel Minimo

Controla a verbosidade por contexto de origem (pacote/namespace):

```java
Log.setLogger(new LoggerConfiguration()
    .setMinimumLevel(LogEventLevel.Information)
    .setMinimumLevelOverride("com.exemplo.libbarulhenta", LogEventLevel.Warning)
    .setMinimumLevelOverride("com.exemplo.debug", LogEventLevel.Verbose)
    .writeTo(coloredConsole())
    .createLogger());
```

Isso e util quando uma biblioteca gera logs demais e voce quer silencia-la sem afetar o resto.

## Configuracao por Arquivo

Configure o logging via arquivo `.properties`:

**serilogj.properties:**
```properties
serilogj.minimum-level=Information
serilogj.enrich.0=ThreadId
serilogj.enrich.1=MachineName
```

```java
import serilogj.configuration.PropertiesFileConfiguration;

LoggerConfiguration config = PropertiesFileConfiguration.configure("serilogj.properties");
Log.setLogger(config.writeTo(coloredConsole()).createLogger());
```

Registrar sinks customizados:
```java
import serilogj.configuration.SinkRegistry;

SinkRegistry.register("console", props -> new ConsoleSink(
    props.getOrDefault("template", "{Message}{NewLine}"), null));
```

## Log Context (Contexto de Log)

Adiciona e remove propriedades dentro de um escopo:

```java
import serilogj.context.LogContext;
import serilogj.core.enrichers.LogContextEnricher;

// Adicione o LogContextEnricher na configuracao
.with(new LogContextEnricher())

// Use no codigo
try (AutoCloseable prop = LogContext.pushProperty("RequestId", requestId)) {
    Log.information("Processando requisicao");  // inclui RequestId
    processarRequisicao();
    Log.information("Requisicao concluida");    // inclui RequestId
}
// RequestId nao aparece mais
```

## Finalizacao

Sempre feche o logger ao encerrar a aplicacao para garantir que os buffers sejam enviados:

```java
Log.closeAndFlush();
```

## Build

```bash
mvn compile      # Compilar
mvn test         # Rodar testes (50 testes)
mvn package      # Gerar JAR
mvn verify       # Build completo + testes
```

## Creditos

Criado originalmente por [Jerremy Koot](https://github.com/jerremykoot) e [80dB](https://github.com/80dB) como um port Java do [Serilog](https://serilog.net) de Nicholas Blumhardt.

Este fork inclui correcoes de bugs, compatibilidade com JDK 8-25, correcoes de vazamento de recursos, melhorias de thread safety, suite de testes e novas funcionalidades (enrichers, console sink, async sink, override de nivel minimo, configuracao por arquivo).

## Licenca

Apache License 2.0 — veja [LICENSE](LICENSE) para detalhes.
