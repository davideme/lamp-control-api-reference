using System;
using System.Linq;
using System.Threading.Tasks;
using LampControlApi.Domain.Entities;
using LampControlApi.Domain.Repositories;
using LampControlApi.Services;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LampControlApi.Tests
{
    /// <summary>
    /// Unit tests for the InMemoryLampRepository.
    /// </summary>
    [TestClass]
    public class InMemoryLampRepositoryTests
    {
        private InMemoryLampRepository _repository = null!;

        /// <summary>
        /// Initialize test setup.
        /// </summary>
        [TestInitialize]
        public void Setup()
        {
            _repository = new InMemoryLampRepository();
        }

        /// <summary>
        /// Test that the repository starts empty.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetAllAsync_ShouldReturnEmptyCollection()
        {
            // Act
            var lamps = await _repository.GetAllAsync();

            // Assert
            Assert.AreEqual(0, lamps.Count);
        }

        /// <summary>
        /// Test creating a new lamp.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task CreateAsync_ShouldAddNewLamp()
        {
            // Arrange
            var newLamp = LampEntity.Create(true);

            // Act
            var createdLamp = await _repository.CreateAsync(newLamp);
            var allLamps = await _repository.GetAllAsync();

            // Assert
            Assert.AreEqual(newLamp.Id, createdLamp.Id);
            Assert.AreEqual(newLamp.Status, createdLamp.Status);
            Assert.AreEqual(1, allLamps.Count);
        }

        /// <summary>
        /// Test getting a lamp by ID.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetByIdAsync_ShouldReturnCorrectLamp()
        {
            // Arrange
            var expectedId = Guid.Parse("123e4567-e89b-12d3-a456-426614174000");
            var createdAt = DateTimeOffset.UtcNow;
            var lamp = new LampEntity(expectedId, true, createdAt, createdAt);
            await _repository.CreateAsync(lamp);

            // Act
            var retrievedLamp = await _repository.GetByIdAsync(expectedId);

            // Assert
            Assert.IsNotNull(retrievedLamp);
            Assert.AreEqual(expectedId, retrievedLamp.Id);
            Assert.IsTrue(retrievedLamp.Status);
        }

        /// <summary>
        /// Test updating an existing lamp.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateAsync_ShouldModifyExistingLamp()
        {
            // Arrange
            var lampId = Guid.Parse("123e4567-e89b-12d3-a456-426614174001");
            var createdAt = DateTimeOffset.UtcNow;
            var initialLamp = new LampEntity(lampId, false, createdAt, createdAt);
            await _repository.CreateAsync(initialLamp);

            var updatedLamp = initialLamp.WithUpdatedStatus(true); // Flip the status

            // Act
            var result = await _repository.UpdateAsync(updatedLamp);

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(lampId, result.Id);
            Assert.IsTrue(result.Status);
        }

        /// <summary>
        /// Test deleting a lamp.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteAsync_ShouldRemoveLamp()
        {
            // Arrange
            var lampId = Guid.Parse("123e4567-e89b-12d3-a456-426614174002");
            var lamp = LampEntity.Create(true);
            var lampWithId = new LampEntity(lampId, lamp.Status, lamp.CreatedAt, lamp.UpdatedAt);
            await _repository.CreateAsync(lampWithId);

            // Act
            var deleted = await _repository.DeleteAsync(lampId);
            var allLamps = await _repository.GetAllAsync();

            // Assert
            Assert.IsTrue(deleted);
            Assert.AreEqual(0, allLamps.Count);
            Assert.IsFalse(allLamps.Any(l => l.Id == lampId));
        }

        /// <summary>
        /// Test creating a lamp with null entity throws ArgumentNullException.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task CreateAsync_WithNullEntity_ShouldThrowArgumentNullException()
        {
            // Act & Assert
            await Assert.ThrowsExceptionAsync<ArgumentNullException>(() => _repository.CreateAsync(null!));
        }

        /// <summary>
        /// Test updating a lamp with null entity throws ArgumentNullException.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateAsync_WithNullEntity_ShouldThrowArgumentNullException()
        {
            // Act & Assert
            await Assert.ThrowsExceptionAsync<ArgumentNullException>(() => _repository.UpdateAsync(null!));
        }

        /// <summary>
        /// Test updating a non-existent lamp returns null.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateAsync_NonExistentLamp_ShouldReturnNull()
        {
            // Arrange
            var nonExistentLamp = LampEntity.Create(true);

            // Act
            var result = await _repository.UpdateAsync(nonExistentLamp);

            // Assert
            Assert.IsNull(result);
        }

        /// <summary>
        /// Test getting a non-existent lamp by ID returns null.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetByIdAsync_NonExistentLamp_ShouldReturnNull()
        {
            // Act
            var result = await _repository.GetByIdAsync(Guid.NewGuid());

            // Assert
            Assert.IsNull(result);
        }

        /// <summary>
        /// Test deleting a non-existent lamp.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteAsync_NonExistentLamp_ShouldReturnFalse()
        {
            // Arrange
            var nonExistentId = Guid.NewGuid();

            // Act
            var deleted = await _repository.DeleteAsync(nonExistentId);

            // Assert
            Assert.IsFalse(deleted);
        }

        /// <summary>
        /// Test that ListAsync returns the correct page of lamps.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListAsync_ShouldReturnPagedResults()
        {
            // Arrange
            var now = DateTimeOffset.UtcNow;
            for (var i = 0; i < 5; i++)
            {
                var lamp = new LampEntity(Guid.NewGuid(), true, now.AddSeconds(i), now.AddSeconds(i));
                await _repository.CreateAsync(lamp);
            }

            // Act
            var page1 = await _repository.ListAsync(limit: 2, offset: 0);
            var page2 = await _repository.ListAsync(limit: 2, offset: 2);
            var page3 = await _repository.ListAsync(limit: 2, offset: 4);

            // Assert
            Assert.AreEqual(2, page1.Count);
            Assert.AreEqual(2, page2.Count);
            Assert.AreEqual(1, page3.Count);
        }

        /// <summary>
        /// Test that ListAsync returns lamps ordered by CreatedAt then Id.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListAsync_ShouldReturnLampsOrderedByCreatedAt()
        {
            // Arrange
            var now = DateTimeOffset.UtcNow;
            var lamp1 = new LampEntity(Guid.NewGuid(), true, now.AddSeconds(2), now);
            var lamp2 = new LampEntity(Guid.NewGuid(), false, now.AddSeconds(0), now);
            var lamp3 = new LampEntity(Guid.NewGuid(), true, now.AddSeconds(1), now);
            await _repository.CreateAsync(lamp1);
            await _repository.CreateAsync(lamp2);
            await _repository.CreateAsync(lamp3);

            // Act
            var result = await _repository.ListAsync(limit: 3, offset: 0);
            var list = result.ToList();

            // Assert - should be oldest first
            Assert.AreEqual(3, list.Count);
            Assert.IsTrue(list[0].CreatedAt <= list[1].CreatedAt);
            Assert.IsTrue(list[1].CreatedAt <= list[2].CreatedAt);
        }

        /// <summary>
        /// Test that ListAsync returns empty collection when offset exceeds total count.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListAsync_ShouldReturnEmpty_WhenOffsetExceedsCount()
        {
            // Arrange
            await _repository.CreateAsync(LampEntity.Create(true));

            // Act
            var result = await _repository.ListAsync(limit: 10, offset: 100);

            // Assert
            Assert.AreEqual(0, result.Count);
        }
    }
}
