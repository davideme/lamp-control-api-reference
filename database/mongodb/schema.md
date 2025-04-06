# MongoDB Schema Documentation

## Database: lamp_control

### Collection: lamps

#### Document Structure

```javascript
{
  "_id": "<UUID string>",  // Unique identifier for the lamp
  "isOn": false,          // Current status of the lamp (true = ON, false = OFF)
  "createdAt": ISODate(),  // Timestamp when the lamp was created
  "updatedAt": ISODate(),  // Timestamp when the lamp was last updated
  "deletedAt": ISODate()   // Timestamp when the lamp was soft deleted (null if active)
}
```

#### Validation Schema

```javascript
{
  $jsonSchema: {
    bsonType: "object",
    required: ["_id", "isOn", "createdAt", "updatedAt"],
    properties: {
      _id: {
        bsonType: "string",
        description: "UUID string as the document identifier"
      },
      isOn: {
        bsonType: "bool",
        description: "Current status of the lamp (true = ON, false = OFF)"
      },
      createdAt: {
        bsonType: "date",
        description: "Timestamp when the lamp was created"
      },
      updatedAt: {
        bsonType: "date",
        description: "Timestamp when the lamp was last updated"
      },
      deletedAt: {
        bsonType: ["date", "null"],
        description: "Timestamp when the lamp was soft deleted"
      }
    }
  }
}
```

#### Indexes

1. Default `_id` index (automatically created)
```javascript
db.lamps.createIndex({ "_id": 1 }, { unique: true })
```

2. Status index for quick status-based queries
```javascript
db.lamps.createIndex({ "isOn": 1 })
```

3. Timestamps indexes for time-based queries and soft deletes
```javascript
db.lamps.createIndex({ "createdAt": 1 })
db.lamps.createIndex({ "deletedAt": 1 })
```

#### Example Usage

1. Create a new lamp
```javascript
db.lamps.insertOne({
  "_id": uuid(),  // Generate UUID
  "isOn": false,
  "createdAt": new Date(),
  "updatedAt": new Date(),
  "deletedAt": null
})
```

2. Update lamp status
```javascript
db.lamps.updateOne(
  { "_id": "<lamp_id>" },
  { 
    $set: { 
      "isOn": true,
      "updatedAt": new Date()
    }
  }
)
```

3. Soft delete a lamp
```javascript
db.lamps.updateOne(
  { "_id": "<lamp_id>" },
  { 
    $set: { 
      "deletedAt": new Date(),
      "updatedAt": new Date()
    }
  }
)
```

4. Query active lamps
```javascript
db.lamps.find({ "deletedAt": null })
```

### Collection Creation Script

```javascript
db.createCollection("lamps", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "isOn", "createdAt", "updatedAt"],
      properties: {
        _id: {
          bsonType: "string",
          description: "UUID string as the document identifier"
        },
        isOn: {
          bsonType: "bool",
          description: "Current status of the lamp (true = ON, false = OFF)"
        },
        createdAt: {
          bsonType: "date",
          description: "Timestamp when the lamp was created"
        },
        updatedAt: {
          bsonType: "date",
          description: "Timestamp when the lamp was last updated"
        },
        deletedAt: {
          bsonType: ["date", "null"],
          description: "Timestamp when the lamp was soft deleted"
        }
      }
    }
  }
})
``` 