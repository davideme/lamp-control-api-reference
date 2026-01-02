# ADR 007: PostgreSQL Storage with SQLAlchemy 2.0

## Status

Accepted

## Context

The Python implementation of the Lamp Control API currently uses an in-memory repository for data persistence. While suitable for development and testing, production deployments require durable, ACID-compliant data storage with async support, type safety, and efficient connection pooling optimized for FastAPI's async architecture.

### Current State

- **Framework**: FastAPI 0.115.12 with async/await
- **Architecture**: `LampRepository` class with in-memory dictionary storage
- **Storage**: In-memory dict with no persistence
- **Dependencies**: FastAPI, Pydantic, pytest

### Requirements

1. **Async-First**: Native async/await support for FastAPI compatibility
2. **Type Safety**: Type hints compatible with mypy/Pyright
3. **Performance**: High-performance async PostgreSQL driver
4. **Pydantic Integration**: Seamless integration with FastAPI/Pydantic models
5. **Schema Compatibility**: Use existing PostgreSQL schema at `database/sql/postgresql/schema.sql`
6. **Testing**: Integration tests with real PostgreSQL instances

### Technology Landscape (2025-2026)

**SQLAlchemy 2.0 + asyncpg**
- Industry standard Python ORM (96k+ GitHub stars)
- SQLAlchemy 2.0 (2023): Complete async rewrite, type hints
- asyncpg: Fastest PostgreSQL driver for Python (10x faster than psycopg2)
- Excellent FastAPI integration
- Type-safe with modern Python type hints
- Alembic for migrations

**Tortoise ORM**
- Django-like ORM for async
- Pydantic integration
- Smaller ecosystem (5k stars)
- Less mature than SQLAlchemy

**psycopg3**
- PostgreSQL-specific driver
- Async support
- No ORM (manual mapping required)
- Good for raw SQL control

## Decision

We will implement **SQLAlchemy 2.0 with asyncpg** as the PostgreSQL data access layer for the Python Lamp Control API implementation.

### Architecture

```
FastAPI Routes → LampService → LampRepository → SQLAlchemy 2.0 → asyncpg → PostgreSQL
```

### Core Components

#### 1. **Database Configuration**

```python
# src/database/config.py
from sqlalchemy.ext.asyncio import AsyncEngine, create_async_engine, async_sessionmaker
from sqlalchemy.pool import NullPool
import os

class DatabaseConfig:
    def __init__(self):
        self.database_url = os.getenv(
            "DATABASE_URL",
            "postgresql+asyncpg://lampuser:lamppass@localhost:5432/lampcontrol"
        )
        self.pool_size = int(os.getenv("DB_POOL_SIZE", "20"))
        self.max_overflow = int(os.getenv("DB_POOL_MAX_OVERFLOW", "10"))
        self.pool_pre_ping = True
        self.echo = os.getenv("SQL_ECHO", "false").lower() == "true"

def create_engine(config: DatabaseConfig) -> AsyncEngine:
    """Create async SQLAlchemy engine with connection pooling."""
    return create_async_engine(
        config.database_url,
        echo=config.echo,
        pool_size=config.pool_size,
        max_overflow=config.max_overflow,
        pool_pre_ping=config.pool_pre_ping,
        pool_recycle=3600,  # Recycle connections after 1 hour
    )

# Global engine and session factory
config = DatabaseConfig()
engine = create_engine(config)
AsyncSessionLocal = async_sessionmaker(
    engine,
    expire_on_commit=False,
    class_=AsyncSession
)

async def get_db() -> AsyncSession:
    """Dependency for FastAPI routes."""
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
```

#### 2. **Entity Model (SQLAlchemy 2.0)**

```python
# src/database/models.py
from sqlalchemy import Boolean, DateTime, Index
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from sqlalchemy.sql import func
from datetime import datetime
from typing import Optional
import uuid

class Base(DeclarativeBase):
    """Base class for all database models."""
    pass

class LampEntity(Base):
    """Lamp entity matching PostgreSQL schema."""
    
    __tablename__ = "lamps"
    
    # Columns with type annotations (SQLAlchemy 2.0 style)
    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )
    is_on: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now()
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
        onupdate=func.now()
    )
    deleted_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
        default=None
    )
    
    # Indexes
    __table_args__ = (
        Index("idx_lamps_is_on", "is_on"),
        Index("idx_lamps_created_at", "created_at"),
        Index("idx_lamps_deleted_at", "deleted_at"),
    )
    
    def __repr__(self) -> str:
        return f"<Lamp(id={self.id}, is_on={self.is_on})>"
```

