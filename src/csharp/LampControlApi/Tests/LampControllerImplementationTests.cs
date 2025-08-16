using LampControlApi.Controllers;
using LampControlApi.Services;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;

namespace LampControlApi.Tests
{
    /// <summary>
    /// Unit tests for the LampControllerImplementation.
    /// </summary>
    [TestClass]
    public class LampControllerImplementationTests
    {
        private Mock<ILampRepository> _mockRepository = null!;
        private LampControllerImplementation _controller = null!;

        /// <summary>
        /// Initialize test setup.
        /// </summary>
        [TestInitialize]
        public void Setup()
        {
            _mockRepository = new Mock<ILampRepository>();
            _controller = new LampControllerImplementation(_mockRepository.Object);
        }

        /// <summary>
        /// Test that constructor throws ArgumentNullException when repository is null.
        /// </summary>
        [TestMethod]
        public void Constructor_WithNullRepository_ShouldThrowArgumentNullException()
        {
            // Act & Assert
            Assert.ThrowsException<ArgumentNullException>(() => new LampControllerImplementation(null!));
        }

        /// <summary>
        /// Test that ListLampsAsync returns empty collection when repository is empty.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WhenRepositoryEmpty_ShouldReturnEmptyCollection()
        {
            // Arrange
            var emptyLamps = new List<Lamp>();
            _mockRepository.Setup(r => r.GetAllAsync()).ReturnsAsync(emptyLamps);

            // Act
            var result = await _controller.ListLampsAsync();

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(0, result.Count);
            _mockRepository.Verify(r => r.GetAllAsync(), Times.Once);
        }

        /// <summary>
        /// Test that ListLampsAsync returns all lamps from repository.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WithLampsInRepository_ShouldReturnAllLamps()
        {
            // Arrange
            var expectedLamps = new List<Lamp>
            {
                new Lamp { Id = Guid.NewGuid(), Status = true, UpdatedAt = DateTimeOffset.UtcNow.AddMinutes(-1) },
                new Lamp { Id = Guid.NewGuid(), Status = false, UpdatedAt = DateTimeOffset.UtcNow.AddMinutes(-2) },
                new Lamp { Id = Guid.NewGuid(), Status = true, UpdatedAt = DateTimeOffset.UtcNow }
            };
            _mockRepository.Setup(r => r.GetAllAsync()).ReturnsAsync(expectedLamps);

            // Act
            var result = await _controller.ListLampsAsync();

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(expectedLamps.Count, result.Count);

            // Ensure ordering matches UpdatedAt desc, then Id desc
            var expectedOrdered = expectedLamps.OrderByDescending(l => l.UpdatedAt).ThenByDescending(l => l.Id).ToList();
            CollectionAssert.AreEqual(expectedOrdered, result.ToList());
            _mockRepository.Verify(r => r.GetAllAsync(), Times.Once);
        }

        /// <summary>
        /// Test that CreateLampAsync throws ArgumentNullException when body is null.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task CreateLampAsync_WithNullBody_ShouldThrowArgumentNullException()
        {
            // Act & Assert
            await Assert.ThrowsExceptionAsync<ArgumentNullException>(() => _controller.CreateLampAsync(null!));
        }

        /// <summary>
        /// Test that CreateLampAsync creates lamp with correct properties.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task CreateLampAsync_WithValidBody_ShouldCreateLampWithCorrectProperties()
        {
            // Arrange
            var lampCreate = new LampCreate { Status = true };
            var expectedLamp = new Lamp { Id = Guid.NewGuid(), Status = true };

            _mockRepository.Setup(r => r.CreateAsync(It.IsAny<Lamp>()))
                .ReturnsAsync((Lamp lamp) =>
                {
                    lamp.Id = expectedLamp.Id; // Simulate repository setting ID
                    return lamp;
                });

            // Act
            var result = await _controller.CreateLampAsync(lampCreate);

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(expectedLamp.Id, result.Id);
            Assert.AreEqual(lampCreate.Status, result.Status);
            _mockRepository.Verify(
                r => r.CreateAsync(It.Is<Lamp>(l =>
                    l.Id != Guid.Empty && l.Status == lampCreate.Status)),
                Times.Once);
        }

