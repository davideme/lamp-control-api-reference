using LampControlApi.Controllers;
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
            var newLamp = new Lamp
            {
                Id = Guid.NewGuid(),
                Status = true
            };

            // Act
            var createdLamp = await _repository.CreateAsync(newLamp);
            var allLamps = await _repository.GetAllAsync();

            // Assert
            Assert.AreEqual(newLamp.Id, createdLamp.Id);
            Assert.AreEqual(newLamp.Status, createdLamp.Status);
            Assert.AreEqual(1, allLamps.Count);
        }

        /// <summary>
        /// Test getting a lamp by ID
        /// </summary>
        /// <returns>A task</returns>
        [TestMethod]
        public async Task GetByIdAsync_ShouldReturnCorrectLamp()
        {
            // Arrange
            var expectedId = Guid.Parse("123e4567-e89b-12d3-a456-426614174000");
            var lamp = new Lamp
            {
                Id = expectedId,
                Status = true
            };
            await _repository.CreateAsync(lamp);

            // Act
            var retrievedLamp = await _repository.GetByIdAsync(expectedId);

            // Assert
            Assert.IsNotNull(retrievedLamp);
            Assert.AreEqual(expectedId, retrievedLamp.Id);
            Assert.IsTrue(retrievedLamp.Status);
        }

        /// <summary>
        /// Test updating an existing lamp
        /// </summary>
        /// <returns>A task</returns>
        [TestMethod]
        public async Task UpdateAsync_ShouldModifyExistingLamp()
        {
            // Arrange
            var lampId = Guid.Parse("123e4567-e89b-12d3-a456-426614174001");
            var initialLamp = new Lamp
            {
                Id = lampId,
                Status = false
            };
            await _repository.CreateAsync(initialLamp);

            var updatedLamp = new Lamp
            {
                Id = lampId,
                Status = true // Flip the status
            };

            // Act
            var result = await _repository.UpdateAsync(updatedLamp);

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(lampId, result.Id);
            Assert.AreEqual(updatedLamp.Status, result.Status);
        }

        /// <summary>
        /// Test deleting a lamp
        /// </summary>
        /// <returns>A task</returns>
        [TestMethod]
        public async Task DeleteAsync_ShouldRemoveLamp()
        {
            // Arrange
            var lampId = Guid.Parse("123e4567-e89b-12d3-a456-426614174002");
            var lamp = new Lamp
            {
                Id = lampId,
                Status = true
            };
            await _repository.CreateAsync(lamp);

            // Act
            var deleted = await _repository.DeleteAsync(lampId);
            var allLamps = await _repository.GetAllAsync();

            // Assert
            Assert.IsTrue(deleted);
            Assert.AreEqual(0, allLamps.Count);
            Assert.IsFalse(allLamps.Any(l => l.Id == lampId));
        }

        /// <summary>
        /// Test deleting a non-existent lamp
        /// </summary>
        /// <returns>A task</returns>
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
    }
}
