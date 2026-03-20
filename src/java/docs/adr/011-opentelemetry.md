# ADR 010: OpenTelemetry Instrumentation for Java

## Status
Accepted

## Date
2026-03-17

## Context
The project-level Observability Strategy ADR ([docs/adr/007](../../../docs/adr/007-observability-strategy.md)) mandates OpenTelemetry as the standard instrumentation framework and defines shared semantic conventions, trace propagation, and export requirements. This ADR describes how those requirements are met in the Java / Spring Boot implementation.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) tracks the implementation work.

## Decision
Adopt the **OpenTelemetry Java Agent** for zero-code auto-instrumentation of the Spring Boot application.

### Instrumentation Summary

| Signal | Library / Mechanism | Official OTel? | Instrumentation Type |
|--------|---------------------|----------------|----------------------|
| Traces – Inbound HTTP | OTel Java Agent (Spring Web MVC instrumentation) | ✅ Yes | Zero-code |
| Traces – Outbound HTTP | OTel Java Agent (`RestTemplate`/`WebClient` instrumentation) | ✅ Yes | Zero-code |
| Traces – Database (JPA/JDBC) | OTel Java Agent (Hibernate/JDBC instrumentation) | ✅ Yes | Zero-code |
| Metrics – HTTP server | OTel Java Agent + Micrometer OTel bridge | ✅ Yes | Zero-code |
| Metrics – JVM runtime | `opentelemetry-runtime-telemetry-java17` (JFR-based, Java 17+) | ✅ Yes | Zero-code |
| Logs | `opentelemetry-logback-appender-1.0` (Logback → OTel bridge) | ✅ Yes | Config only |

> **Zero-code**: the Java Agent instruments everything via the `-javaagent` JVM flag — no changes to application source code.  
> **Config only**: adding the OTel appender to `logback-spring.xml` (one XML stanza).  
> All I/O signals (inbound HTTP, outbound HTTP, database) are fully covered by the Java Agent with no application code changes.

### Approach: Java Agent

Spring Boot's rich ecosystem is comprehensively covered by the OTel Java Agent (`opentelemetry-javaagent.jar`), which auto-instruments:
- Spring Web MVC (inbound HTTP spans)
- Spring `RestTemplate` / `WebClient` (outbound HTTP spans)
- JPA / Hibernate (database spans)
- JDBC drivers

### Maven Dependencies

The Java Agent handles all auto-instrumentation at runtime. No application code changes are needed for basic instrumentation.

For testing with `InMemorySpanExporter`:
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>1.44.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk-testing</artifactId>
    <version>1.44.0</version>
    <scope>test</scope>
</dependency>
```


## Alternatives considered

The **Spring Boot OpenTelemetry starter** (Spring Boot 3.3+) was evaluated as an alternative to the Java Agent. It bundles the OpenTelemetry SDK and auto-configuration via a starter dependency:
```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>2.10.0</version>
</dependency>
```

This ADR standardizes on the **OpenTelemetry Java Agent** as the default and supported approach for this repository. The Spring Boot starter remains a documented alternative but is **not** the standard configuration in this codebase.

### Instrumentation Scope

**Inbound HTTP spans:**
Auto-instrumented by the Java Agent / Spring Boot starter. Records `http.request.method`, `http.route`, `http.response.status_code`, `url.path`, `server.address`, and `server.port`.

**Outbound HTTP spans:**
Auto-instrumented for `RestTemplate` and `WebClient` by the agent. The `traceparent` header is injected automatically.

**Database spans:**
Auto-instrumented via JPA / Hibernate / JDBC instrumentation. `db.system`, `db.operation.name`, and sanitised `db.statement` are recorded. The agent sanitises bind parameters by default; verify with `otel.instrumentation.jdbc.statement-sanitizer.enabled=true` (default: enabled).

### Metrics Baseline

Spring Boot Actuator with Micrometer is the default metrics solution. Bridge to OTel using `micrometer-registry-otlp` or the agent's Micrometer bridge:

| Metric | Source |
|--------|--------|
| `http.server.request.duration` | Spring MVC auto-instrumentation |
| `http.server.active_requests` | Spring MVC auto-instrumentation |
| JVM runtime metrics | `io.opentelemetry.instrumentation:opentelemetry-runtime-telemetry-java17` (Java 17+) |

### Log Correlation
Use the `opentelemetry-logback-appender-1.0` or `opentelemetry-log4j-appender-2.17` appender to bridge SLF4J / Logback logs to OTel. When an active span exists, `trace_id` and `span_id` are automatically injected into MDC and the structured log record.

With Logback, add the OTel appender to `logback-spring.xml`:
```xml
<appender name="OpenTelemetry"
          class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    <captureExperimentalAttributes>true</captureExperimentalAttributes>