        /// <summary>
        /// Test that CreateLampAsync generates new GUID for lamp ID.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task CreateLampAsync_ShouldGenerateNewGuidForLampId()
        {
            // Arrange
            var lampCreate = new LampCreate { Status = false };
            var capturedLamp = (Lamp?)null;

            _mockRepository.Setup(r => r.CreateAsync(It.IsAny<Lamp>()))
                .Callback<Lamp>(lamp => capturedLamp = lamp)
                .ReturnsAsync((Lamp lamp) => lamp);

            // Act
            await _controller.CreateLampAsync(lampCreate);

            // Assert
            Assert.IsNotNull(capturedLamp);
            Assert.AreNotEqual(Guid.Empty, capturedLamp.Id);
        }

        /// <summary>
        /// Test that GetLampAsync throws ArgumentException when lampId is null.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetLampAsync_WithNullLampId_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.GetLampAsync(null!));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that GetLampAsync throws ArgumentException when lampId is empty.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetLampAsync_WithEmptyLampId_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.GetLampAsync(string.Empty));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that GetLampAsync throws ArgumentException when lampId is whitespace.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetLampAsync_WithWhitespaceLampId_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.GetLampAsync("   "));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that GetLampAsync throws ArgumentException when lampId is invalid GUID format.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetLampAsync_WithInvalidGuidFormat_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.GetLampAsync("invalid-guid"));
            Assert.AreEqual("lampId", exception.ParamName);
            Assert.IsTrue(exception.Message.Contains("Invalid lamp ID format"));
        }

        /// <summary>
        /// Test that GetLampAsync throws KeyNotFoundException when lamp is not found.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetLampAsync_WhenLampNotFound_ShouldThrowKeyNotFoundException()
        {
            // Arrange
            var lampId = Guid.NewGuid();
            _mockRepository.Setup(r => r.GetByIdAsync(lampId)).ReturnsAsync((Lamp?)null);

            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<KeyNotFoundException>(() => _controller.GetLampAsync(lampId.ToString()));
            Assert.IsTrue(exception.Message.Contains(lampId.ToString()));
        }

        /// <summary>
        /// Test that GetLampAsync returns lamp when found.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task GetLampAsync_WhenLampExists_ShouldReturnLamp()
        {
            // Arrange
            var lampId = Guid.NewGuid();
            var expectedLamp = new Lamp { Id = lampId, Status = true };
            _mockRepository.Setup(r => r.GetByIdAsync(lampId)).ReturnsAsync(expectedLamp);

            // Act
            var result = await _controller.GetLampAsync(lampId.ToString());

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(expectedLamp.Id, result.Id);
            Assert.AreEqual(expectedLamp.Status, result.Status);
            _mockRepository.Verify(r => r.GetByIdAsync(lampId), Times.Once);
        }

        /// <summary>
        /// Test that UpdateLampAsync throws ArgumentException when lampId is null.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateLampAsync_WithNullLampId_ShouldThrowArgumentException()
        {
            // Arrange
            var lampUpdate = new LampUpdate { Status = true };

            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.UpdateLampAsync(null!, lampUpdate));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that UpdateLampAsync throws ArgumentException when lampId is empty.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateLampAsync_WithEmptyLampId_ShouldThrowArgumentException()
        {
            // Arrange
            var lampUpdate = new LampUpdate { Status = true };

            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.UpdateLampAsync(string.Empty, lampUpdate));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that UpdateLampAsync throws ArgumentNullException when body is null.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateLampAsync_WithNullBody_ShouldThrowArgumentNullException()
        {
            // Arrange
            var lampId = Guid.NewGuid().ToString();

            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentNullException>(() => _controller.UpdateLampAsync(lampId, null!));
            Assert.AreEqual("body", exception.ParamName);
        }

