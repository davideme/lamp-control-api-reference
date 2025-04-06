# Lamp Control API Reference Implementation - Product Requirements Document

## 1. Overview

### 1.1 Product Summary
This project will create a comprehensive reference implementation of a simple lamp control API in multiple popular web programming languages. The API will demonstrate CRUD operations for a lamp resource with consistent implementations across different languages and database technologies, showcasing multiple API interface styles (REST, GraphQL, and gRPC).

### 1.2 Business Goals
- Provide a clear, concise reference implementation that demonstrates proper API design and implementation patterns
- Facilitate comparison between different programming languages and frameworks for web API development
- Showcase implementation differences in terms of code conciseness and test coverage

### 1.3 Success Metrics
- Complete implementation across all specified languages
- Consistent functionality across all implementations
- Comprehensive documentation
- Measurable comparison metrics (lines of code, test coverage)

## 2. Target Audience

### 2.1 Primary Users
- Web developers learning new programming languages or frameworks
- API designers looking for implementation patterns
- Technical educators and instructors
- Development teams evaluating technology choices for API development

### 2.2 User Needs
- Clear, idiomatic code examples in each language
- Consistent API design across implementations
- Easy setup and running instructions
- Comprehensive test coverage to demonstrate best practices

## 3. Product Requirements

### 3.1 Core Functionality

#### 3.1.1 Lamp Resource
A lamp will have the following properties:
- ID (unique identifier)
- Status (on/off boolean)

#### 3.1.2 API Functionality
All implementations must support the following operations:
- Create a new lamp with an initial on/off state
- Retrieve a lamp by ID
- List all lamps
- Update a lamp's on/off state
- Delete a lamp

#### 3.1.3 API Interface Styles
Each language implementation must provide the same functionality through three interface styles:
- REST API (OpenAPI 3.0+ compliant)
- GraphQL
- gRPC

### 3.2 Technical Requirements

#### 3.2.1 Programming Languages
The reference implementation will include the following languages:
- JavaScript/Node.js
- Python
- Java
- PHP
- Ruby
- Go

#### 3.2.2 Database Support
Each language implementation must support the following database options:
- SQL databases (MySQL and PostgreSQL)
- Document database (MongoDB)

Each implementation should use:
- The most efficient connection systems for each language
- The most common/popular solution for each language (ORM/ODM approaches or direct queries as appropriate)

#### 3.2.3 API Standards
- REST API will follow OpenAPI 3.0+ specification
- All interface definitions (REST, GraphQL schema, gRPC protobuf) must be consistent across languages
- API must include versioning (v1)

#### 3.2.4 Authentication
- Version 1 will not include authentication (open access)

#### 3.2.5 Testing
- Each implementation must include appropriate unit and integration tests
- Test coverage metrics must be generated and reported
- Tests should be comprehensive across all endpoints and database options

## 4. Project Structure

### 4.1 Repository Organization
- Monorepo structure with directories for each language implementation
- Root-level documentation for project overview and language comparison
- Standardized directory structure within each language implementation
- Individual READMEs for each language following a standardized format

### 4.2 Documentation Requirements
- Root-level project description and language comparison
- API specifications for all three interface styles (REST, GraphQL, gRPC)
- Language-specific READMEs with:
  - Setup instructions
  - API usage examples
  - Testing instructions
  - Implementation notes/decisions
  - Performance considerations

### 4.3 Comparison Metrics
- Lines of code analysis for each implementation
- Test coverage percentages
- High-level comparison of the three API interfaces (not language-specific)

## 5. Implementation Details

### 5.1 API Endpoints/Interfaces

#### 5.1.1 REST API (OpenAPI 3.0+)
- `GET /v1/lamps` - List all lamps
- `GET /v1/lamps/{id}` - Get a specific lamp
- `POST /v1/lamps` - Create a new lamp
- `PUT /v1/lamps/{id}` - Update a lamp's status
- `DELETE /v1/lamps/{id}` - Delete a lamp

#### 5.1.2 GraphQL
Schema must include:
- Query: getLamp, getLamps
- Mutation: createLamp, updateLamp, deleteLamp

#### 5.1.3 gRPC
Protobuf definition for:
- CreateLamp
- GetLamp
- ListLamps
- UpdateLamp
- DeleteLamp

### 5.2 Database Implementation
- Each language implementation must support both SQL (MySQL/PostgreSQL) and MongoDB
- Appropriate schema migration scripts or setup instructions
- Environment-based configuration for connection settings

### 5.3 Testing Requirements
- Unit tests for all business logic
- Integration tests for API endpoints
- Database integration testing
- Aim for high test coverage (minimum 80%)

## 6. Delivery

### 6.1 Milestones
- API specification finalization (REST, GraphQL, gRPC)
- Database schema design
- Initial implementation in one language (reference)
- Implementation across all languages
- Testing and documentation completion
- Comparison metrics generation

### 6.2 Completion Criteria
- All specified languages implemented
- All three API interfaces functional in each language
- Both database types supported in each language
- Comprehensive test coverage
- Complete documentation at root and language levels
- Comparison metrics generated

### 6.3 Future Considerations (beyond v1)
- Authentication mechanisms
- Additional lamp properties (brightness, color, etc.)
- Performance benchmarking
- Client implementation examples
- Containerization and deployment examples