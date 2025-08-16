# ADR 002: Pagination Strategy

## Context

To handle large datasets efficiently and provide a better user experience, the API needs a consistent and scalable pagination strategy. This is particularly important for endpoints that return lists of resources.

## Decision

The API will use cursor-based pagination for all list endpoints. This approach provides better performance and flexibility compared to offset-based pagination, especially for datasets that are frequently updated.

### Implementation Details

1. **Query Parameters**:
   - `cursor`: A string representing the position in the dataset.
   - `pageSize`: An integer specifying the number of items to return (default: 25, min: 1, max: 100).

2. **Response Structure**:
   - `data`: An array of resources.
   - `nextCursor`: A string representing the cursor for the next page (nullable if no more data).
   - `hasMore`: A boolean indicating if more data is available.

3. **Endpoints**:
   - All list endpoints will support these parameters and response structure.

## Consequences

- **Pros**:
  - Efficient for large datasets.
  - Avoids issues with data consistency during pagination.
  - Flexible for various use cases.

- **Cons**:
  - Slightly more complex to implement compared to offset-based pagination.

## Status

Accepted