#### 3. **Pydantic Models**

```python
# src/models/lamp.py
from pydantic import BaseModel, Field, ConfigDict
from datetime import datetime
from typing import Optional
import uuid

class LampBase(BaseModel):
    """Base Lamp schema."""
    is_on: bool = Field(description="Lamp status (true = ON, false = OFF)")

class LampCreate(LampBase):
    """Schema for creating a lamp."""
    pass

class LampUpdate(LampBase):
    """Schema for updating a lamp."""
    pass

class LampResponse(LampBase):
    """Schema for lamp response."""
    id: uuid.UUID
    created_at: datetime
    updated_at: datetime
    deleted_at: Optional[datetime] = None
    
    model_config = ConfigDict(from_attributes=True)
```

#### 4. **Repository Implementation**

```python
# src/repositories/lamp_repository.py
from sqlalchemy import select, update, and_
from sqlalchemy.ext.asyncio import AsyncSession
from src.database.models import LampEntity
from src.models.lamp import LampCreate, LampUpdate, LampResponse
from typing import List, Optional
from datetime import datetime
import uuid

class LampRepository:
    """Repository for lamp database operations."""
    
    def __init__(self, session: AsyncSession):
        self.session = session
    
    async def create(self, lamp_create: LampCreate) -> LampEntity:
        """Create a new lamp."""
        lamp = LampEntity(
            id=uuid.uuid4(),
            is_on=lamp_create.is_on,
            created_at=datetime.utcnow(),
            updated_at=datetime.utcnow()
        )
        self.session.add(lamp)
        await self.session.flush()  # Get the ID without committing
        await self.session.refresh(lamp)  # Refresh to get server defaults
        return lamp
    
    async def get_by_id(self, lamp_id: uuid.UUID) -> Optional[LampEntity]:
        """Get lamp by ID (excluding soft-deleted)."""
        query = select(LampEntity).where(
            and_(
                LampEntity.id == lamp_id,
                LampEntity.deleted_at.is_(None)
            )
        )
        result = await self.session.execute(query)
        return result.scalar_one_or_none()
    
    async def get_all(
        self,
        offset: int = 0,
        limit: int = 100
    ) -> List[LampEntity]:
        """Get all active lamps with pagination."""
        query = (
            select(LampEntity)
            .where(LampEntity.deleted_at.is_(None))
            .order_by(LampEntity.created_at.asc())
            .offset(offset)
            .limit(limit)
        )
        result = await self.session.execute(query)
        return list(result.scalars().all())
    
    async def update(
        self,
        lamp_id: uuid.UUID,
        lamp_update: LampUpdate
    ) -> Optional[LampEntity]:
        """Update lamp status."""
        # First check if lamp exists and is not deleted
        lamp = await self.get_by_id(lamp_id)
        if not lamp:
            return None
        
        # Update fields
        lamp.is_on = lamp_update.is_on
        lamp.updated_at = datetime.utcnow()
        
        await self.session.flush()
        await self.session.refresh(lamp)
        return lamp
    
    async def delete(self, lamp_id: uuid.UUID) -> bool:
        """Soft delete a lamp."""
        query = (
            update(LampEntity)
            .where(
                and_(
                    LampEntity.id == lamp_id,
                    LampEntity.deleted_at.is_(None)
                )
            )
            .values(deleted_at=datetime.utcnow())
        )
        result = await self.session.execute(query)
        await self.session.flush()
        return result.rowcount > 0
    
    async def count(self) -> int:
        """Count all active lamps."""
        query = select(func.count(LampEntity.id)).where(
            LampEntity.deleted_at.is_(None)
        )
        result = await self.session.execute(query)
        return result.scalar_one()
```

#### 5. **FastAPI Routes**

