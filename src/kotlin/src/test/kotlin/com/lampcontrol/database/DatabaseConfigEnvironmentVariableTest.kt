package com.lampcontrol.database

import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.*
import kotlin.test.*

/**
 * Tests for DatabaseConfig.fromEnv() using JUnit Pioneer to set environment variables.
 * These tests exercise the actual environment variable parsing code paths.
 */
class DatabaseConfigEnvironmentVariableTest {
    companion object {
        private const val CLOUD_SQL_SOCKET_DATABASE_URL =
            "postgresql://postgres:A3tN1%7DgX%7B5%7Be9ZaL@/lamp-control" +
                "?host=/cloudsql/lamp-control-469416:europe-west1:lamp-control-db&connect_timeout=5"
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://testuser:testpass@localhost:5432/testdb")
    fun `fromEnv parses DATABASE_URL correctly`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("testuser", config.user)
        assertEquals("testpass", config.password)
        assertEquals("localhost", config.host)
        assertEquals(5432, config.port)
        assertEquals("testdb", config.database)
        assertEquals(0, config.poolMin) // Default
        assertEquals(4, config.poolMax) // Default
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgres://myuser:mypass@db.example.com:5433/production")
    fun `fromEnv parses DATABASE_URL with postgres scheme`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("myuser", config.user)
        assertEquals("mypass", config.password)
        assertEquals("db.example.com", config.host)
        assertEquals(5433, config.port)
        assertEquals("production", config.database)
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://admin:secret123@10.0.0.1:5434/lamp_control")
    @SetEnvironmentVariable(key = "DB_POOL_MIN_SIZE", value = "5")
    @SetEnvironmentVariable(key = "DB_POOL_MAX_SIZE", value = "20")
    fun `fromEnv respects pool size env vars with DATABASE_URL`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("admin", config.user)
        assertEquals("secret123", config.password)
        assertEquals("10.0.0.1", config.host)
        assertEquals(5434, config.port)
        assertEquals("lamp_control", config.database)
        assertEquals(5, config.poolMin)
        assertEquals(20, config.poolMax)
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "invalid-url-format")
    fun `fromEnv throws on invalid DATABASE_URL format`() {
        assertFailsWith<IllegalArgumentException> {
            DatabaseConfig.fromEnv()
        }
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "mysql://user:pass@host:3306/db")
    fun `fromEnv throws on non-PostgreSQL DATABASE_URL`() {
        assertFailsWith<IllegalArgumentException> {
            DatabaseConfig.fromEnv()
        }
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "myhost")
    @SetEnvironmentVariable(key = "DB_USER", value = "myuser")
    fun `fromEnv uses individual env vars when DATABASE_URL not set`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("myhost", config.host)
        assertEquals("myuser", config.user)
        assertEquals(5432, config.port) // Default
        assertEquals("lamp_control", config.database) // Default
        assertEquals("", config.password) // Default
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "testhost")
    @SetEnvironmentVariable(key = "DB_PORT", value = "5433")
    @SetEnvironmentVariable(key = "DB_NAME", value = "testdb")
    @SetEnvironmentVariable(key = "DB_USER", value = "testuser")
    @SetEnvironmentVariable(key = "DB_PASSWORD", value = "testpass")
    @SetEnvironmentVariable(key = "DB_POOL_MIN_SIZE", value = "2")
    @SetEnvironmentVariable(key = "DB_POOL_MAX_SIZE", value = "10")
    fun `fromEnv uses all individual env vars`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("testhost", config.host)
        assertEquals(5433, config.port)
        assertEquals("testdb", config.database)
        assertEquals("testuser", config.user)
        assertEquals("testpass", config.password)
        assertEquals(2, config.poolMin)
        assertEquals(10, config.poolMax)
    }

    @Test
    @SetEnvironmentVariable(key = "DB_NAME", value = "my_database")
    fun `fromEnv considers PostgreSQL configured when DB_NAME is set`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("localhost", config.host) // Default
        assertEquals(5432, config.port) // Default
        assertEquals("my_database", config.database)
        assertEquals("lamp_user", config.user) // Default
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "dbhost")
    fun `fromEnv returns null when only DB_HOST is set without DB_USER`() {
        val config = DatabaseConfig.fromEnv()

        // DB_HOST alone doesn't indicate PostgreSQL is configured
        // Need both DB_HOST and DB_USER
        assertNull(config)
    }

    @Test
    @SetEnvironmentVariable(key = "DB_USER", value = "dbuser")
    fun `fromEnv returns null when only DB_USER is set without DB_HOST`() {
        val config = DatabaseConfig.fromEnv()

        // DB_USER alone doesn't indicate PostgreSQL is configured
        // Need both DB_HOST and DB_USER
        assertNull(config)
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "host1")
    @SetEnvironmentVariable(key = "DB_USER", value = "user1")
    @SetEnvironmentVariable(key = "DB_PORT", value = "invalid")
    fun `fromEnv uses default port when DB_PORT is invalid`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(5432, config.port) // Falls back to default
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "host1")
    @SetEnvironmentVariable(key = "DB_USER", value = "user1")
    @SetEnvironmentVariable(key = "DB_POOL_MIN_SIZE", value = "not-a-number")
    fun `fromEnv uses default pool min when DB_POOL_MIN_SIZE is invalid`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(0, config.poolMin) // Falls back to default
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "host1")
    @SetEnvironmentVariable(key = "DB_USER", value = "user1")
    @SetEnvironmentVariable(key = "DB_POOL_MAX_SIZE", value = "not-a-number")
    fun `fromEnv uses default pool max when DB_POOL_MAX_SIZE is invalid`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(4, config.poolMax) // Falls back to default
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://user1:pass1@host1:5432/db1")
    @SetEnvironmentVariable(key = "DB_HOST", value = "host2")
    @SetEnvironmentVariable(key = "DB_USER", value = "user2")
    fun `fromEnv prefers DATABASE_URL over individual env vars`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        // Should use DATABASE_URL values, not DB_HOST/DB_USER
        assertEquals("host1", config.host)
        assertEquals("user1", config.user)
        assertEquals("pass1", config.password)
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://u:p@127.0.0.1:5432/db")
    fun `fromEnv handles IPv4 addresses in DATABASE_URL`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("127.0.0.1", config.host)
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://user:pass@postgres-server:5432/db")
    fun `fromEnv handles hostname in DATABASE_URL`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("postgres-server", config.host)
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://user:password123!@host:5432/db_name_2024")
    fun `fromEnv handles special characters in password and database name`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("password123!", config.password)
        assertEquals("db_name_2024", config.database)
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://user.name:pass@host:5432/db")
    fun `fromEnv handles special characters in username`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("user.name", config.user)
    }

    @Test
    @SetEnvironmentVariable(
        key = "DATABASE_URL",
        value = CLOUD_SQL_SOCKET_DATABASE_URL,
    )
    fun `fromEnv parses Cloud SQL Unix socket DATABASE_URL`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("postgres", config.user)
        assertEquals("A3tN1}gX{5{e9ZaL", config.password)
        assertEquals("/cloudsql/lamp-control-469416:europe-west1:lamp-control-db", config.host)
        assertEquals(5432, config.port)
        assertEquals("lamp-control", config.database)
        assertEquals(
            "jdbc:postgresql://localhost/lamp-control?host=/cloudsql/lamp-control-469416:europe-west1:lamp-control-db&connect_timeout=5",
            config.connectionString(),
        )
    }

    @Test
    @SetEnvironmentVariable(
        key = "DATABASE_URL",
        value = "postgresql://postgres:secret@/lamp-control?unixSocketPath=/cloudsql/project:region:instance",
    )
    fun `fromEnv parses unixSocketPath query parameter`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("/cloudsql/project:region:instance", config.host)
        assertEquals(
            "jdbc:postgresql://localhost/lamp-control?unixSocketPath=/cloudsql/project:region:instance",
            config.connectionString(),
        )
    }

    @Test
    @SetEnvironmentVariable(
        key = "DATABASE_URL",
        value = "postgresql://user:my%21pass%3A123@db.example.com:5432/app",
    )
    fun `fromEnv decodes percent encoded password`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("my!pass:123", config.password)
    }

    @Test
    @ClearEnvironmentVariable(key = "DATABASE_URL")
    @ClearEnvironmentVariable(key = "DB_HOST")
    @ClearEnvironmentVariable(key = "DB_NAME")
    @ClearEnvironmentVariable(key = "DB_USER")
    fun `fromEnv returns null when no PostgreSQL config is present`() {
        val config = DatabaseConfig.fromEnv()

        assertNull(config)
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "192.168.1.100")
    @SetEnvironmentVariable(key = "DB_PORT", value = "15432")
    @SetEnvironmentVariable(key = "DB_USER", value = "admin")
    fun `fromEnv handles non-standard port via individual env vars`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals("192.168.1.100", config.host)
        assertEquals(15432, config.port)
        assertEquals("admin", config.user)
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "")
    @SetEnvironmentVariable(key = "DB_USER", value = "user")
    fun `fromEnv returns null when DB_HOST is empty string`() {
        val config = DatabaseConfig.fromEnv()

        // Empty string should be treated as not set
        assertNull(config)
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "host")
    @SetEnvironmentVariable(key = "DB_USER", value = "")
    fun `fromEnv returns null when DB_USER is empty string`() {
        val config = DatabaseConfig.fromEnv()

        // Empty string should be treated as not set
        assertNull(config)
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "")
    @SetEnvironmentVariable(key = "DB_NAME", value = "")
    fun `fromEnv returns null when all config values are empty strings`() {
        val config = DatabaseConfig.fromEnv()

        // All empty strings should be treated as not configured
        assertNull(config)
    }

    @Test
    @SetEnvironmentVariable(key = "DB_HOST", value = "host")
    @SetEnvironmentVariable(key = "DB_USER", value = "user")
    @SetEnvironmentVariable(key = "DB_MAX_LIFETIME_MS", value = "7200000")
    @SetEnvironmentVariable(key = "DB_IDLE_TIMEOUT_MS", value = "900000")
    @SetEnvironmentVariable(key = "DB_CONNECTION_TIMEOUT_MS", value = "60000")
    fun `fromEnv respects timeout env vars`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(7200000L, config.maxLifetimeMs) // 2 hours
        assertEquals(900000L, config.idleTimeoutMs) // 15 minutes
        assertEquals(60000L, config.connectionTimeoutMs) // 60 seconds
    }

    @Test
    @SetEnvironmentVariable(key = "DATABASE_URL", value = "postgresql://user:pass@localhost:5432/db")
    @SetEnvironmentVariable(key = "DB_MAX_LIFETIME_MS", value = "1800000")
    @SetEnvironmentVariable(key = "DB_IDLE_TIMEOUT_MS", value = "600000")
    @SetEnvironmentVariable(key = "DB_CONNECTION_TIMEOUT_MS", value = "20000")
    fun `fromEnv respects timeout env vars with DATABASE_URL`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(1800000L, config.maxLifetimeMs) // 30 minutes
        assertEquals(600000L, config.idleTimeoutMs) // 10 minutes
        assertEquals(20000L, config.connectionTimeoutMs) // 20 seconds
    }

    @Test
    @SetEnvironmentVariable(key = "DB_NAME", value = "testdb")
    @SetEnvironmentVariable(key = "DB_MAX_LIFETIME_MS", value = "invalid")
    fun `fromEnv uses default max lifetime when invalid`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(3600000L, config.maxLifetimeMs) // Falls back to 1 hour default
    }

    @Test
    @SetEnvironmentVariable(key = "DB_NAME", value = "testdb")
    @SetEnvironmentVariable(key = "DB_IDLE_TIMEOUT_MS", value = "invalid")
    fun `fromEnv uses default idle timeout when invalid`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(1800000L, config.idleTimeoutMs) // Falls back to 30 minutes default
    }

    @Test
    @SetEnvironmentVariable(key = "DB_NAME", value = "testdb")
    @SetEnvironmentVariable(key = "DB_CONNECTION_TIMEOUT_MS", value = "invalid")
    fun `fromEnv uses default connection timeout when invalid`() {
        val config = DatabaseConfig.fromEnv()

        assertNotNull(config)
        assertEquals(30000L, config.connectionTimeoutMs) // Falls back to 30 seconds default
    }
}
