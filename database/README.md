# Database Setup Guide

This guide provides instructions for setting up the database for the Lamp Control API. The system supports both SQL (MySQL/PostgreSQL) and MongoDB implementations.

## Table of Contents

- [Common Features](#common-features)
- [MySQL Setup](#mysql-setup)
- [PostgreSQL Setup](#postgresql-setup)
- [MongoDB Setup](#mongodb-setup)

## Common Features

All database implementations share these common features:

- UUID-based primary keys
- Timestamps (created_at, updated_at)
- Soft delete support (deleted_at)
- Status tracking (ON/OFF)
- Indexed fields for optimal query performance

## MySQL Setup

1. Install MySQL (8.0 or later recommended)

2. Create the database and tables:
   ```bash
   mysql -u your_user -p < database/sql/mysql/schema.sql
   ```

3. Verify the setup:
   ```sql
   USE lamp_control;
   SHOW TABLES;
   DESCRIBE lamps;
   ```

### Key Features
- Uses `CHAR(36)` for UUID storage
- Automatic UUID generation via trigger
- ENUM type for status field
- Automatic timestamp management
- InnoDB engine for ACID compliance

## PostgreSQL Setup

1. Install PostgreSQL (13.0 or later recommended)

2. Create a new database:
   ```bash
   createdb lamp_control
   ```

3. Apply the schema:
   ```bash
   psql -d lamp_control -f database/sql/postgresql/schema.sql
   ```

4. Verify the setup:
   ```sql
   \dt
   \d lamps
   ```

### Key Features
- Native UUID type support
- Custom ENUM type for status
- Automatic timestamp management via trigger
- Full timezone support
- Rich indexing capabilities

## MongoDB Setup

1. Install MongoDB (5.0 or later recommended)

2. Create the database and collection:
   ```javascript
   use lamp_control
   
   // Copy and paste the collection creation script from database/mongodb/schema.md
   ```

3. Verify the setup:
   ```javascript
   db.lamps.getIndexes()
   ```

### Key Features
- Schema validation using JSON Schema
- Flexible document structure
- Native date type support
- Automatic indexing of _id field
- Rich query capabilities

## Development Guidelines

1. **Schema Changes**
   - Document all schema changes
   - Provide migration scripts
   - Update all language implementations

2. **Data Integrity**
   - Always use UUIDs for IDs
   - Maintain consistent timestamp formats
   - Implement proper soft delete handling

3. **Performance**
   - Use appropriate indexes
   - Monitor query performance
   - Optimize for common operations

4. **Testing**
   - Test with representative data volumes
   - Verify index effectiveness
   - Ensure proper error handling

## Troubleshooting

### Common Issues

1. **MySQL**
   - Character set issues: Ensure utf8mb4 is used
   - UUID generation: Verify trigger installation

2. **PostgreSQL**
   - Extension availability: Check uuid-ossp
   - Enum type creation: Verify type exists

3. **MongoDB**
   - Validation errors: Check document structure
   - Index creation: Verify index build completion

### Support

For issues or questions:
1. Check the troubleshooting guide
2. Review the schema documentation
3. Open an issue in the repository

## Migration Guide

When migrating between database systems:

1. Export data in a consistent format
2. Verify UUID compatibility
3. Check timestamp formats
4. Validate status values
5. Verify index creation

## Security Considerations

1. **Access Control**
   - Use dedicated service accounts
   - Implement least privilege access
   - Regularly rotate credentials

2. **Data Protection**
   - Enable TLS for connections
   - Implement backup strategies
   - Monitor access patterns

## Performance Monitoring

Monitor these key metrics:
1. Query response times
2. Index usage statistics
3. Connection pool usage
4. Storage utilization
5. Cache hit rates 