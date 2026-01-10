"""Configuration management for the Lamp Control API."""

from pydantic_settings import BaseSettings, SettingsConfigDict


class DatabaseSettings(BaseSettings):
    """Database configuration settings loaded from environment variables.

    This class uses pydantic-settings to load configuration from environment
    variables or a .env file. All settings have sensible defaults for local
    development.
    """

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )

    # PostgreSQL connection settings
    db_host: str = "localhost"
    db_port: int = 5432
    db_name: str = "lamp_control"
    db_user: str = "lamp_user"
    db_password: str = "lamp_password"

    # Connection pool settings
    db_pool_min_size: int = 5
    db_pool_max_size: int = 20

    # Database connection URL - if set, PostgreSQL will be used
    # If not set, in-memory storage will be used
    database_url: str | None = None

    def use_postgres(self) -> bool:
        """Determine if PostgreSQL should be used based on database_url presence.

        Returns:
            True if database_url is configured, False otherwise (use in-memory).
        """
        return self.database_url is not None and self.database_url.strip() != ""

    def get_connection_string(self) -> str:
        """Build PostgreSQL connection string for asyncpg driver.

        Returns:
            Connection string in the format:
            postgresql+asyncpg://user:password@host:port/database

        Raises:
            ValueError: If database_url is not configured.
        """
        if self.database_url:
            return self.database_url

        # Fallback to building from individual parameters
        return (
            f"postgresql+asyncpg://{self.db_user}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}"
        )
