# ADR 004: Ktor as Web Framework for Kotlin REST API

## Status

Accepted

## Date

2025-06-22

## Context

The Kotlin implementation of the Lamp Control API requires a web framework for creating REST JSON endpoints. Given the project's emphasis on being "Kotlin idiomatic as possible," we need to select a framework that:

- Embraces Kotlin language features and idioms
- Provides native coroutine support for async operations
- Offers type-safe configuration and routing
- Minimizes boilerplate code
- Leverages Kotlin's functional programming capabilities
- Supports modern Kotlin patterns like DSLs, extension functions, and data classes

Available web framework options for Kotlin include:

1. **Ktor** - Kotlin-native web framework by JetBrains
2. **Spring Boot** - Java-based framework with Kotlin support
3. **Spring WebFlux** - Reactive Spring framework
4. **Micronaut** - Compile-time framework with Kotlin support
5. **Quarkus** - Cloud-native framework with Kotlin support

## Decision

We will use **Ktor** as our web framework for the Kotlin REST API implementation.

## Rationale

### Why Ktor is Most Kotlin Idiomatic

1. **Kotlin-First Design**
   ```kotlin
   // Ktor routing DSL - pure Kotlin idioms
   routing {
       route("/api/lamps") {
           get {
               call.respond(lampService.findAll())
           }
           post {
               val request = call.receive<CreateLampRequest>()
               call.respond(HttpStatusCode.Created, lampService.create(request))
           }
       }
   }
   ```

2. **Native Coroutine Support**
   ```kotlin
   // Ktor handlers are suspend functions by default
   get("/lamps/{id}") {
       val id = call.parameters["id"]?.toLongOrNull() 
           ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
       
       val lamp = lampService.findByIdAsync(id) // Suspend function
       call.respond(lamp)
   }
   ```

3. **Type-Safe Configuration**
   ```kotlin
   // Type-safe application configuration
   fun Application.module() {
       install(ContentNegotiation) {
           json(Json {
               prettyPrint = true
               isLenient = true
               ignoreUnknownKeys = true
           })
       }
       
       install(CallLogging) {
           level = Level.INFO
           filter { call -> call.request.path().startsWith("/api") }
       }
   }
   ```

4. **Kotlin DSL Throughout**
   ```kotlin
   // Testing with Kotlin DSL
   @Test
   fun `should create lamp successfully`() = testApplication {
       application {
           configureRouting()
           configureSerialization()
       }
       
       client.post("/api/lamps") {
           contentType(ContentType.Application.Json)
           setBody(CreateLampRequest("Living Room Lamp", false))
       }.apply {
           assertEquals(HttpStatusCode.Created, status)
           val lamp = body<Lamp>()
           assertEquals("Living Room Lamp", lamp.name)
       }
   }
   ```

5. **Functional Programming Patterns**
   ```kotlin
   // Pipeline-style request processing
   route("/api/lamps") {
       authenticate("auth-jwt") {
           get {
               val filters = call.request.queryParameters
               val lamps = lampService.findAll()
                   .filter { lamp -> filters["room"]?.let { lamp.room == it } ?: true }
                   .filter { lamp -> filters["status"]?.let { lamp.isOn == it.toBoolean() } ?: true }
                   .sortedBy { it.name }
               
               call.respond(lamps)
           }
       }
   }
   ```

6. **Extension Functions for Clean Code**
   ```kotlin
   // Custom extension functions for common patterns
   suspend fun ApplicationCall.respondCreated(resource: Any) {
       respond(HttpStatusCode.Created, resource)
   }
   
   suspend fun ApplicationCall.validateAndReceive<T : Any>(type: KClass<T>): T {
       return try {
           receive(type)
       } catch (e: Exception) {
           throw BadRequestException("Invalid request body: ${e.message}")
       }
   }
   
   // Usage in routes
   post("/lamps") {
       val request = call.validateAndReceive<CreateLampRequest>()
       val lamp = lampService.create(request)
       call.respondCreated(lamp)
   }
   ```

7. **Immutable Data Classes and Sealed Classes**
   ```kotlin
   @Serializable
   data class Lamp(
       val id: Long,
       val name: String,
       val isOn: Boolean,
       val brightness: Int = 100,
       val room: String? = null,
       val createdAt: Instant = Clock.System.now()
   )
   
   @Serializable
   sealed class LampEvent {
       @Serializable
       data class TurnedOn(val lampId: Long, val timestamp: Instant) : LampEvent()
       
       @Serializable
       data class TurnedOff(val lampId: Long, val timestamp: Instant) : LampEvent()
       
       @Serializable
       data class BrightnessChanged(val lampId: Long, val newBrightness: Int, val timestamp: Instant) : LampEvent()
   }
   ```

### Kotlin Idioms Supported by Ktor

1. **Scope Functions**
   ```kotlin
   install(StatusPages) {
       exception<LampNotFoundException> { call, cause ->
           call.respond(
               HttpStatusCode.NotFound,
               ErrorResponse(
                   error = "LAMP_NOT_FOUND",
                   message = cause.message ?: "Lamp not found"
               )
           )
       }
   }
   ```

2. **Delegation and Property Delegates**
   ```kotlin
   class LampController(private val lampService: LampService) {
       private val logger by lazy { LoggerFactory.getLogger(this::class.java) }
       
       suspend fun handleGetLamp(call: ApplicationCall) {
           val id by call.parameters
           // ... rest of implementation
       }
   }
   ```

