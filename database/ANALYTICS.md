# Analytics Guide

This document outlines the analytics capabilities built into the lamp control database schema and provides guidance on tracking key metrics.

## Key Metrics

### Operational Metrics

1. **Lamp State Changes**
   ```sql
   -- MySQL/PostgreSQL
   SELECT 
     DATE_TRUNC('hour', updated_at) as hour,
     COUNT(*) as total_changes
   FROM lamps
   WHERE deleted_at IS NULL
   GROUP BY DATE_TRUNC('hour', updated_at)
   ORDER BY hour;

   -- MongoDB
   db.lamps.aggregate([
     { $match: { deletedAt: null } },
     { $group: {
         _id: { $dateTrunc: { date: "$updatedAt", unit: "hour" } },
         total_changes: { $count: {} }
     }},
     { $sort: { "_id": 1 } }
   ])
   ```

2. **Active Lamps**
   ```sql
   -- MySQL/PostgreSQL
   SELECT 
     COUNT(*) as total_lamps,
     SUM(CASE WHEN is_on THEN 1 ELSE 0 END) as lamps_on,
     SUM(CASE WHEN NOT is_on THEN 1 ELSE 0 END) as lamps_off
   FROM lamps
   WHERE deleted_at IS NULL;

   -- MongoDB
   db.lamps.aggregate([
     { $match: { deletedAt: null } },
     { $group: {
         _id: null,
         total_lamps: { $count: {} },
         lamps_on: { $sum: { $cond: ["$isOn", 1, 0] } },
         lamps_off: { $sum: { $cond: ["$isOn", 0, 1] } }
     }}
   ])
   ```

### Time-Based Analysis

1. **Usage Patterns**
   ```sql
   -- MySQL/PostgreSQL
   SELECT 
     EXTRACT(HOUR FROM updated_at) as hour_of_day,
     COUNT(*) as changes,
     SUM(CASE WHEN is_on THEN 1 ELSE 0 END) as turned_on,
     SUM(CASE WHEN NOT is_on THEN 1 ELSE 0 END) as turned_off
   FROM lamps
   WHERE deleted_at IS NULL
   GROUP BY EXTRACT(HOUR FROM updated_at)
   ORDER BY hour_of_day;

   -- MongoDB
   db.lamps.aggregate([
     { $match: { deletedAt: null } },
     { $group: {
         _id: { $hour: "$updatedAt" },
         changes: { $count: {} },
         turned_on: { $sum: { $cond: ["$isOn", 1, 0] } },
         turned_off: { $sum: { $cond: ["$isOn", 0, 1] } }
     }},
     { $sort: { "_id": 1 } }
   ])
   ```

2. **Lifetime Analysis**
   ```sql
   -- MySQL/PostgreSQL
   SELECT 
     id,
     created_at,
     COALESCE(deleted_at, CURRENT_TIMESTAMP) as end_time,
     EXTRACT(EPOCH FROM COALESCE(deleted_at, CURRENT_TIMESTAMP) - created_at)/86400 as days_active
   FROM lamps
   ORDER BY days_active DESC;

   -- MongoDB
   db.lamps.aggregate([
     { $project: {
         created: "$createdAt",
         end: { $ifNull: ["$deletedAt", new Date()] },
         days_active: {
           $divide: [
             { $subtract: [
               { $ifNull: ["$deletedAt", new Date()] },
               "$createdAt"
             ]},
             86400000  // milliseconds in a day
           ]
         }
     }},
     { $sort: { days_active: -1 } }
   ])
   ```

## Performance Optimization

### Indexing Strategy
The schema includes indexes optimized for analytics queries:
- Status index for quick state aggregations
- Timestamp indexes for time-based analysis
- Soft delete index for active lamp queries

### Query Optimization Tips
1. Use timestamp range queries with indexes
2. Leverage boolean operations for status queries
3. Consider materialized views for complex aggregations
4. Use appropriate date/time functions for temporal analysis

## Data Export

### CSV Export
```sql
-- MySQL
SELECT * FROM lamps
INTO OUTFILE '/tmp/lamps_export.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n';

-- PostgreSQL
COPY lamps TO '/tmp/lamps_export.csv' WITH CSV HEADER;

-- MongoDB
mongoexport --db lamp_control --collection lamps --type=csv --out=/tmp/lamps_export.csv --fields="_id,isOn,createdAt,updatedAt,deletedAt"
```

### JSON Export
```bash
# MongoDB
mongoexport --db lamp_control --collection lamps --out=/tmp/lamps_export.json

# PostgreSQL (requires JSON functions)
psql -d lamp_control -c "COPY (SELECT row_to_json(lamps) FROM lamps) TO '/tmp/lamps_export.json'"
```

## Monitoring Recommendations

1. **Key Metrics to Monitor**
   - Query response times
   - Index usage statistics
   - Storage growth rate
   - Update frequency

2. **Alerting Thresholds**
   - High query latency (> 100ms)
   - Low index usage (< 90%)
   - Rapid storage growth
   - Unusual update patterns

## Integration with Analytics Tools

### Time Series Data
The schema supports time series analysis through:
- Timestamp fields (created_at, updated_at)
- Status change tracking
- Soft delete support

### ETL Considerations
1. Use timestamp fields for incremental loads
2. Track changes through updated_at field
3. Handle soft deletes appropriately
4. Consider partitioning by time ranges

### Recommended Tools
1. **Visualization**
   - Grafana
   - Tableau
   - Power BI

2. **Analysis**
   - Python (pandas)
   - R
   - Apache Spark

## Future Enhancements

1. **Planned Features**
   - Partitioning for historical data
   - Materialized views for common aggregations
   - Additional performance metrics
   - Enhanced time series capabilities

2. **Scalability Considerations**
   - Sharding strategies
   - Archive policies
   - Aggregation optimization
   - Cache implementation 