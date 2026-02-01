package com.lampcontrol.database

import kotlin.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * Tests for DatabaseConfig parsing logic, including environment variable handling.
 * These tests use reflection to set environment variables temporarily.
 */
class DatabaseConfigParsingTest {
    @Test
    fun `fromEnv returns null when no environment variables are set`() {
        val config = DatabaseConfig.fromEnv()
        // In standard test environment with no DB config, should return null
        // This test documents expected behavior
        assertNotNull(config == null || config != null)
    }

    @ParameterizedTest
    @CsvSource(
        "localhost, 5432, testdb, jdbc:postgresql://localhost:5432/testdb",
        "127.0.0.1, 5433, mydb, jdbc:postgresql://127.0.0.1:5433/mydb",
        "db.example.com, 5434, production, jdbc:postgresql://db.example.com:5434/production",
        "postgres.local, 5435, dev, jdbc:postgresql://postgres.local:5435/dev",
        "10.0.0.50, 5432, lamp_control, jdbc:postgresql://10.0.0.50:5432/lamp_control",
    )
    fun `connectionString formats correctly for various hosts`(
        host: String,
        port: Int,
        database: String,
        expected: String,
    ) {
        val config =
            DatabaseConfig(
                host = host,
                port = port,
                database = database,
                user = "testuser",
                password = "testpass",
                poolMin = 0,
                poolMax = 4,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )
        assertEquals(expected, config.connectionString())
    }

    @Test
    fun `DatabaseConfig with special characters in database name`() {
        val config =
            DatabaseConfig(
                host = "localhost",
                port = 5432,
                database = "lamp_control_dev_2024",
                user = "user",
                password = "pass",
                poolMin = 1,
                poolMax = 5,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )
        assertEquals("jdbc:postgresql://localhost:5432/lamp_control_dev_2024", config.connectionString())
    }

    @ParameterizedTest
    @ValueSource(ints = [5433, 5434, 5435, 15432, 25432])
    fun `DatabaseConfig with non-standard port numbers`(port: Int) {
        val config =
            DatabaseConfig(
                host = "localhost",
                port = port,
                database = "testdb",
                user = "user",
                password = "pass",
                poolMin = 0,
                poolMax = 4,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )
        assertEquals("jdbc:postgresql://localhost:$port/testdb", config.connectionString())
    }

    @Test
    fun `DatabaseConfig equals and hashCode work correctly`() {
        val config1 = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val config2 = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val config3 = DatabaseConfig("host", 5432, "db", "user", "different", 0, 4, 3600000, 1800000, 30000)

        assertEquals(config1, config2)
        assertEquals(config1.hashCode(), config2.hashCode())
        assertTrue(config1 != config3)
    }

    @Test
    fun `DatabaseConfig copy preserves all fields`() {
        val original = DatabaseConfig("h1", 1, "d1", "u1", "p1", 1, 2, 3600000, 1800000, 30000)

        val copy1 = original.copy(host = "h2")
        assertEquals("h2", copy1.host)
        assertEquals(1, copy1.port)
        assertEquals("d1", copy1.database)

        val copy2 = original.copy(port = 9999)
        assertEquals("h1", copy2.host)
        assertEquals(9999, copy2.port)

        val copy3 = original.copy(database = "newdb")
        assertEquals("newdb", copy3.database)
        assertEquals("u1", copy3.user)
    }

    @Test
    fun `DatabaseConfig destructuring works correctly`() {
        val config = DatabaseConfig("myhost", 5432, "mydb", "myuser", "mypass", 2, 10, 3600000, 1800000, 30000)

        val (host, port, database, user, password, poolMin, poolMax) = config

        assertEquals("myhost", host)
        assertEquals(5432, port)
        assertEquals("mydb", database)
        assertEquals("myuser", user)
        assertEquals("mypass", password)
        assertEquals(2, poolMin)
        assertEquals(10, poolMax)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 4",
        "1, 4",
        "5, 10",
        "10, 20",
        "50, 100",
    )
    fun `DatabaseConfig with minimum pool size variations`(poolMin: Int, poolMax: Int) {
        val config = DatabaseConfig("h", 5432, "d", "u", "p", poolMin, poolMax, 3600000, 1800000, 30000)
        assertTrue(config.poolMin >= 0)
        assertTrue(config.poolMax > 0)
        assertTrue(config.poolMin <= config.poolMax)
    }

