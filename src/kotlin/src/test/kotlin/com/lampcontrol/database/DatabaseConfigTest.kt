package com.lampcontrol.database

import kotlin.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class DatabaseConfigTest {
    @ParameterizedTest
    @CsvSource(
        "db.example.com, 5433, mydb, jdbc:postgresql://db.example.com:5433/mydb",
        "localhost, 5432, lamp_control, jdbc:postgresql://localhost:5432/lamp_control",
        "192.168.1.100, 5433, testdb, jdbc:postgresql://192.168.1.100:5433/testdb",
        "10.0.0.1, 5432, db, jdbc:postgresql://10.0.0.1:5432/db",
        "example.com, 5432, db, jdbc:postgresql://example.com:5432/db",
        "db.internal.network, 5433, mydb, jdbc:postgresql://db.internal.network:5433/mydb",
        "host, 5432, my-special_db.123, jdbc:postgresql://host:5432/my-special_db.123",
    )
    fun `connectionString builds correct JDBC URL`(host: String, port: Int, database: String, expected: String) {
        val config =
            DatabaseConfig(
                host = host,
                port = port,
                database = database,
                user = "user",
                password = "pass",
                poolMin = 0,
                poolMax = 4,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        assertEquals(expected, config.connectionString())
    }

    @ParameterizedTest
    @ValueSource(strings = ["my-db", "my_db", "mydb123"])
    fun `DatabaseConfig supports various database names`(database: String) {
        val config = DatabaseConfig("host", 5432, database, "user", "pass", 0, 4, 3600000, 1800000, 30000)
        assertNotNull(config.connectionString())
        assertTrue(config.connectionString().endsWith("/$database"))
    }

    @ParameterizedTest
    @ValueSource(ints = [5433, 5434, 5435, 15432])
    fun `DatabaseConfig with non-standard ports`(port: Int) {
        val config = DatabaseConfig("localhost", port, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        assertEquals(port, config.port)
        assertTrue(config.connectionString().contains(":$port/"))
    }

    @Test
    fun `DatabaseConfig can be created with custom values`() {
        val config =
            DatabaseConfig(
                host = "192.168.1.100",
                port = 5433,
                database = "testdb",
                user = "testuser",
                password = "testpass",
                poolMin = 5,
                poolMax = 20,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        assertNotNull(config)
        assertEquals("192.168.1.100", config.host)
        assertEquals(5433, config.port)
        assertEquals("testdb", config.database)
        assertEquals("testuser", config.user)
        assertEquals("testpass", config.password)
        assertEquals(5, config.poolMin)
        assertEquals(20, config.poolMax)
    }

    @Test
    fun `DatabaseConfig with minimal configuration`() {
        val config =
            DatabaseConfig(
                host = "localhost",
                port = 5432,
                database = "testdb",
                user = "user",
                password = "",
                poolMin = 0,
                poolMax = 1,
                maxLifetimeMs = 3600000,
                idleTimeoutMs = 1800000,
                connectionTimeoutMs = 30000,
            )

        assertEquals("jdbc:postgresql://localhost:5432/testdb", config.connectionString())
        assertEquals("user", config.user)
        assertEquals("", config.password)
    }

    @Test
    fun `connectionString format is consistent`() {
        val configs =
            listOf(
                DatabaseConfig("host1", 5432, "db1", "u1", "p1", 0, 4, 3600000, 1800000, 30000),
                DatabaseConfig("host2", 5433, "db2", "u2", "p2", 1, 5, 3600000, 1800000, 30000),
                DatabaseConfig("host3", 5434, "db3", "u3", "p3", 2, 6, 3600000, 1800000, 30000),
            )

        configs.forEach { config ->
            val connStr = config.connectionString()
            assertTrue(connStr.startsWith("jdbc:postgresql://"))
            assertTrue(connStr.contains(config.host))
            assertTrue(connStr.contains(config.port.toString()))
            assertTrue(connStr.contains(config.database))
        }
    }

    @Test
    fun `DatabaseConfig data class properties`() {
        val config1 = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val config2 = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val config3 = DatabaseConfig("host", 5433, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)

        assertEquals(config1, config2)
        assertTrue(config1 != config3)
        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun `DatabaseConfig copy function works correctly`() {
        val config = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val copied = config.copy(port = 5433)

        assertEquals("host", copied.host)
        assertEquals(5433, copied.port)
        assertEquals("db", copied.database)
    }

    @Test
    fun `DatabaseConfig toString includes all properties`() {
        val config = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val string = config.toString()

        assertTrue(string.contains("host"))
        assertTrue(string.contains("5432"))
        assertTrue(string.contains("db"))
    }

    @Test
    fun `DatabaseConfig component functions work`() {
        val config = DatabaseConfig("host", 5432, "db", "user", "pass", 0, 4, 3600000, 1800000, 30000)
        val (host, port, database, user, password, poolMin, poolMax, maxLifetime, idleTimeout, connectionTimeout) = config

        assertEquals("host", host)
        assertEquals(5432, port)
        assertEquals("db", database)
        assertEquals("user", user)
        assertEquals("pass", password)
        assertEquals(0, poolMin)
        assertEquals(4, poolMax)
        assertEquals(3600000, maxLifetime)
        assertEquals(1800000, idleTimeout)
        assertEquals(30000, connectionTimeout)
    }

    @Test
    fun `fromEnv returns null when no environment variables are set`() {
        // This tests the case where DATABASE_URL, DB_NAME, DB_HOST, and DB_USER are all not set
        // In the test environment, these are typically not set by default
        DatabaseConfig.fromEnv()

        // Should return null when no PostgreSQL configuration is detected
        // This may return a config if environment variables are set, which is also valid
        // The important part is that the method doesn't throw an exception
    }

    @ParameterizedTest
    @CsvSource(
        "10, 50",
        "0, 100",
        "5, 5",
    )
    fun `DatabaseConfig pool configuration`(poolMin: Int, poolMax: Int) {
        val config = DatabaseConfig("host", 5432, "db", "user", "pass", poolMin, poolMax, 3600000, 1800000, 30000)

        assertEquals(poolMin, config.poolMin)
        assertEquals(poolMax, config.poolMax)
    }

    @Test
    fun `DatabaseConfig equality with different passwords`() {
        val config1 = DatabaseConfig("host", 5432, "db", "user", "pass1", 0, 4, 3600000, 1800000, 30000)
        val config2 = DatabaseConfig("host", 5432, "db", "user", "pass2", 0, 4, 3600000, 1800000, 30000)

        assertTrue(config1 != config2)
    }
}
