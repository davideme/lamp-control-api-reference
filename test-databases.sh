#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Testing database setups..."

# Function to test MySQL
test_mysql() {
    echo -e "\n${GREEN}Testing MySQL...${NC}"
    mysql -h${MYSQL_HOST:-mysql} -ulamp_user -plamp_password lamp_control -e "
        SELECT 'Database connected successfully' as Status;
        SHOW TABLES;
        DESCRIBE lamps;
        INSERT INTO lamps (is_on) VALUES (true);
        SELECT * FROM lamps;
    "
}

# Function to test PostgreSQL
test_postgres() {
    echo -e "\n${GREEN}Testing PostgreSQL...${NC}"
    PGPASSWORD=lamp_password psql -h ${POSTGRES_HOST:-postgres} -U lamp_user -d lamp_control -c "
        \echo 'Database connected successfully';
        \dt
        \d lamps;
        INSERT INTO lamps (is_on) VALUES (true);
        SELECT * FROM lamps;
    "
}

# Function to test MongoDB
test_mongodb() {
    echo -e "\n${GREEN}Testing MongoDB...${NC}"
    mongosh "mongodb://lamp_user:lamp_password@${MONGODB_HOST:-mongodb}:27017/lamp_control" --eval '
        print("Database connected successfully");
        db.lamps.getIndexes();
        db.lamps.insertOne({
            _id: new UUID().toString(),
            isOn: true,
            createdAt: new Date(),
            updatedAt: new Date()
        });
        db.lamps.find();
    '
}

# No need to wait when using healthchecks
echo "Starting tests..."

# Run tests
test_mysql
test_postgres
test_mongodb

echo -e "\n${GREEN}All tests completed!${NC}" 