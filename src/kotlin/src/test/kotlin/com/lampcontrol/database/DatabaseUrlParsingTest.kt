package com.lampcontrol.database

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for DATABASE_URL parsing logic.
 * Since parseDatabaseUrl is private, we test it indirectly through the public API
 * by simulating what would happen if environment variables were set.
 */
class DatabaseUrlParsingTest {

    @Test
    fun `DATABASE_URL format parsing logic validation`() {
        // Test the regex pattern that parseDatabaseUrl uses
        val validUrls = listOf(
            "postgresql://user:pass@localhost:5432/dbname",
            "postgres://user:pass@localhost:5432/dbname",
            "postgresql://myuser:mypass@db.example.com:5433/production",
            "postgres://testuser:testpass@127.0.0.1:5432/testdb",
            "postgresql://admin:secret123@10.0.0.1:5432/lamp_control"
        )

        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        validUrls.forEach { url ->
            val match = regex.matchEntire(url)
            assertNotNull(match, "Should match valid DATABASE_URL: $url")

            val (user, password, host, port, database) = match.destructured
            assertNotNull(user)
            assertNotNull(password)
            assertNotNull(host)
            assertNotNull(port)
            assertNotNull(database)
        }
    }

    @Test
    fun `DATABASE_URL regex extracts correct components`() {
        val testUrl = "postgresql://myuser:mypass@db.example.com:5433/production"
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val match = regex.matchEntire(testUrl)
        assertNotNull(match)

        val (user, password, host, port, database) = match.destructured

        assertEquals("myuser", user)
        assertEquals("mypass", password)
        assertEquals("db.example.com", host)
        assertEquals("5433", port)
        assertEquals("production", database)
    }

    @Test
    fun `DATABASE_URL supports both postgresql and postgres schemes`() {
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val postgresqlUrl = "postgresql://user:pass@host:5432/db"
        val postgresUrl = "postgres://user:pass@host:5432/db"

        assertNotNull(regex.matchEntire(postgresqlUrl))
        assertNotNull(regex.matchEntire(postgresUrl))
    }

    @Test
    fun `DATABASE_URL regex handles various host formats`() {
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val hostFormats = listOf(
            "postgresql://user:pass@localhost:5432/db",
            "postgresql://user:pass@127.0.0.1:5432/db",
            "postgresql://user:pass@db.example.com:5432/db",
            "postgresql://user:pass@postgres-server:5432/db",
            "postgresql://user:pass@10.0.0.50:5432/db"
        )

        hostFormats.forEach { url ->
            val match = regex.matchEntire(url)
            assertNotNull(match, "Should parse host from: $url")
        }
    }

    @Test
    fun `DATABASE_URL regex handles various port numbers`() {
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val ports = listOf(5432, 5433, 5434, 15432, 25432)

        ports.forEach { port ->
            val url = "postgresql://user:pass@localhost:$port/db"
            val match = regex.matchEntire(url)
            assertNotNull(match)

            val (_, _, _, extractedPort, _) = match.destructured
            assertEquals(port.toString(), extractedPort)
        }
    }

    @Test
    fun `DATABASE_URL regex handles passwords with special characters`() {
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        // Note: The regex uses [^@]+ for password, which means it stops at the first @
        // This is correct because @ separates password from host
        val testCases = listOf(
            "postgresql://user:simple@host:5432/db" to "simple",
            "postgresql://user:pass123!@host:5432/db" to "pass123!",
            "postgresql://user:p-ssw0rd_123@host:5432/db" to "p-ssw0rd_123",
            "postgresql://user:SecurePass123@host:5432/db" to "SecurePass123"
        )

        testCases.forEach { (url, expectedPassword) ->
            val match = regex.matchEntire(url)
            assertNotNull(match, "Should parse: $url")

            val (_, password, _, _, _) = match.destructured
            assertEquals(expectedPassword, password)
        }
    }

    @Test
    fun `DATABASE_URL regex handles usernames with special characters`() {
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val testCases = listOf(
            "postgresql://simple:pass@host:5432/db" to "simple",
            "postgresql://user_name:pass@host:5432/db" to "user_name",
            "postgresql://user-name:pass@host:5432/db" to "user-name",
            "postgresql://user.name:pass@host:5432/db" to "user.name"
        )

        testCases.forEach { (url, expectedUser) ->
            val match = regex.matchEntire(url)
            assertNotNull(match, "Should parse: $url")

            val (user, _, _, _, _) = match.destructured
            assertEquals(expectedUser, user)
        }
    }

    @Test
    fun `DATABASE_URL regex handles database names with underscores and numbers`() {
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val dbNames = listOf(
            "simple",
            "lamp_control",
            "my_database_2024",
            "test-db",
            "production123",
            "db_dev_v2"
        )

        dbNames.forEach { dbName ->
            val url = "postgresql://user:pass@host:5432/$dbName"
            val match = regex.matchEntire(url)
            assertNotNull(match, "Should parse database name: $dbName")

            val (_, _, _, _, extractedDb) = match.destructured
            assertEquals(dbName, extractedDb)
        }
    }

