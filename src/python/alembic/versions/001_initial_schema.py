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
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = '001'
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    """Apply the initial schema.

    Note: It's recommended to apply the schema.sql file directly instead
    of running this migration, as the SQL file contains additional database
    features like triggers that are better managed there.
    """
    # Enable UUID extension
    op.execute('CREATE EXTENSION IF NOT EXISTS "uuid-ossp"')

    # Create lamps table
    op.create_table(
        'lamps',
        sa.Column('id', postgresql.UUID(as_uuid=True), primary_key=True, server_default=sa.text('UUID_GENERATE_V4()')),
        sa.Column('is_on', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('created_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.text('CURRENT_TIMESTAMP')),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=False, server_default=sa.text('CURRENT_TIMESTAMP')),
        sa.Column('deleted_at', sa.DateTime(timezone=True), nullable=True),
    )

    # Create indexes
    op.create_index('idx_lamps_is_on', 'lamps', ['is_on'])
    op.create_index('idx_lamps_created_at', 'lamps', ['created_at'])
    op.create_index('idx_lamps_deleted_at', 'lamps', ['deleted_at'])

    # Create trigger function for updated_at
    op.execute("""
        CREATE OR REPLACE FUNCTION UPDATE_UPDATED_AT_COLUMN()
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
        EXECUTE FUNCTION UPDATE_UPDATED_AT_COLUMN();
    """)


def downgrade() -> None:
    """Remove the initial schema."""
    op.execute('DROP TRIGGER IF EXISTS update_lamps_updated_at ON lamps')
    op.execute('DROP FUNCTION IF EXISTS UPDATE_UPDATED_AT_COLUMN()')
    op.drop_index('idx_lamps_deleted_at', table_name='lamps')
    op.drop_index('idx_lamps_created_at', table_name='lamps')
    op.drop_index('idx_lamps_is_on', table_name='lamps')
    op.drop_table('lamps')
    op.execute('DROP EXTENSION IF EXISTS "uuid-ossp"')
