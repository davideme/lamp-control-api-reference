using System;
using System.IO;
using System.Threading.Tasks;
using DotNet.Testcontainers.Builders;
using LampControlApi.Domain.Entities;
using LampControlApi.Infrastructure.Database;
using LampControlApi.Services;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging.Abstractions;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Testcontainers.PostgreSql;

namespace LampControlApi.Tests.Infrastructure
{
    /// <summary>
    /// Integration tests for PostgresLampRepository using Testcontainers.
    /// </summary>
    [TestClass]
    public class PostgresLampRepositoryTests
    {
        private PostgreSqlContainer? postgres;
        private LampControlDbContext? context;
        private PostgresLampRepository? repository;

        /// <summary>
        /// Initializes the test by starting a PostgreSQL container and setting up the database context.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous operation.</placeholder></returns>
        [TestInitialize]
        public async Task InitializeAsync()
        {
            this.postgres = new PostgreSqlBuilder()
                .WithImage("postgres:16-alpine")
                .WithDatabase("lampcontrol_test")
                .WithUsername("test")
                .WithPassword("test")
                .WithWaitStrategy(Wait.ForUnixContainer().UntilPortIsAvailable(5432))
                .Build();

            await this.postgres.StartAsync();

            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseNpgsql(this.postgres.GetConnectionString())
                .Options;

            this.context = new LampControlDbContext(options);

            // Apply actual schema from schema.sql file (including triggers)
            var repositoryRoot = FindRepositoryRoot();
            var schemaPath = Path.Combine(
                repositoryRoot,
                "database",
                "sql",
                "postgresql",
                "schema.sql");

            var schemaSql = await File.ReadAllTextAsync(schemaPath);
            await this.context.Database.ExecuteSqlRawAsync(schemaSql);

            this.repository = new PostgresLampRepository(
                this.context,
                NullLogger<PostgresLampRepository>.Instance);
        }

        /// <summary>
        /// Cleanup test resources.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous operation.</placeholder></returns>
        [TestCleanup]
        public async Task CleanupAsync()
        {
            if (this.context != null)
            {
                await this.context.DisposeAsync();
            }

            if (this.postgres != null)
            {
                await this.postgres.DisposeAsync();
            }
        }

        /// <summary>
        /// Test creating a lamp in PostgreSQL.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task CreateAsync_ShouldPersistLamp()
        {
            // Arrange
            var lamp = LampEntity.Create(status: true);

            // Act
            var created = await this.repository!.CreateAsync(lamp);

            // Assert
            Assert.IsNotNull(created);
            Assert.AreEqual(lamp.Id, created.Id);
            Assert.IsTrue(created.Status);
            Assert.IsTrue(created.CreatedAt > DateTimeOffset.MinValue);
            Assert.IsTrue(created.UpdatedAt > DateTimeOffset.MinValue);

            // Verify persistence by retrieving
            var retrieved = await this.repository.GetByIdAsync(created.Id);
            Assert.IsNotNull(retrieved);
            Assert.AreEqual(created.Id, retrieved.Id);
            Assert.AreEqual(created.Status, retrieved.Status);
        }

        /// <summary>
        /// Test getting all lamps from PostgreSQL.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task GetAllAsync_ShouldReturnAllLamps()
        {
            // Arrange
            var lamp1 = LampEntity.Create(status: true);
            var lamp2 = LampEntity.Create(status: false);

            await this.repository!.CreateAsync(lamp1);
            await this.repository.CreateAsync(lamp2);

            // Act
            var allLamps = await this.repository.GetAllAsync();

            // Assert
            Assert.IsNotNull(allLamps);
            Assert.AreEqual(2, allLamps.Count);
        }

        /// <summary>
        /// Test getting a lamp by ID from PostgreSQL.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task GetByIdAsync_ShouldReturnLamp_WhenExists()
        {
            // Arrange
            var lamp = LampEntity.Create(status: true);
            var created = await this.repository!.CreateAsync(lamp);

            // Act
            var retrieved = await this.repository.GetByIdAsync(created.Id);

            // Assert
            Assert.IsNotNull(retrieved);
            Assert.AreEqual(created.Id, retrieved.Id);
            Assert.AreEqual(created.Status, retrieved.Status);
        }