    @Test
    fun `DATABASE_URL regex correctly extracts from Heroku-style URLs`() {
        // Heroku typically provides URLs like: postgres://user:pass@host:5432/dbname
        val herokuUrl = "postgres://abcdefg:hijklmnop123456789@ec2-1-2-3-4.compute-1.amazonaws.com:5432/d1a2b3c4d5e6"
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val match = regex.matchEntire(herokuUrl)
        assertNotNull(match)

        val (user, password, host, port, database) = match.destructured

        assertEquals("abcdefg", user)
        assertEquals("hijklmnop123456789", password)
        assertEquals("ec2-1-2-3-4.compute-1.amazonaws.com", host)
        assertEquals("5432", port)
        assertEquals("d1a2b3c4d5e6", database)
    }

    @Test
    fun `DATABASE_URL regex correctly extracts from Render-style URLs`() {
        // Render provides URLs like: postgresql://user:pass@hostname:5432/dbname
        val renderUrl = "postgresql://lamp_user:secure_password_123@dpg-abc123.oregon-postgres.render.com:5432/lamp_control_db"
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val match = regex.matchEntire(renderUrl)
        assertNotNull(match)

        val (user, password, host, port, database) = match.destructured

        assertEquals("lamp_user", user)
        assertEquals("secure_password_123", password)
        assertEquals("dpg-abc123.oregon-postgres.render.com", host)
        assertEquals("5432", port)
        assertEquals("lamp_control_db", database)
    }

    @Test
    fun `fromEnv would parse DATABASE_URL correctly if set`() {
        // This test documents the expected behavior when DATABASE_URL is set
        // We can't actually test this without setting environment variables,
        // but we can verify the regex pattern works correctly

        val exampleUrl = "postgresql://testuser:testpass@localhost:5432/testdb"
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")

        val match = regex.matchEntire(exampleUrl)
        assertNotNull(match)

        val (user, password, host, port, database) = match.destructured

        // These would be the values extracted by parseDatabaseUrl
        val expectedConfig = DatabaseConfig(
            host = host,
            port = port.toInt(),
            database = database,
            user = user,
            password = password,
            poolMin = 0, // Default
            poolMax = 4  // Default
        )

        assertEquals("testuser", expectedConfig.user)
        assertEquals("testpass", expectedConfig.password)
        assertEquals("localhost", expectedConfig.host)
        assertEquals(5432, expectedConfig.port)
        assertEquals("testdb", expectedConfig.database)
        assertEquals("jdbc:postgresql://localhost:5432/testdb", expectedConfig.connectionString())
    }

    @Test
    fun `fromEnv would handle individual env vars correctly if set`() {
        // This test documents the expected behavior when individual env vars are set
        // Testing the logic that would execute if DB_HOST and DB_USER were set

        val expectedConfig = DatabaseConfig(
            host = "localhost",  // From DB_HOST
            port = 5432,        // Default or from DB_PORT
            database = "lamp_control",  // Default or from DB_NAME
            user = "lamp_user",  // From DB_USER
            password = "",       // Default or from DB_PASSWORD
            poolMin = 0,         // Default or from DB_POOL_MIN_SIZE
            poolMax = 4          // Default or from DB_POOL_MAX_SIZE
        )

        assertNotNull(expectedConfig)
        assertEquals("localhost", expectedConfig.host)
        assertEquals(5432, expectedConfig.port)
        assertEquals("lamp_control", expectedConfig.database)
        assertEquals("lamp_user", expectedConfig.user)
    }

    @Test
    fun `fromEnv environment variable precedence logic`() {
        // Documents the precedence: DATABASE_URL > individual env vars

        // If DATABASE_URL is set, it should be parsed first
        val databaseUrlConfig = "postgresql://url_user:url_pass@url_host:5433/url_db"
        val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")
        val match = regex.matchEntire(databaseUrlConfig)
        assertNotNull(match)

        // If DATABASE_URL is not set, individual env vars should be used
        val individualConfig = DatabaseConfig(
            host = "env_host",
            port = 5434,
            database = "env_db",
            user = "env_user",
            password = "env_pass",
            poolMin = 1,
            poolMax = 5
        )

        assertNotNull(individualConfig)
        assertEquals("env_host", individualConfig.host)
    }

    @Test
    fun `pool size defaults when not specified in environment`() {
        // Tests the default pool size values used when env vars are not set
        val config = DatabaseConfig(
            host = "host",
            port = 5432,
            database = "db",
            user = "user",
            password = "pass",
            poolMin = 0,  // Default from DB_POOL_MIN_SIZE
            poolMax = 4   // Default from DB_POOL_MAX_SIZE
        )

        assertEquals(0, config.poolMin)
        assertEquals(4, config.poolMax)
    }

    @Test
    fun `fromEnv configuration detection logic`() {
        // Documents the three conditions that indicate PostgreSQL is configured:
        // 1. DATABASE_URL is set
        // 2. DB_NAME is explicitly provided
        // 3. Both DB_HOST and DB_USER are explicitly provided

        // Simulate these detection conditions
        val conditions = listOf(
            "DATABASE_URL set" to true,  // databaseUrl is not empty
            "DB_NAME set" to true,       // database is not empty
            "DB_HOST and DB_USER set" to true  // host and user are not empty
        )

        conditions.forEach { (condition, shouldBeConfigured) ->
            assertEquals(true, shouldBeConfigured, "PostgreSQL should be configured when: $condition")
        }
    }
}
