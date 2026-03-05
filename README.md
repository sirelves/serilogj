# serilogj

_serilogj_ is a structured logging library for Java, ported from [Serilog for .NET](https://serilog.net). It uses message templates with named placeholders (e.g., `"Hello {Name}"`) and supports object destructuring with the `@` prefix (e.g., `{@user}`). Designed for use with [Seq](https://getseq.net) for structured log searching.

**Compatible with JDK 8, 11, 17, 21 and 25.** Pure Java, no external dependencies.

## Getting Started

### Maven

```xml
<dependency>
  <groupId>org.serilogj</groupId>
  <artifactId>serilogj</artifactId>
  <version>0.6.1</version>
</dependency>
```

### Basic Configuration

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

### Logging

```java
Log.information("Processing {RecordCount} records", records.length);
Log.warning("Disk space is low: {SpaceMB} MB remaining", spaceMB);
Log.error(exception, "Failed to process order {OrderId}", orderId);
```

### Log Levels

Six levels, from least to most severe:

```java
Log.verbose("Detailed trace information");
Log.debug("Internal diagnostic info");
Log.information("Normal operation events");
Log.warning("Something unexpected happened");
Log.error(ex, "An operation failed");
Log.fatal(ex, "Application cannot continue");
```

### Object Destructuring

Use `@` to destructure objects into their properties:

```java
User user = new User();
user.setUserName("john");

// Scalar: logs user.toString()
Log.information("Hello {user}", user);

// Destructured: logs all public properties of user as structured data
Log.information("Hello {@user}", user);
```

### Source Context

Add the calling class to log events:

```java
ILogger logger = Log.forContext(MyService.class);
logger.information("Service started");
// Produces: ... [Information] Service started {SourceContext: "com.example.MyService"}
```

## Sinks

### Colored Console

ANSI-colored console output:

```java
import static serilogj.sinks.coloredconsole.ColoredConsoleSinkConfigurator.*;

.writeTo(coloredConsole())
.writeTo(coloredConsole("{Timestamp:yyyy-MM-dd HH:mm:ss} [{Level}] {Message}{NewLine}{Exception}"))
```

### Plain Console

Simple console output without ANSI codes:

```java
import static serilogj.sinks.console.ConsoleSinkConfigurator.*;

.writeTo(console())
.writeTo(console("{Timestamp:HH:mm:ss} [{Level}] {Message}{NewLine}{Exception}"))
```

### Rolling File

Daily log file rotation with optional retention:

```java
import static serilogj.sinks.rollingfile.RollingFileSinkConfigurator.*;

.writeTo(rollingFile("logs/app-{Date}.log"))
```

### Seq

Send structured events to a [Seq](https://getseq.net) server:

```java
import static serilogj.sinks.seq.SeqSinkConfigurator.*;

.writeTo(seq("http://localhost:5341/"))
.writeTo(seq("http://localhost:5341/", "your-api-key"))
```

### Async Wrapper

Wrap any sink for non-blocking async writes:

```java
import serilogj.sinks.async.AsyncWrapperSink;

ILogEventSink innerSink = coloredConsole();
.writeTo(new AsyncWrapperSink(innerSink))       // default buffer: 10000
.writeTo(new AsyncWrapperSink(innerSink, 5000))  // custom buffer size
```

### Per-Sink Minimum Level

```java
.writeTo(coloredConsole(), LogEventLevel.Verbose)     // console gets everything
.writeTo(rollingFile("app-{Date}.log"), LogEventLevel.Warning) // file only warnings+
```

## Enrichers

Add contextual information to every log event:

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

| Enricher | Property | Value |
|----------|----------|-------|
| `ThreadIdEnricher` | `ThreadId` | Current thread ID |
| `ThreadNameEnricher` | `ThreadName` | Current thread name |
| `ProcessIdEnricher` | `ProcessId` | JVM process ID |
| `MachineNameEnricher` | `MachineName` | Hostname |

## Minimum Level Override

Control log verbosity per source context (namespace):

```java
Log.setLogger(new LoggerConfiguration()
    .setMinimumLevel(LogEventLevel.Information)
    .setMinimumLevelOverride("com.example.noisylib", LogEventLevel.Warning)
    .setMinimumLevelOverride("com.example.debug", LogEventLevel.Verbose)
    .writeTo(coloredConsole())
    .createLogger());
```

## Properties File Configuration

Configure logging from a `.properties` file:

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

Register custom sinks:
```java
import serilogj.configuration.SinkRegistry;

SinkRegistry.register("console", props -> new ConsoleSink(
    props.getOrDefault("template", "{Message}{NewLine}"), null));
```

## Log Context

Push and pop properties within a scope:

```java
import serilogj.context.LogContext;
import serilogj.core.enrichers.LogContextEnricher;

// Add LogContextEnricher during configuration
.with(new LogContextEnricher())

// Use in code
try (AutoCloseable prop = LogContext.pushProperty("RequestId", requestId)) {
    Log.information("Handling request");  // includes RequestId
    processRequest();
    Log.information("Request complete");  // includes RequestId
}
// RequestId no longer attached
```

## Cleanup

Always close the logger on application shutdown to flush buffered sinks:

```java
Log.closeAndFlush();
```

## Building

```bash
mvn compile      # Compile
mvn test         # Run tests (50 tests)
mvn package      # Build JAR
mvn verify       # Full build + tests
```

## What's New in This Fork

### Bug Fixes

- **RollingFileSink** — Fixed duplicate condition that could skip file rotation
- **BooleanScalarConversionPolicy** — Boolean values were detected but never returned (result was always null)
- **ScalarValue** — TemporalAccessor formatting (LocalDateTime, ZonedDateTime, etc.) was broken due to inverted condition
- **LogEventProperty** — `isValidName()` accepted any string with at least one non-space character; now validates that names start with a letter or underscore and contain only letters, digits, or underscores
- **LogEvent** — Fixed typo `remotePropertyIfPresent` → `removePropertyIfPresent` (old name kept as deprecated)
- **PropertyBinder** — All internal log format strings were invalid (`%1`, `%2`, `{0}`), causing errors in self-diagnostics
- **FileSink** — File size limit was declared but never enforced; now checks file size before writing

### JDK 8–25 Compatibility

- **Reflection** — `setAccessible(true)` fails on JDK 16+ without `--add-opens`; now wrapped in try-catch with graceful skip
- **JsonFormatter** — Replaced `new Integer(c)` (removed in JDK 16) with `(int) c`
- **SeqSink** — Replaced hardcoded charset string `"UTF8"` with `StandardCharsets.UTF_8`
- **Maven Compiler Plugin** — Updated from 3.0 (2012) to 3.13.0

### Resource Leaks & Thread Safety

- **SeqSink** — HTTP connections and streams were never properly closed; now uses try-with-resources and `disconnect()`. String concatenation in loop replaced with StringBuilder
- **FileSink** — `output` was not set to null after close
- **MessageTemplateCache** — HashMap with synchronized blocks replaced with ConcurrentHashMap; eliminates race conditions
- **Log** — `_logger` field is now `volatile` for safe cross-thread visibility
- **EnumScalarConversionPolicy** — HashMap with weak synchronization replaced with ConcurrentHashMap + `computeIfAbsent`
- **JsonFormatter** — SimpleDateFormat created on every call replaced with ThreadLocal for reuse

### New Features

- **Plain Console Sink** — Simple console output without ANSI escape codes, useful for environments that don't support colors (CI, file redirection)
- **Async Wrapper Sink** — Wraps any sink with a background thread and BlockingQueue for non-blocking log writes
- **Enrichers** — ThreadId, ThreadName, ProcessId, MachineName — attach runtime context to every log event
- **Minimum Level Override** — Set different log levels per source context (e.g., silence a noisy library while keeping your own code verbose)
- **Properties File Configuration** — Configure minimum level and enrichers from a `.properties` file; register custom sinks via SinkRegistry

### Testing & CI

- **50 unit tests** across 9 test classes covering parsers, formatters, policies, sinks, cache, reflection, and end-to-end logging
- **JUnit 5** + Maven Surefire Plugin 3.2.5
- **CI workflow** for GitHub Actions with JDK matrix: 8, 11, 17, 21, 25
- All tests pass on JDK 8, 17, and 25 (tested locally)

## Credits

Originally created by [Jerremy Koot](https://github.com/jerremykoot) and [80dB](https://github.com/80dB) as a Java port of [Serilog](https://serilog.net) by Nicholas Blumhardt.

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
