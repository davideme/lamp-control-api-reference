package com.lampcontrol.database

import kotlin.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DatabaseConfigTest {
    @ParameterizedTest
    @CsvSource(
        "db.example.com, 5433, mydb, jdbc:postgresql://db.example.com:5433/mydb",
        "localhost, 5432, lamp_control, jdbc:postgresql://localhost:5432/lamp_control",
        "192.168.1.100, 5433, testdb, jdbc:postgresql://192.168.1.100:5433/testdb",
        "10.0.0.1, 5432, db, jdbc:postgresql://10.0.0.1:5432/db",
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

        assertEquals("192.168.1.100", config.host)
        assertEquals(5433, config.port)
        assertEquals("testdb", config.database)
        assertEquals("testuser", config.user)
        assertEquals("testpass", config.password)
        assertEquals(5, config.poolMin)
        assertEquals(20, config.poolMax)
    }

    @Test
    fun `fromEnv does not throw when no environment variables are set`() {
        // Verifies the method executes without exceptions
        DatabaseConfig.fromEnv()
    }
}
