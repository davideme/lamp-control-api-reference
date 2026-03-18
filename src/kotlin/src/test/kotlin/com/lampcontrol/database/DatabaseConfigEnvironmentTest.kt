package com.lampcontrol.database

import org.junit.jupiter.api.Test
import kotlin.test.*

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
            )

        assertNotNull(config)
        assertEquals("production.db.example.com", config.host)
        assertEquals(5432, config.port)
        assertEquals("production_db", config.database)
        assertEquals("prod_user", config.user)
        assertEquals("secure_password_123", config.password)
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
            )

        assertEquals("", config.password)
        assertNotNull(config.connectionString())
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
            )

        assertEquals("::1", config.host)
        assertEquals("jdbc:postgresql://::1:5432/db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig hashCode is consistent`() {
        val config1 = DatabaseConfig("host", 5432, "db", "user", "pass")
        val config2 = DatabaseConfig("host", 5432, "db", "user", "pass")

        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun `DatabaseConfig with different hosts have different hashCodes`() {
        val config1 = DatabaseConfig("host1", 5432, "db", "user", "pass")
        val config2 = DatabaseConfig("host2", 5432, "db", "user", "pass")

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
            )

        val (host, port, db, user, pass) = config

        assertEquals("myhost", host)
        assertEquals(5433, port)
        assertEquals("mydb", db)
        assertEquals("myuser", user)
        assertEquals("mypass", pass)
    }

    @Test
    fun `DatabaseConfig copy preserves unchanged fields`() {
        val original = DatabaseConfig("host", 5432, "db", "user", "pass")
        val copied = original.copy(host = "newhost")

        assertEquals("newhost", copied.host)
        assertEquals(5432, copied.port)
        assertEquals("db", copied.database)
        assertEquals("user", copied.user)
        assertEquals("pass", copied.password)
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
            )

        val connStr = config.connectionString()
        assert(connStr.contains("db.production.company.com"))
        assert(connStr.startsWith("jdbc:postgresql://"))
    }

    @Test
    fun `DatabaseConfig toString format`() {
        val config = DatabaseConfig("testhost", 5433, "testdb", "testuser", "testpass")
        val str = config.toString()

        assert(str.contains("DatabaseConfig"))
        assert(str.contains("testhost"))
        assert(str.contains("5433"))
        assert(str.contains("testdb"))
    }

    @Test
    fun `DatabaseConfig connectionString with database containing underscore`() {
        val config = DatabaseConfig("localhost", 5432, "my_test_db", "user", "pass")

        assertEquals("jdbc:postgresql://localhost:5432/my_test_db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig connectionString with database containing hyphen`() {
        val config = DatabaseConfig("localhost", 5432, "my-test-db", "user", "pass")

        assertEquals("jdbc:postgresql://localhost:5432/my-test-db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig equality comparison comprehensive`() {
        val base = DatabaseConfig("host", 5432, "db", "user", "pass")

        val diffHost = DatabaseConfig("other", 5432, "db", "user", "pass")
        val diffPort = DatabaseConfig("host", 5433, "db", "user", "pass")
        val diffDb = DatabaseConfig("host", 5432, "other", "user", "pass")
        val diffUser = DatabaseConfig("host", 5432, "db", "other", "pass")
        val diffPass = DatabaseConfig("host", 5432, "db", "user", "other")

        assert(base != diffHost)
        assert(base != diffPort)
        assert(base != diffDb)
        assert(base != diffUser)
        assert(base != diffPass)
    }

    @Test
    fun `DatabaseConfig with port at minimum`() {
        val config = DatabaseConfig("localhost", 1, "db", "user", "pass")

        assertEquals(1, config.port)
        assertEquals("jdbc:postgresql://localhost:1/db", config.connectionString())
    }

    @Test
    fun `DatabaseConfig with port at maximum`() {
        val config = DatabaseConfig("localhost", 65535, "db", "user", "pass")

        assertEquals(65535, config.port)
        assertEquals("jdbc:postgresql://localhost:65535/db", config.connectionString())
    }
}