</appender>
```

### Propagation
W3C Trace Context is the default propagation format for the OTel Java Agent. No additional configuration is required.

### Export and Collector Configuration

**With Java Agent:**
Pass agent configuration via JVM system properties or environment variables:

```bash
# Export enabled (endpoint provided)
java -javaagent:opentelemetry-javaagent.jar \
     -Dotel.service.name=lamp-control-api-java \
     -Dotel.exporter.otlp.endpoint=http://collector:4317 \
     -jar lamp-control-api.jar

# No-op-by-default exporting (endpoint omitted, exporters disabled)
java -javaagent:opentelemetry-javaagent.jar \
     -Dotel.service.name=lamp-control-api-java \
     -Dotel.traces.exporter=none \
     -Dotel.metrics.exporter=none \
     -Dotel.logs.exporter=none \
     -jar lamp-control-api.jar
```

| Variable / Property | Default | Description |
|--------------------|---------|-------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | *(none)* | OTLP endpoint; used when exporters are configured for `otlp` |
| `OTEL_SERVICE_NAME` | `lamp-control-api-java` | `service.name` resource attribute |
| `OTEL_RESOURCE_ATTRIBUTES` | *(none)* | Additional resource attributes |
| `OTEL_TRACES_SAMPLER` | `parentbased_always_on` | Sampling strategy |
| `OTEL_LOGS_EXPORTER` | `otlp` | Log exporter implementation (`none` to disable log export) |

To comply with the global observability ADR's "no-op-by-default when endpoint is absent" requirement, the platform runtime MUST explicitly disable exporters whenever `OTEL_EXPORTER_OTLP_ENDPOINT` is not set. This can be achieved by setting the following environment variables (or equivalent `-Dotel.*.exporter` system properties):

- `OTEL_TRACES_EXPORTER=none`
- `OTEL_METRICS_EXPORTER=none`
- `OTEL_LOGS_EXPORTER=none`

**With Spring Boot starter (no agent):**
Configure in `application.yml`:
```yaml
management:
  opentelemetry:
    resource-attributes:
      service.name: lamp-control-api-java
      deployment.environment: ${SPRING_PROFILES_ACTIVE:development}
```

### Testing and Verification
- Unit tests are unaffected; `OpenTelemetry.noop()` is the default SDK when no provider is configured.
- Integration tests using `@SpringBootTest` + Testcontainers MAY register an `InMemorySpanExporter` bean to assert span names and attributes.
- A local Docker Compose profile `observability` SHOULD start Jaeger and the OTel Collector alongside the PostgreSQL container.

## Consequences

### Positive
- Java Agent provides near-zero-code instrumentation for Spring, JPA, and JDBC.
- Spring Boot starter offers tighter integration and programmatic configuration.
- Micrometer bridge reuses existing Actuator metrics infrastructure.

### Negative
- Java Agent approach adds an `-javaagent` JVM flag to startup scripts and Dockerfiles.
- Agent version must be kept aligned with OTel API version to avoid class-loading conflicts.
- Two approaches (agent vs. starter) require a team decision on which to standardise.

## References
- [docs/adr/007-observability-strategy.md](../../../docs/adr/007-observability-strategy.md)
- Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
- [Spring Boot OTel Starter](https://opentelemetry.io/docs/zero-code/java/spring-boot-starter/)
- [Micrometer OTel Registry](https://micrometer.io/docs/registry/otlp)
