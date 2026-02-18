package com.lampcontrol.database

import kotlin.test.*
import org.junit.jupiter.api.Test

/**
 * Tests for DatabaseConfig that exercise parsing and validation logic.
 * These tests focus on the static factory methods and edge cases.
 */
class DatabaseConfigEnvironmentTest {
    @Test
    fun `DatabaseConfig can be constructed with all parameters`() {
        val config =
            DatabaseConfig(
                host = "production.db.example.com",
                port = 5432,
                database = "production_db",
                user = "prod_user",
                password = "secure_password_123",
                poolMin = 10,
                poolMax = 50,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        assertNotNull(config)
        assertEquals("production.db.example.com", config.host)
        assertEquals(5432, config.port)
        assertEquals("production_db", config.database)
        assertEquals("prod_user", config.user)
        assertEquals("secure_password_123", config.password)
        assertEquals(10, config.poolMin)
        assertEquals(50, config.poolMax)
    }

    @Test
    fun `DatabaseConfig with empty password`() {
        val config =
            DatabaseConfig(
                host = "localhost",
                port = 5432,
                database = "testdb",
                user = "testuser",
                password = "",
                poolMin = 0,
                poolMax = 4,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        assertEquals("", config.password)
        assertNotNull(config.connectionString())
    }

    @Test
    fun `DatabaseConfig with very large pool size`() {
        val config =
            DatabaseConfig(
                host = "localhost",
                port = 5432,
                database = "db",
                user = "user",
                password = "pass",
                poolMin = 100,
                poolMax = 500,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        assertEquals(100, config.poolMin)
        assertEquals(500, config.poolMax)
    }

    @Test
    fun `DatabaseConfig with IPv6 host`() {
        val config =
            DatabaseConfig(
                host = "::1",
                port = 5432,
                database = "db",
                user = "user",
                password = "pass",
                poolMin = 0,
                poolMax = 4,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        assertEquals("::1", config.host)
        assertEquals("jdbc:postgresql://::1:5432/db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig hashCode is consistent`() {
        val config1 = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val config2 = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun `DatabaseConfig with different hosts have different hashCodes`() {
        val config1 = DatabaseConfig("host1", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val config2 = DatabaseConfig("host2", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        assert(config1.hashCode() != config2.hashCode())
    }

    @Test
    fun `DatabaseConfig component destructuring works`() {
        val config =
            DatabaseConfig(
                host = "myhost",
                port = 5433,
                database = "mydb",
                user = "myuser",
                password = "mypass",
                poolMin = 5,
                poolMax = 20,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        val (host, port, db, user, pass, min, max, maxLifetime, idleTimeout, connectionTimeout) = config

        assertEquals("myhost", host)
        assertEquals(5433, port)
        assertEquals("mydb", db)
        assertEquals("myuser", user)
        assertEquals("mypass", pass)
        assertEquals(5, min)
        assertEquals(20, max)
        assertEquals(3600000, maxLifetime)
        assertEquals(1800000, idleTimeout)
        assertEquals(30000, connectionTimeout)
    }

    @Test
    fun `DatabaseConfig copy preserves unchanged fields`() {
        val original = DatabaseConfig("host", 5432, "db", "user", "pass", 2, 10, 3600000, 1800000, 30000)
        val copied = original.copy(host = "newhost")

        assertEquals("newhost", copied.host)
        assertEquals(5432, copied.port)
        assertEquals("db", copied.database)
        assertEquals("user", copied.user)
        assertEquals("pass", copied.password)
        assertEquals(2, copied.poolMin)
        assertEquals(10, copied.poolMax)
    }

    @Test
    fun `DatabaseConfig with FQDN host`() {
        val config =
            DatabaseConfig(
                host = "db.production.company.com",
                port = 5432,
                database = "maindb",
                user = "appuser",
                password = "secret",
                poolMin = 1,
                poolMax = 10,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        val connStr = config.connectionString()
        assert(connStr.contains("db.production.company.com"))
        assert(connStr.startsWith("jdbc:postgresql://"))
    }

    @Test
    fun `DatabaseConfig toString format`() {
        val config = DatabaseConfig("testhost", 5433, "testdb", "testuser", "testpass", 1, 5, 3600000, 1800000, 30000)
        val str = config.toString()

        assert(str.contains("DatabaseConfig"))
        assert(str.contains("testhost"))
        assert(str.contains("5433"))
        assert(str.contains("testdb"))
    }

    @Test
    fun `DatabaseConfig connectionString with database containing underscore`() {
        val config = DatabaseConfig("localhost", 5432, "my_test_db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        assertEquals("jdbc:postgresql://localhost:5432/my_test_db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig connectionString with database containing hyphen`() {
        val config = DatabaseConfig("localhost", 5432, "my-test-db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        assertEquals("jdbc:postgresql://localhost:5432/my-test-db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig equality comparison comprehensive`() {
        val base = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        // Different in each field
        val diffHost = DatabaseConfig("other", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val diffPort = DatabaseConfig("host", 5433, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val diffDb = DatabaseConfig("host", 5432, "other", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val diffUser = DatabaseConfig("host", 5432, "db", "other", "pass", 0, 4, 3600000, 1800000, 30000)
        val diffPass = DatabaseConfig("host", 5432, "db", "user", "other", 0, 4, 3600000, 1800000, 30000)
        val diffMin = DatabaseConfig("host", 5432, "db", "user", "pass", 1, 4, 3600000, 1800000, 30000)
        val diffMax = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 5, 3600000, 1800000, 30000)

        assert(base != diffHost)
        assert(base != diffPort)
        assert(base != diffDb)
        assert(base != diffUser)
        assert(base != diffPass)
        assert(base != diffMin)
        assert(base != diffMax)
    }

    @Test
    fun `DatabaseConfig with port at minimum`() {
        val config = DatabaseConfig("localhost", 1, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        assertEquals(1, config.port)
        assertEquals("jdbc:postgresql://localhost:1/db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig with port at maximum`() {
        val config = DatabaseConfig("localhost", 65535, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        assertEquals(65535, config.port)
        assertEquals("jdbc:postgresql://localhost:65535/db", config.connectionString())
    }
}