        /// <summary>
        /// Test that UpdateLampAsync throws ArgumentException when lampId is invalid GUID format.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateLampAsync_WithInvalidGuidFormat_ShouldThrowArgumentException()
        {
            // Arrange
            var lampUpdate = new LampUpdate { Status = true };

            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.UpdateLampAsync("invalid-guid", lampUpdate));
            Assert.AreEqual("lampId", exception.ParamName);
            Assert.IsTrue(exception.Message.Contains("Invalid lamp ID format"));
        }

        /// <summary>
        /// Test that UpdateLampAsync throws KeyNotFoundException when lamp is not found.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateLampAsync_WhenLampNotFound_ShouldThrowKeyNotFoundException()
        {
            // Arrange
            var lampId = Guid.NewGuid();
            var lampUpdate = new LampUpdate { Status = true };
            _mockRepository.Setup(r => r.GetByIdAsync(lampId)).ReturnsAsync((Lamp?)null);

            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<KeyNotFoundException>(() => _controller.UpdateLampAsync(lampId.ToString(), lampUpdate));
            Assert.IsTrue(exception.Message.Contains(lampId.ToString()));
        }

        /// <summary>
        /// Test that UpdateLampAsync updates and returns lamp when found.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateLampAsync_WhenLampExists_ShouldUpdateAndReturnLamp()
        {
            // Arrange
            var lampId = Guid.NewGuid();
            var existingLamp = new Lamp { Id = lampId, Status = false };
            var lampUpdate = new LampUpdate { Status = true };
            var updatedLamp = new Lamp { Id = lampId, Status = true };

            _mockRepository.Setup(r => r.GetByIdAsync(lampId)).ReturnsAsync(existingLamp);
            _mockRepository.Setup(r => r.UpdateAsync(It.IsAny<Lamp>())).ReturnsAsync(updatedLamp);

            // Act
            var result = await _controller.UpdateLampAsync(lampId.ToString(), lampUpdate);

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(lampId, result.Id);
            Assert.AreEqual(lampUpdate.Status, result.Status);
            _mockRepository.Verify(r => r.GetByIdAsync(lampId), Times.Once);
            _mockRepository.Verify(r => r.UpdateAsync(It.Is<Lamp>(l => l.Id == lampId && l.Status == lampUpdate.Status)), Times.Once);
        }

        /// <summary>
        /// Test that UpdateLampAsync modifies the existing lamp's status.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task UpdateLampAsync_ShouldModifyExistingLampStatus()
        {
            // Arrange
            var lampId = Guid.NewGuid();
            var existingLamp = new Lamp { Id = lampId, Status = false };
            var lampUpdate = new LampUpdate { Status = true };
            var capturedLamp = (Lamp?)null;

            _mockRepository.Setup(r => r.GetByIdAsync(lampId)).ReturnsAsync(existingLamp);
            _mockRepository.Setup(r => r.UpdateAsync(It.IsAny<Lamp>()))
                .Callback<Lamp>(lamp => capturedLamp = lamp)
                .ReturnsAsync((Lamp lamp) => lamp);

            // Act
            await _controller.UpdateLampAsync(lampId.ToString(), lampUpdate);

            // Assert
            Assert.IsNotNull(capturedLamp);
            Assert.AreEqual(lampId, capturedLamp.Id);
            Assert.AreEqual(lampUpdate.Status, capturedLamp.Status);

            // Verify that the same instance was modified
            Assert.AreSame(existingLamp, capturedLamp);
        }

        /// <summary>
        /// Test that DeleteLampAsync throws ArgumentException when lampId is null.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteLampAsync_WithNullLampId_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.DeleteLampAsync(null!));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that DeleteLampAsync throws ArgumentException when lampId is empty.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteLampAsync_WithEmptyLampId_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.DeleteLampAsync(string.Empty));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that DeleteLampAsync throws ArgumentException when lampId is whitespace.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteLampAsync_WithWhitespaceLampId_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.DeleteLampAsync("   "));
            Assert.AreEqual("lampId", exception.ParamName);
        }

        /// <summary>
        /// Test that DeleteLampAsync throws ArgumentException when lampId is invalid GUID format.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteLampAsync_WithInvalidGuidFormat_ShouldThrowArgumentException()
        {
            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<ArgumentException>(() => _controller.DeleteLampAsync("invalid-guid"));
            Assert.AreEqual("lampId", exception.ParamName);
            Assert.IsTrue(exception.Message.Contains("Invalid lamp ID format"));
        }

        /// <summary>
        /// Test that DeleteLampAsync throws KeyNotFoundException when lamp is not found.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteLampAsync_WhenLampNotFound_ShouldThrowKeyNotFoundException()
        {
            // Arrange
            var lampId = Guid.NewGuid();
            _mockRepository.Setup(r => r.DeleteAsync(lampId)).ReturnsAsync(false);

            // Act & Assert
            var exception = await Assert.ThrowsExceptionAsync<KeyNotFoundException>(() => _controller.DeleteLampAsync(lampId.ToString()));
            Assert.IsTrue(exception.Message.Contains(lampId.ToString()));
        }

        /// <summary>
        /// Test that DeleteLampAsync successfully deletes when lamp exists.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task DeleteLampAsync_WhenLampExists_ShouldDeleteSuccessfully()
        {
            // Arrange
            var lampId = Guid.NewGuid();
            _mockRepository.Setup(r => r.DeleteAsync(lampId)).ReturnsAsync(true);

            // Act
            await _controller.DeleteLampAsync(lampId.ToString());

            // Assert - No exception should be thrown
            _mockRepository.Verify(r => r.DeleteAsync(lampId), Times.Once);
        }
    }
}
