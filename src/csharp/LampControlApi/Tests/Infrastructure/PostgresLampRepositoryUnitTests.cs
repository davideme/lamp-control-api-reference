using System;
using System.Linq;
using System.Threading.Tasks;
using LampControlApi.Domain.Entities;
using LampControlApi.Infrastructure.Database;
using LampControlApi.Services;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;

namespace LampControlApi.Tests.Infrastructure
{
    /// <summary>
    /// Unit tests for PostgresLampRepository edge cases and error handling.
    /// </summary>
    [TestClass]
    public class PostgresLampRepositoryUnitTests
    {
        private Mock<ILogger<PostgresLampRepository>> mockLogger = null!;

        /// <summary>
        /// Initialize mocks before each test.
        /// </summary>
        [TestInitialize]
        public void Initialize()
        {
            this.mockLogger = new Mock<ILogger<PostgresLampRepository>>();
        }

        /// <summary>
        /// Test that CreateAsync throws ArgumentNullException when entity is null.
        /// </summary>
        [TestMethod]
        public async Task CreateAsync_ShouldThrowArgumentNullException_WhenEntityIsNull()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);

            // Act & Assert
            await Assert.ThrowsExceptionAsync<ArgumentNullException>(
                async () => await repository.CreateAsync(null!));
        }

        /// <summary>
        /// Test that UpdateAsync throws ArgumentNullException when entity is null.
        /// </summary>
        [TestMethod]
        public async Task UpdateAsync_ShouldThrowArgumentNullException_WhenEntityIsNull()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);

            // Act & Assert
            await Assert.ThrowsExceptionAsync<ArgumentNullException>(
                async () => await repository.UpdateAsync(null!));
        }

        /// <summary>
        /// Test that constructor throws ArgumentNullException when context is null.
        /// </summary>
        [TestMethod]
        public void Constructor_ShouldThrowArgumentNullException_WhenContextIsNull()
        {
            // Act & Assert
            Assert.ThrowsException<ArgumentNullException>(
                () => new PostgresLampRepository(null!, this.mockLogger.Object));
        }

        /// <summary>
        /// Test that constructor throws ArgumentNullException when logger is null.
        /// </summary>
        [TestMethod]
        public void Constructor_ShouldThrowArgumentNullException_WhenLoggerIsNull()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);

            // Act & Assert
            Assert.ThrowsException<ArgumentNullException>(
                () => new PostgresLampRepository(context, null!));
        }

        /// <summary>
        /// Test that GetAllAsync returns empty collection when no lamps exist.
        /// </summary>
        [TestMethod]
        public async Task GetAllAsync_ShouldReturnEmptyCollection_WhenNoLampsExist()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);

            // Act
            var result = await repository.GetAllAsync();

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(0, result.Count);
        }

        /// <summary>
        /// Test that GetByIdAsync returns null for non-existent ID.
        /// </summary>
        [TestMethod]
        public async Task GetByIdAsync_ShouldReturnNull_WhenIdDoesNotExist()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);

            // Act
            var result = await repository.GetByIdAsync(Guid.NewGuid());

            // Assert
            Assert.IsNull(result);
        }

        /// <summary>
        /// Test that logging occurs for all CRUD operations.
        /// </summary>
        [TestMethod]
        public async Task Operations_ShouldLogDebugMessages()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);
            var lamp = LampEntity.Create(status: true);

            // Act
            await repository.CreateAsync(lamp);
            await repository.GetAllAsync();
            await repository.GetByIdAsync(lamp.Id);
            await repository.UpdateAsync(lamp);
            await repository.DeleteAsync(lamp.Id);

            // Assert - Verify debug logging was called
            this.mockLogger.Verify(
                logger => logger.Log(
                    LogLevel.Debug,
                    It.IsAny<EventId>(),
                    It.IsAny<It.IsAnyType>(),
                    It.IsAny<Exception>(),
                    It.IsAny<Func<It.IsAnyType, Exception?, string>>()),
                Times.AtLeast(5));
        }

        /// <summary>
        /// Test that CreateAsync preserves entity ID.
        /// </summary>
        [TestMethod]
        public async Task CreateAsync_ShouldPreserveEntityId()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);
            var expectedId = Guid.NewGuid();
            var lamp = new LampEntity(
                id: expectedId,
                status: true,
                createdAt: DateTimeOffset.UtcNow,
                updatedAt: DateTimeOffset.UtcNow);

            // Act
            var result = await repository.CreateAsync(lamp);

            // Assert
            Assert.AreEqual(expectedId, result.Id);
        }

        /// <summary>
        /// Test that multiple lamps can be created without conflicts.
        /// </summary>
        [TestMethod]
        public async Task CreateAsync_ShouldHandleMultipleLamps()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);

            var lamp1 = LampEntity.Create(status: true);
            var lamp2 = LampEntity.Create(status: false);
            var lamp3 = LampEntity.Create(status: true);

            // Act
            await repository.CreateAsync(lamp1);
            await repository.CreateAsync(lamp2);
            await repository.CreateAsync(lamp3);

            var allLamps = await repository.GetAllAsync();

            // Assert
            Assert.AreEqual(3, allLamps.Count);
        }

        /// <summary>
        /// Test that UpdateAsync returns null when updating non-existent lamp.
        /// </summary>
        [TestMethod]
        public async Task UpdateAsync_ShouldReturnNull_WhenLampDoesNotExist()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);
            var nonExistentLamp = LampEntity.Create(status: true);

            // Act
            var result = await repository.UpdateAsync(nonExistentLamp);

            // Assert
            Assert.IsNull(result);
        }

        /// <summary>
        /// Test that DeleteAsync returns false when deleting non-existent lamp.
        /// </summary>
        [TestMethod]
        public async Task DeleteAsync_ShouldReturnFalse_WhenLampDoesNotExist()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);

            // Act
            var result = await repository.DeleteAsync(Guid.NewGuid());

            // Assert
            Assert.IsFalse(result);
        }

        /// <summary>
        /// Test that DeleteAsync prevents double deletion.
        /// </summary>
        [TestMethod]
        public async Task DeleteAsync_ShouldReturnFalse_WhenLampAlreadyDeleted()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);
            var lamp = LampEntity.Create(status: true);

            await repository.CreateAsync(lamp);
            await repository.DeleteAsync(lamp.Id);

            // Act - Try to delete again
            var result = await repository.DeleteAsync(lamp.Id);

            // Assert
            Assert.IsFalse(result);
        }

        /// <summary>
        /// Test that GetAllAsync orders lamps by creation date.
        /// </summary>
        [TestMethod]
        public async Task GetAllAsync_ShouldOrderByCreatedAt()
        {
            // Arrange
            var options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            using var context = new LampControlDbContext(options);
            var repository = new PostgresLampRepository(context, this.mockLogger.Object);

            // Create lamps with slight delays to ensure different timestamps
            var lamp1 = LampEntity.Create(status: true);
            await repository.CreateAsync(lamp1);
            await Task.Delay(10);

            var lamp2 = LampEntity.Create(status: false);
            await repository.CreateAsync(lamp2);
            await Task.Delay(10);

            var lamp3 = LampEntity.Create(status: true);
            await repository.CreateAsync(lamp3);

            // Act
            var allLamps = await repository.GetAllAsync();

            // Assert
            Assert.AreEqual(3, allLamps.Count);

            // Verify order - should be oldest first
            var lampsList = allLamps.ToList();
            Assert.IsTrue(lampsList[0].CreatedAt <= lampsList[1].CreatedAt);
            Assert.IsTrue(lampsList[1].CreatedAt <= lampsList[2].CreatedAt);
        }
    }
}