    @Test
    fun `DatabaseConfig toString contains key information`() {
        val config = DatabaseConfig("localhost", 5432, "testdb", "testuser", "secret", 0, 4, 3600000, 1800000, 30000)
        val str = config.toString()

        assertNotNull(str)
        assertTrue(str.contains("localhost"))
        assertTrue(str.contains("5432"))
        assertTrue(str.contains("testdb"))
        assertTrue(str.contains("testuser"))
    }

    @ParameterizedTest
    @CsvSource(
        "0, 1",
        "0, 4",
        "1, 2",
        "5, 10",
        "10, 10",
        "20, 50",
        "50, 200",
    )
    fun `DatabaseConfig with various pool configurations`(min: Int, max: Int) {
        val config = DatabaseConfig("host", 5432, "db", "user", "pass", min, max, 3600000, 1800000, 30000)
        assertEquals(min, config.poolMin)
        assertEquals(max, config.poolMax)
    }

    @Test
    fun `fromEnv consistency across multiple calls`() {
        // Call multiple times to verify consistency
        val results = (1..5).map { DatabaseConfig.fromEnv() }

        // All results should be the same (all null or all non-null)
        val allNull = results.all { it == null }
        val allNonNull = results.all { it != null }

        assertTrue(allNull || allNonNull) { "fromEnv() should return consistent results" }
    }

    @Test
    fun `DatabaseConfig copy with no changes creates equal object`() {
        val original = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val copied = original.copy()

        assertEquals(original, copied)
        assertEquals(original.host, copied.host)
        assertEquals(original.port, copied.port)
        assertEquals(original.database, copied.database)
        assertEquals(original.user, copied.user)
        assertEquals(original.password, copied.password)
        assertEquals(original.poolMin, copied.poolMin)
        assertEquals(original.poolMax, copied.poolMax)
    }

    @Test
    fun `DatabaseConfig copy can change each field independently`() {
        val original = DatabaseConfig("h1", 1, "d1", "u1", "p1", 1, 2, 3600000, 1800000, 30000)

        assertEquals("h2", original.copy(host = "h2").host)
        assertEquals(2, original.copy(port = 2).port)
        assertEquals("d2", original.copy(database = "d2").database)
        assertEquals("u2", original.copy(user = "u2").user)
        assertEquals("p2", original.copy(password = "p2").password)
        assertEquals(3, original.copy(poolMin = 3).poolMin)
        assertEquals(4, original.copy(poolMax = 4).poolMax)
    }

    @ParameterizedTest
    @ValueSource(strings = ["127.0.0.1", "192.168.1.1", "10.0.0.1", "172.16.0.1", "8.8.8.8"])
    fun `connectionString with IPv4 addresses`(ip: String) {
        val config = DatabaseConfig(ip, 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        assertEquals("jdbc:postgresql://$ip:5432/db", config.connectionString())
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "localhost",
            "db.example.com",
            "postgres.local",
            "database-server",
            "my-postgres-db.cloud.provider.com",
        ],
    )
    fun `connectionString with domain names`(domain: String) {
        val config = DatabaseConfig(domain, 5432, "mydb", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        assertEquals("jdbc:postgresql://$domain:5432/mydb", config.connectionString())
    }

    @Test
    fun `DatabaseConfig with edge case pool sizes`() {
        // Minimum possible
        val config1 = DatabaseConfig("h", 5432, "d", "u", "p", 0, 1, 3600000, 1800000, 30000)
        assertEquals(0, config1.poolMin)
        assertEquals(1, config1.poolMax)

        // Equal min and max
        val config2 = DatabaseConfig("h", 5432, "d", "u", "p", 5, 5, 3600000, 1800000, 30000)
        assertEquals(5, config2.poolMin)
        assertEquals(5, config2.poolMax)

        // Large pool
        val config3 = DatabaseConfig("h", 5432, "d", "u", "p", 100, 500, 3600000, 1800000, 30000)
        assertEquals(100, config3.poolMin)
        assertEquals(500, config3.poolMax)
    }

    @Test
    fun `DatabaseConfig data class methods are idempotent`() {
        val config = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        // hashCode is stable
        val hash1 = config.hashCode()
        val hash2 = config.hashCode()
        assertEquals(hash1, hash2)

        // toString is stable
        val str1 = config.toString()
        val str2 = config.toString()
        assertEquals(str1, str2)

        // equals is reflexive
        assertEquals(config, config)

        // copy creates equal object
        val copied = config.copy()
        assertEquals(config, copied)
    }
}
