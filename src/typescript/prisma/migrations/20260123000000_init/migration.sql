-- CreateTable
CREATE TABLE "lamps" (
    "id" UUID NOT NULL,
    "is_on" BOOLEAN NOT NULL,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "lamps_pkey" PRIMARY KEY ("id")
);

-- CreateIndex (partial index: active rows only)
CREATE INDEX "idx_lamps_active_created_at_id" ON "lamps"("created_at" ASC, "id" ASC) WHERE deleted_at IS NULL;

-- CreateIndex (partial index: active rows only)
CREATE INDEX "idx_lamps_active_is_on" ON "lamps"("is_on") WHERE deleted_at IS NULL;