3. **Higher-Order Functions**
   ```kotlin
   fun Route.lampRoutes(lampService: LampService) {
       route("/lamps") {
           get { handleWithErrorHandling { lampService.findAll() } }
           post { handleWithErrorHandling { lampService.create(call.receive()) } }
           
           route("/{id}") {
               get { handleWithErrorHandling { lampService.findById(call.getLampId()) } }
               put { handleWithErrorHandling { lampService.update(call.getLampId(), call.receive()) } }
               delete { handleWithErrorHandling { lampService.delete(call.getLampId()) } }
           }
       }
   }
   
   private suspend fun ApplicationCall.handleWithErrorHandling(
       block: suspend () -> Any
   ) {
       try {
           val result = block()
           respond(result)
       } catch (e: Exception) {
           respond(HttpStatusCode.InternalServerError, ErrorResponse.from(e))
       }
   }
   ```

## Framework Comparison for Kotlin Idioms

| Framework | Kotlin DSL | Coroutines | Type Safety | Functional Style | Immutability | Score |
|-----------|------------|------------|-------------|------------------|--------------|-------|
| **Ktor** | ✅ Native | ✅ Native | ✅ Full | ✅ Excellent | ✅ Encouraged | 5/5 |
| Spring Boot | ⚠️ Limited | ⚠️ Added | ⚠️ Partial | ⚠️ Java-style | ⚠️ Mixed | 2/5 |
| Spring WebFlux | ⚠️ Limited | ✅ Good | ⚠️ Partial | ⚠️ Reactive | ⚠️ Mixed | 3/5 |
| Micronaut | ⚠️ Limited | ✅ Good | ⚠️ Partial | ⚠️ Java-style | ⚠️ Mixed | 2.5/5 |

## Project Structure for Maximum Kotlin Idioms

```
src/main/kotlin/
├── Application.kt                 # Main application entry point
├── plugins/                       # Ktor plugins configuration
│   ├── Routing.kt
│   ├── Serialization.kt
│   ├── Monitoring.kt
│   └── Security.kt
├── domain/                        # Domain models (data classes)
│   ├── Lamp.kt
│   ├── LampEvent.kt
│   └── LampRepository.kt
├── service/                       # Business logic (suspend functions)
│   └── LampService.kt
├── routes/                        # Route definitions (DSL)
│   └── LampRoutes.kt
├── dto/                          # Request/Response DTOs (data classes)
│   ├── CreateLampRequest.kt
│   ├── UpdateLampRequest.kt
│   └── ErrorResponse.kt
└── extensions/                   # Kotlin extensions
    ├── ApplicationCallExtensions.kt
    └── ValidationExtensions.kt
```

## Dependencies for Idiomatic Kotlin

```kotlin
dependencies {
    // Core Ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    
    // Content negotiation with kotlinx.serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    
    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    
    // Kotlin datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetime_version")
    
    // Validation with Kotlin contracts
    implementation("io.ktor:ktor-server-request-validation:$ktor_version")
    
    // Testing with Kotlin DSL
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.mockk:mockk:$mockk_version")
}
```

## Alternatives Considered

### Spring Boot
**Pros:**
- Mature ecosystem with extensive libraries
- Enterprise features and patterns
- Large community and documentation

**Cons:**
- Java-centric design philosophy
- Annotation-heavy approach (less functional)
- Blocking I/O by default
- Heavy runtime footprint
- Configuration through properties/YAML rather than type-safe Kotlin

### Spring WebFlux
**Pros:**
- Reactive programming model
- Better coroutine support than Spring MVC
- Non-blocking I/O

**Cons:**
- Still Java-centric in design
- Complex reactive programming model
- Mixed paradigms (reactive + imperative)
- Less intuitive than Kotlin coroutines

## Consequences

### Positive
- **Maximum Kotlin Idioms**: Full leverage of Kotlin language features
- **Type Safety**: Compile-time safety throughout the application
- **Performance**: Lightweight runtime with fast startup
- **Async by Default**: Native coroutine support for scalable APIs
- **Testability**: Excellent testing support with Kotlin DSL
- **Maintainability**: Clean, readable code following Kotlin conventions

### Negative
- **Smaller Ecosystem**: Fewer third-party libraries compared to Spring
- **Learning Curve**: Different from traditional Java frameworks
- **Enterprise Features**: May need additional libraries for enterprise patterns

### Neutral
- **Community**: Growing but smaller than Spring ecosystem
- **Documentation**: Good but less extensive than Spring

## Migration Considerations

Since ADR 003 shows Spring Boot configuration, we need to:

1. **Update build.gradle.kts** to use Ktor dependencies
2. **Remove Spring Boot plugins** and add Ktor configuration
3. **Create Ktor application structure** following Kotlin idioms
4. **Implement the OpenAPI specification** using Ktor routing DSL

## Future Considerations

1. **Ktor 3.0+**: Continue following Ktor evolution for new Kotlin features
2. **Kotlin Multiplatform**: Potential for sharing code with client applications
3. **Kotlin Serialization**: Full adoption of kotlinx.serialization for JSON
4. **Coroutine Flow**: Integration with Kotlin Flow for streaming APIs

## References

- [Ktor Documentation](https://ktor.io/docs/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Kotlin Idioms](https://kotlinlang.org/docs/idioms.html)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [Ktor Best Practices](https://ktor.io/docs/development-mode.html)
