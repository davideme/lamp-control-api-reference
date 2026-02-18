-- name: CreateLamp :one
INSERT INTO lamps (id, is_on, created_at, updated_at)
VALUES ($1, $2, $3, $4)
RETURNING id, is_on, created_at, updated_at, deleted_at;

-- name: GetLampByID :one
SELECT id, is_on, created_at, updated_at, deleted_at
FROM lamps
WHERE id = $1 AND deleted_at IS NULL;

-- name: ListLamps :many
SELECT id, is_on, created_at, updated_at, deleted_at
FROM lamps
WHERE deleted_at IS NULL
ORDER BY created_at ASC, id ASC
LIMIT $1 OFFSET $2;

-- name: CountLamps :one
SELECT COUNT(*) as count
FROM lamps
WHERE deleted_at IS NULL;

-- name: UpdateLamp :one
UPDATE lamps
SET is_on = $2
WHERE id = $1 AND deleted_at IS NULL
RETURNING id, is_on, created_at, updated_at, deleted_at;

-- name: DeleteLamp :execrows
UPDATE lamps
SET deleted_at = $2, updated_at = $2
WHERE id = $1 AND deleted_at IS NULL;

-- name: GetAllLampsForTest :many
SELECT id, is_on, created_at, updated_at, deleted_at
FROM lamps
WHERE deleted_at IS NULL

ORDER BY created_at ASC;