        /// <summary>
        /// Test getting a non-existent lamp by ID from PostgreSQL.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task GetByIdAsync_ShouldReturnNull_WhenNotExists()
        {
            // Arrange
            var nonExistentId = Guid.NewGuid();

            // Act
            var retrieved = await this.repository!.GetByIdAsync(nonExistentId);

            // Assert
            Assert.IsNull(retrieved);
        }

        /// <summary>
        /// Test updating a lamp in PostgreSQL.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task UpdateAsync_ShouldModifyLamp()
        {
            // Arrange
            var lamp = LampEntity.Create(status: false);
            var created = await this.repository!.CreateAsync(lamp);

            // Act
            var updatedLamp = created.WithUpdatedStatus(newStatus: true);
            var result = await this.repository.UpdateAsync(updatedLamp);

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(created.Id, result.Id);
            Assert.IsTrue(result.Status);
            Assert.IsTrue(result.UpdatedAt > created.UpdatedAt);

            // Verify persistence
            var retrieved = await this.repository.GetByIdAsync(created.Id);
            Assert.IsNotNull(retrieved);
            Assert.IsTrue(retrieved.Status);
        }

        /// <summary>
        /// Test updating a non-existent lamp in PostgreSQL.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task UpdateAsync_ShouldReturnNull_WhenNotExists()
        {
            // Arrange
            var nonExistentLamp = LampEntity.Create(status: true);

            // Act
            var result = await this.repository!.UpdateAsync(nonExistentLamp);

            // Assert
            Assert.IsNull(result);
        }

        /// <summary>
        /// Test deleting a lamp from PostgreSQL (soft delete).
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task DeleteAsync_ShouldSoftDeleteLamp()
        {
            // Arrange
            var lamp = LampEntity.Create(status: true);
            var created = await this.repository!.CreateAsync(lamp);

            // Act
            var deleted = await this.repository.DeleteAsync(created.Id);

            // Assert
            Assert.IsTrue(deleted);

            // Verify soft delete - lamp should not be retrievable
            var retrieved = await this.repository.GetByIdAsync(created.Id);
            Assert.IsNull(retrieved);
        }

        /// <summary>
        /// Test deleting a non-existent lamp from PostgreSQL.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task DeleteAsync_ShouldReturnFalse_WhenNotExists()
        {
            // Arrange
            var nonExistentId = Guid.NewGuid();

            // Act
            var deleted = await this.repository!.DeleteAsync(nonExistentId);

            // Assert
            Assert.IsFalse(deleted);
        }

        /// <summary>
        /// Test that soft-deleted lamps are not returned by GetAllAsync.
        /// </summary>
        /// <returns><placeholder>A <see cref="Task"/> representing the asynchronous unit test.</placeholder></returns>
        [TestMethod]
        public async Task GetAllAsync_ShouldNotReturnDeletedLamps()
        {
            // Arrange
            var lamp1 = LampEntity.Create(status: true);
            var lamp2 = LampEntity.Create(status: false);

            var created1 = await this.repository!.CreateAsync(lamp1);
            await this.repository.CreateAsync(lamp2);

            // Delete one lamp
            await this.repository.DeleteAsync(created1.Id);

            // Act
            var allLamps = await this.repository.GetAllAsync();

            // Assert
            Assert.IsNotNull(allLamps);
            Assert.AreEqual(1, allLamps.Count);
        }

        /// <summary>
        /// Finds the repository root by searching upward for the .git directory.
        /// This is more robust than using relative paths with multiple ".." operators.
        /// </summary>
        /// <returns>The absolute path to the repository root.</returns>
        /// <exception cref="DirectoryNotFoundException">Thrown when the repository root cannot be found.</exception>
        private static string FindRepositoryRoot()
        {
            var currentDirectory = new DirectoryInfo(Directory.GetCurrentDirectory());

            while (currentDirectory != null)
            {
                // Check if .git directory exists in current directory
                if (Directory.Exists(Path.Combine(currentDirectory.FullName, ".git")))
                {
                    return currentDirectory.FullName;
                }

                // Move up to parent directory
                currentDirectory = currentDirectory.Parent;
            }

            throw new DirectoryNotFoundException(
                "Could not find repository root. Ensure tests are run from within the repository.");
        }
    }
}
