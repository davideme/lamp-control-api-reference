-- CreateExtension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- CreateTable
CREATE TABLE "lamps" (
    "id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "is_on" BOOLEAN NOT NULL DEFAULT false,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "lamps_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "idx_lamps_is_on" ON "lamps"("is_on");

-- CreateIndex
CREATE INDEX "idx_lamps_created_at" ON "lamps"("created_at");

-- CreateIndex
CREATE INDEX "idx_lamps_deleted_at" ON "lamps"("deleted_at");

-- CreateFunction
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- CreateTrigger
CREATE TRIGGER update_lamps_updated_at
    BEFORE UPDATE ON "lamps"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
