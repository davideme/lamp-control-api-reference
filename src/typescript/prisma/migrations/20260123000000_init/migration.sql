-- CreateTable
CREATE TABLE "lamps" (
    "id" UUID NOT NULL,
    "is_on" BOOLEAN NOT NULL,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "lamps_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "idx_lamps_is_on" ON "lamps"("is_on");

-- CreateIndex
CREATE INDEX "idx_lamps_created_at" ON "lamps"("created_at");

-- CreateIndex
CREATE INDEX "idx_lamps_deleted_at" ON "lamps"("deleted_at");
