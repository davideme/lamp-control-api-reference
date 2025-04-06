# ADR 001: Use Boolean for Lamp Status

## Status
Accepted

## Context
The lamp control system needs to track the state of each lamp (on/off). Initially, this was implemented using an ENUM type with values 'ON' and 'OFF'. However, this decision needed to be revisited for better efficiency and simplicity.

## Decision
We will use a boolean field (`is_on` in SQL, `isOn` in MongoDB) instead of an ENUM type for lamp status.

### Implementation Details
- MySQL: `is_on BOOLEAN NOT NULL DEFAULT FALSE`
- PostgreSQL: `is_on BOOLEAN NOT NULL DEFAULT FALSE`
- MongoDB: `isOn: boolean` with schema validation

## Rationale

### Advantages
1. **Storage Efficiency**
   - Boolean typically uses 1 bit vs. ENUM's multiple bytes
   - More efficient indexing
   - Smaller memory footprint

2. **Type Safety**
   - Native boolean type in most programming languages
   - No need for string comparison
   - Eliminates possibility of invalid states

3. **Query Performance**
   - Faster comparisons
   - Better index utilization
   - Simpler query optimization

4. **Code Clarity**
   - More intuitive in conditionals (`if (lamp.isOn)` vs `if (lamp.status === 'ON')`)
   - Clearer semantic meaning
   - Reduced chance of errors

5. **Analytics Benefits**
   - Easier aggregation queries
   - Better compatibility with analytics tools
   - Simpler boolean logic in reports

### Disadvantages
1. **Less Extensible**
   - Adding new states would require schema changes
   - Binary nature limits future state options

2. **Migration Effort**
   - Requires data migration for existing systems
   - Need to update application code

## Alternatives Considered

### 1. ENUM Type
```sql
ENUM('ON', 'OFF')
```
- More readable in raw database queries
- Easier to extend with new states
- Larger storage footprint
- String comparison overhead

### 2. Integer with Constants
```sql
TINYINT(1)
```
- Similar storage benefits
- Less semantic meaning
- Potential for invalid values

## Consequences

### Positive
- Improved performance
- Better type safety
- Cleaner code
- Smaller storage footprint

### Negative
- Less flexible for future state additions
- Migration needed for existing systems

## Related Decisions
- Database schema design
- API response format
- Query optimization strategies

## Notes
- Consider feature flags for transitioning existing systems
- Monitor performance metrics after implementation
- Document any migration scripts needed 