"""Initial schema for lamps table

Revision ID: 001
Revises:
Create Date: 2026-01-10

This migration references the existing PostgreSQL schema defined in
database/sql/postgresql/schema.sql. For initial database setup, apply
that SQL file directly using:

    psql -U lamp_user -d lamp_control -f database/sql/postgresql/schema.sql

Or use the provided docker-compose setup which handles this automatically.

This Alembic migration is provided for reference and to establish the initial
state for future migrations.
"""

import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

from alembic import op

# revision identifiers, used by Alembic.
revision = "001"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    """Apply the initial schema.

    Note: It's recommended to apply the schema.sql file directly instead
    of running this migration, as the SQL file contains additional database
    features like triggers that are better managed there.
    """
    # Create lamps table
    op.create_table(
        "lamps",
        sa.Column(
            "id",
            postgresql.UUID(as_uuid=True),
            primary_key=True,
            server_default=sa.text("GEN_RANDOM_UUID()"),
        ),
        sa.Column("is_on", sa.Boolean(), nullable=False, server_default="false"),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("CURRENT_TIMESTAMP"),
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("CURRENT_TIMESTAMP"),
        ),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
    )

    # Create partial indexes for active rows
    op.execute("""
        CREATE INDEX IF NOT EXISTS idx_lamps_active_created_at_id
        ON lamps (created_at ASC, id ASC)
        WHERE deleted_at IS NULL
    """)
    op.execute("""
        CREATE INDEX IF NOT EXISTS idx_lamps_active_is_on
        ON lamps (is_on)
        WHERE deleted_at IS NULL
    """)

    # Create trigger function for updated_at
    op.execute("""
        CREATE OR REPLACE FUNCTION update_updated_at_column()
        RETURNS TRIGGER AS $$
        BEGIN
            NEW.updated_at = CURRENT_TIMESTAMP;
            RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;
    """)

    # Create trigger
    op.execute("""
        CREATE TRIGGER update_lamps_updated_at
        BEFORE UPDATE ON lamps
        FOR EACH ROW
        EXECUTE FUNCTION update_updated_at_column();
    """)


def downgrade() -> None:
    """Remove the initial schema."""
    op.execute("DROP TRIGGER IF EXISTS update_lamps_updated_at ON lamps")
    op.execute("DROP FUNCTION IF EXISTS update_updated_at_column()")
    op.execute("DROP INDEX IF EXISTS idx_lamps_active_is_on")
    op.execute("DROP INDEX IF EXISTS idx_lamps_active_created_at_id")
    op.drop_table("lamps")
