// MongoDB initialization script
db = db.getSiblingDB('lamp_control');

// Create collection with schema validation
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
});

// Create indexes
db.lamps.createIndex({ "isOn": 1 });
db.lamps.createIndex({ "createdAt": 1 });
db.lamps.createIndex({ "deletedAt": 1 });

// Insert a test document
db.lamps.insertOne({
  _id: UUID().toString(),
  isOn: false,
  createdAt: new Date(),
  updatedAt: new Date(),
  deletedAt: null
}); 