```python
# src/api/routes/lamps.py
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from src.database.config import get_db
from src.repositories.lamp_repository import LampRepository
from src.models.lamp import LampCreate, LampUpdate, LampResponse
from typing import List
import uuid

router = APIRouter(prefix="/lamps", tags=["lamps"])

def get_repository(session: AsyncSession = Depends(get_db)) -> LampRepository:
    """Dependency to get repository instance."""
    return LampRepository(session)

@router.post("/", response_model=LampResponse, status_code=201)
async def create_lamp(
    lamp: LampCreate,
    repo: LampRepository = Depends(get_repository)
) -> LampResponse:
    """Create a new lamp."""
    entity = await repo.create(lamp)
    return LampResponse.model_validate(entity)

@router.get("/", response_model=List[LampResponse])
async def list_lamps(
    offset: int = Query(0, ge=0, description="Number of records to skip"),
    limit: int = Query(100, ge=1, le=1000, description="Number of records to return"),
    repo: LampRepository = Depends(get_repository)
) -> List[LampResponse]:
    """Get all active lamps with pagination."""
    entities = await repo.get_all(offset, limit)
    return [LampResponse.model_validate(e) for e in entities]

@router.get("/{lamp_id}", response_model=LampResponse)
async def get_lamp(
    lamp_id: uuid.UUID,
    repo: LampRepository = Depends(get_repository)
) -> LampResponse:
    """Get a lamp by ID."""
    entity = await repo.get_by_id(lamp_id)
    if not entity:
        raise HTTPException(status_code=404, detail="Lamp not found")
    return LampResponse.model_validate(entity)

@router.put("/{lamp_id}", response_model=LampResponse)
async def update_lamp(
    lamp_id: uuid.UUID,
    lamp: LampUpdate,
    repo: LampRepository = Depends(get_repository)
) -> LampResponse:
    """Update a lamp's status."""
    entity = await repo.update(lamp_id, lamp)
    if not entity:
        raise HTTPException(status_code=404, detail="Lamp not found")
    return LampResponse.model_validate(entity)

@router.delete("/{lamp_id}", status_code=204)
async def delete_lamp(
    lamp_id: uuid.UUID,
    repo: LampRepository = Depends(get_repository)
) -> None:
    """Soft delete a lamp."""
    deleted = await repo.delete(lamp_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Lamp not found")
```

#### 6. **FastAPI Application**

```python
# src/main.py
from fastapi import FastAPI
from src.api.routes import lamps
from src.database.config import engine
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Handle startup and shutdown events."""
    # Startup
    yield
    # Shutdown
    await engine.dispose()

app = FastAPI(
    title="Lamp Control API",
    version="1.0.0",
    lifespan=lifespan
)

# Include routers
app.include_router(lamps.router)

@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

### Configuration

#### **Environment Variables**

```bash
# Database connection
DATABASE_URL=postgresql+asyncpg://lampuser:lamppass@localhost:5432/lampcontrol

# Connection pool
DB_POOL_SIZE=20
DB_POOL_MAX_OVERFLOW=10

# SQL logging (development only)
SQL_ECHO=false
```

#### **.env file**

```env
DATABASE_URL=postgresql+asyncpg://localhost:5432/lampcontrol
DB_USER=lampuser
DB_PASSWORD=lamppass
DB_POOL_SIZE=20
DB_POOL_MAX_OVERFLOW=10
```

### Dependencies

#### **pyproject.toml** (Poetry)

```toml
[tool.poetry]
name = "lamp-control-api"
version = "1.0.0"
description = "Lamp Control API with PostgreSQL"

[tool.poetry.dependencies]
python = "^3.11"
fastapi = "^0.115.0"
uvicorn = {extras = ["standard"], version = "^0.27.0"}
sqlalchemy = {extras = ["asyncio"], version = "^2.0.25"}
asyncpg = "^0.29.0"
pydantic = "^2.5.0"
pydantic-settings = "^2.1.0"
alembic = "^1.13.1"

[tool.poetry.group.dev.dependencies]
pytest = "^7.4.4"
pytest-asyncio = "^0.23.3"
httpx = "^0.26.0"
testcontainers = {extras = ["postgres"], version = "^3.7.1"}
mypy = "^1.8.0"
ruff = "^0.1.13"

[build-system]
requires = ["poetry-core"]
build-backend = "poetry.core.masonry.api"
```

### Migration Strategy with Alembic

#### **Initialize Alembic**

```bash
# Install alembic
poetry add alembic

# Initialize
alembic init alembic

# Configure alembic.ini
# Edit: sqlalchemy.url = postgresql+asyncpg://localhost/lampcontrol
```

#### **alembic/env.py** (Async Support)

```python
from logging.config import fileConfig
from sqlalchemy import pool
from sqlalchemy.engine import Connection
from sqlalchemy.ext.asyncio import async_engine_from_config
from alembic import context
from src.database.models import Base
import asyncio

config = context.config
fileConfig(config.config_file_name)

target_metadata = Base.metadata

def run_migrations_offline() -> None:
    """Run migrations in 'offline' mode."""
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )
    
    with context.begin_transaction():
        context.run_migrations()

def do_run_migrations(connection: Connection) -> None:
    context.configure(connection=connection, target_metadata=target_metadata)
    
    with context.begin_transaction():
        context.run_migrations()

