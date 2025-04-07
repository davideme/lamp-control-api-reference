// Extend Jest timeout for integration tests
jest.setTimeout(30000);

// Global test setup can be added here
beforeAll(async () => {
  // Setup test database connections, etc.
});

// Global test teardown
afterAll(async () => {
  // Cleanup test resources
}); 