async def run_async_migrations() -> None:
    """Run migrations in 'online' mode with async support."""
    connectable = async_engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )
    
    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)
    
    await connectable.dispose()

def run_migrations_online() -> None:
    """Run migrations in 'online' mode."""
    asyncio.run(run_async_migrations())

if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
```

#### **Create Initial Migration**

```bash
# Create migration from existing schema
alembic revision --autogenerate -m "Initial schema"

# Or create manual migration from existing SQL
cat << 'EOF' > alembic/versions/001_initial_schema.py
"""Initial schema

Revision ID: 001
"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

def upgrade() -> None:
    op.execute('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"')
    
    op.create_table(
        'lamps',
        sa.Column('id', postgresql.UUID(as_uuid=True), primary_key=True),
        sa.Column('is_on', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.text('CURRENT_TIMESTAMP')),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.text('CURRENT_TIMESTAMP')),
        sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    )
    
    op.create_index('idx_lamps_is_on', 'lamps', ['is_on'])
    op.create_index('idx_lamps_created_at', 'lamps', ['created_at'])
    op.create_index('idx_lamps_deleted_at', 'lamps', ['deleted_at'])

def downgrade() -> None:
    op.drop_table('lamps')
EOF

# Apply migration
alembic upgrade head
```

### Testing Strategy

#### **Integration Test with Testcontainers**

```python
# tests/test_lamp_repository.py
import pytest
import uuid
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from testcontainers.postgres import PostgresContainer
from src.database.models import Base, LampEntity
from src.repositories.lamp_repository import LampRepository
from src.models.lamp import LampCreate, LampUpdate

@pytest.fixture(scope="session")
def postgres_container():
    """Start PostgreSQL container for tests."""
    with PostgresContainer("postgres:16-alpine") as postgres:
        yield postgres

@pytest.fixture(scope="session")
async def engine(postgres_container):
    """Create async engine for tests."""
    database_url = postgres_container.get_connection_url().replace(
        "psycopg2", "asyncpg"
    )
    engine = create_async_engine(database_url, echo=True)
    
    # Create tables
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
    yield engine
    
    await engine.dispose()

@pytest.fixture
async def session(engine):
    """Create async session for each test."""
    async_session = async_sessionmaker(
        engine, class_=AsyncSession, expire_on_commit=False
    )
    
    async with async_session() as session:
        yield session
        await session.rollback()

@pytest.fixture
def repository(session):
    """Create repository instance."""
    return LampRepository(session)

@pytest.mark.asyncio
async def test_create_lamp(repository, session):
    """Test creating a lamp."""
    # Arrange
    lamp_create = LampCreate(is_on=True)
    
    # Act
    lamp = await repository.create(lamp_create)
    await session.commit()
    
    # Assert
    assert lamp.id is not None
    assert lamp.is_on is True
    assert lamp.created_at is not None

@pytest.mark.asyncio
async def test_get_lamp_by_id(repository, session):
    """Test retrieving a lamp by ID."""
    # Arrange
    lamp_create = LampCreate(is_on=False)
    created = await repository.create(lamp_create)
    await session.commit()
    
    # Act
    lamp = await repository.get_by_id(created.id)
    
    # Assert
    assert lamp is not None
    assert lamp.id == created.id
    assert lamp.is_on is False

@pytest.mark.asyncio
async def test_update_lamp(repository, session):
    """Test updating a lamp."""
    # Arrange
    lamp_create = LampCreate(is_on=False)
    created = await repository.create(lamp_create)
    await session.commit()
    
    # Act
    lamp_update = LampUpdate(is_on=True)
    updated = await repository.update(created.id, lamp_update)
    await session.commit()
    
    # Assert
    assert updated is not None
    assert updated.is_on is True

@pytest.mark.asyncio
async def test_soft_delete_lamp(repository, session):
    """Test soft deleting a lamp."""
    # Arrange
    lamp_create = LampCreate(is_on=True)
    created = await repository.create(lamp_create)
    await session.commit()
    
    # Act
    deleted = await repository.delete(created.id)
    await session.commit()
    
    # Assert
    assert deleted is True
    lamp = await repository.get_by_id(created.id)
    assert lamp is None  # Soft deleted, so not found

@pytest.mark.asyncio
async def test_get_all_lamps_with_pagination(repository, session):
    """Test getting all lamps with pagination."""
    # Arrange
    for i in range(5):
        await repository.create(LampCreate(is_on=i % 2 == 0))
    await session.commit()
    
    # Act
    lamps = await repository.get_all(offset=0, limit=3)
    
    # Assert
    assert len(lamps) == 3
```

### Performance Optimizations

#### **1. Connection Pooling**

```python
# Optimize pool size based on load
engine = create_async_engine(
    database_url,
    pool_size=20,  # Steady-state connections
    max_overflow=10,  # Burst capacity
    pool_pre_ping=True,  # Validate connections
    pool_recycle=3600,  # Recycle after 1 hour
)
```

#### **2. Bulk Operations**

```python
async def create_batch(self, lamps: List[LampCreate]) -> List[LampEntity]:
    """Create multiple lamps efficiently."""
    entities = [
        LampEntity(id=uuid.uuid4(), is_on=lamp.is_on)
        for lamp in lamps
    ]
    self.session.add_all(entities)
    await self.session.flush()
    return entities
```

#### **3. Query Optimization**

```python
# Use select with specific columns for large datasets
query = select(LampEntity.id, LampEntity.is_on).where(...)

# Use load options for relationships
query = select(LampEntity).options(selectinload(LampEntity.related))
```

## Rationale

### Why SQLAlchemy 2.0?

1. **Industry Standard**: Most popular Python ORM (96k stars)
2. **Async-First**: Complete async rewrite in 2.0, perfect for FastAPI
3. **Type Safety**: Modern type hints, mypy/Pyright compatible
4. **Mature**: 15+ years of development, battle-tested
5. **Flexibility**: ORM or Core (query builder), choose your level
6. **Documentation**: Extensive docs, large community

### Why asyncpg?

1. **Performance**: 10x faster than psycopg2 for PostgreSQL
2. **Native Async**: Built for async/await from the ground up
3. **Type Support**: Excellent PostgreSQL type support (UUID, JSONB, arrays)
4. **Production-Proven**: Used by major companies

### Why Not Tortoise ORM?

- **Less Mature**: Younger project (2018 vs 2005)
- **Smaller Ecosystem**: 5k stars vs 96k
- **Trade-off**: Simpler API but less powerful than SQLAlchemy

### Why Not psycopg3?

- **No ORM**: Manual mapping, more boilerplate
- **Use Case**: Better for stored procedures or raw SQL
- **Decision**: SQLAlchemy provides better abstraction

## Consequences

### Positive

- ✅ **Async Performance**: Non-blocking I/O, perfect for FastAPI
- ✅ **Type Safety**: Full type hint support, mypy validation
- ✅ **Pydantic Integration**: Seamless with FastAPI models
- ✅ **Production-Ready**: Battle-tested, used by thousands of companies
- ✅ **Testability**: Excellent Testcontainers support
- ✅ **Migration Tools**: Alembic for schema management

### Negative

- ❌ **Learning Curve**: SQLAlchemy 2.0 async patterns differ from 1.x
- ❌ **Verbosity**: More code than simpler ORMs like Tortoise
- ❌ **N+1 Queries**: Lazy loading can cause performance issues
- ❌ **Async Complexity**: Must understand async/await thoroughly

### Neutral

- 🔄 **Migration Strategy**: Must learn Alembic
- 🔄 **Connection Pool Tuning**: Requires production profiling
- 🔄 **Query Optimization**: Complex queries need careful design

## Implementation Checklist

- [ ] Add SQLAlchemy and asyncpg dependencies
- [ ] Create `Base` and `LampEntity` models with type hints
- [ ] Implement `DatabaseConfig` and async engine
- [ ] Create `LampRepository` with async methods
- [ ] Define Pydantic models for API
- [ ] Implement FastAPI routes with dependency injection
- [ ] Configure Alembic for migrations
- [ ] Create initial migration from existing schema
- [ ] Write integration tests with Testcontainers
- [ ] Update README with database setup instructions
- [ ] Add environment variable documentation

## References

- [SQLAlchemy 2.0 Documentation](https://docs.sqlalchemy.org/en/20/)
- [asyncpg Documentation](https://magicstack.github.io/asyncpg/)
- [Alembic Documentation](https://alembic.sqlalchemy.org/)
- [FastAPI with SQLAlchemy](https://fastapi.tiangolo.com/tutorial/sql-databases/)
- [PostgreSQL Schema: database/sql/postgresql/schema.sql](../../../database/sql/postgresql/schema.sql)
- [Testcontainers Python](https://testcontainers-python.readthedocs.io/)

## Related ADRs

- [ADR 001: Python Version Selection](001-python-version-selection.md)
- [ADR 002: FastAPI Framework](002-fastapi-framework.md)
- [Root ADR 005: PostgreSQL Storage Support](../../../docs/adr/005-postgresql-storage-support.md)
