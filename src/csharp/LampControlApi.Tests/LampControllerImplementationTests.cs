using System.Threading;
using LampControlApi.Controllers;
using LampControlApi.Domain.Entities;
using LampControlApi.Domain.Repositories;
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
        /// Test that ListLampsAsync (parameterless) returns empty collection when repository is empty.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WhenRepositoryEmpty_ShouldReturnEmptyCollection()
        {
            // Arrange
            var emptyLampEntities = new List<LampEntity>();
            _mockRepository.Setup(r => r.ListAsync(int.MaxValue, 0, It.IsAny<CancellationToken>())).ReturnsAsync(emptyLampEntities);

            // Act
            var result = await _controller.ListLampsAsync();

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(0, result.Count);
            _mockRepository.Verify(r => r.ListAsync(int.MaxValue, 0, It.IsAny<CancellationToken>()), Times.Once);
        }

        /// <summary>
        /// Test that ListLampsAsync (parameterless) returns all lamps from repository.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WithLampsInRepository_ShouldReturnAllLamps()
        {
            // Arrange
            var expectedLampEntities = new List<LampEntity>
            {
                new LampEntity(Guid.NewGuid(), true, DateTimeOffset.UtcNow.AddMinutes(-2), DateTimeOffset.UtcNow.AddMinutes(-1)),
                new LampEntity(Guid.NewGuid(), false, DateTimeOffset.UtcNow.AddMinutes(-3), DateTimeOffset.UtcNow.AddMinutes(-2)),
                new LampEntity(Guid.NewGuid(), true, DateTimeOffset.UtcNow.AddMinutes(-1), DateTimeOffset.UtcNow)
            };
            _mockRepository.Setup(r => r.ListAsync(int.MaxValue, 0, It.IsAny<CancellationToken>())).ReturnsAsync(expectedLampEntities);

            // Act
            var result = await _controller.ListLampsAsync();

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(expectedLampEntities.Count, result.Count);
            _mockRepository.Verify(r => r.ListAsync(int.MaxValue, 0, It.IsAny<CancellationToken>()), Times.Once);
        }

        /// <summary>
        /// Test that ListLampsAsync with pageSize and cursor returns first page with HasMore=true.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WithPageSize_WhenMoreResultsExist_ShouldReturnHasMoreTrue()
        {
            // Arrange - repository returns pageSize+1 items, signalling there is a next page
            var pageSize = 2;
            var entities = new List<LampEntity>
            {
                new LampEntity(Guid.NewGuid(), true, DateTimeOffset.UtcNow.AddMinutes(-3), DateTimeOffset.UtcNow),
                new LampEntity(Guid.NewGuid(), false, DateTimeOffset.UtcNow.AddMinutes(-2), DateTimeOffset.UtcNow),
                new LampEntity(Guid.NewGuid(), true, DateTimeOffset.UtcNow.AddMinutes(-1), DateTimeOffset.UtcNow),  // extra item
            };
            _mockRepository.Setup(r => r.ListAsync(pageSize + 1, 0, It.IsAny<CancellationToken>())).ReturnsAsync(entities);

            // Act
            var actionResult = await _controller.ListLampsAsync(string.Empty, pageSize);
            var response = actionResult.Value!;

            // Assert
            Assert.IsNotNull(response);
            Assert.AreEqual(pageSize, response.Data.Count);
            Assert.IsTrue(response.HasMore);
            Assert.AreEqual("2", response.NextCursor);
        }

        /// <summary>
        /// Test that ListLampsAsync with pageSize and cursor returns last page with HasMore=false.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WithPageSize_WhenOnLastPage_ShouldReturnHasMoreFalse()
        {
            // Arrange - repository returns fewer than pageSize+1 items, signalling this is the last page
            var pageSize = 2;
            var entities = new List<LampEntity>
            {
                new LampEntity(Guid.NewGuid(), true, DateTimeOffset.UtcNow.AddMinutes(-2), DateTimeOffset.UtcNow),
                new LampEntity(Guid.NewGuid(), false, DateTimeOffset.UtcNow.AddMinutes(-1), DateTimeOffset.UtcNow),
            };
            _mockRepository.Setup(r => r.ListAsync(pageSize + 1, 0, It.IsAny<CancellationToken>())).ReturnsAsync(entities);

            // Act
            var actionResult = await _controller.ListLampsAsync(string.Empty, pageSize);
            var response = actionResult.Value!;

            // Assert
            Assert.IsNotNull(response);
            Assert.AreEqual(2, response.Data.Count);
            Assert.IsFalse(response.HasMore);
            Assert.AreEqual(string.Empty, response.NextCursor);
        }

        /// <summary>
        /// Test that ListLampsAsync advances offset correctly using the cursor.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WithCursor_ShouldPassCorrectOffsetToRepository()
        {
            // Arrange - cursor="4" means offset 4, pageSize=2, so request limit=3 at offset=4
            var pageSize = 2;
            var cursor = "4";
            var entities = new List<LampEntity>
            {
                new LampEntity(Guid.NewGuid(), true, DateTimeOffset.UtcNow.AddMinutes(-2), DateTimeOffset.UtcNow),
            };
            _mockRepository.Setup(r => r.ListAsync(pageSize + 1, 4, It.IsAny<CancellationToken>())).ReturnsAsync(entities);

            // Act
            var actionResult = await _controller.ListLampsAsync(cursor, pageSize);
            var response = actionResult.Value!;

            // Assert
            Assert.IsNotNull(response);
            Assert.AreEqual(1, response.Data.Count);
            Assert.IsFalse(response.HasMore);
            _mockRepository.Verify(r => r.ListAsync(pageSize + 1, 4, It.IsAny<CancellationToken>()), Times.Once);
        }

        /// <summary>
        /// Test that ListLampsAsync uses default page size when pageSize is zero or negative.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WithNonPositivePageSize_ShouldUseDefaultPageSize()
        {
            // Arrange - default page size is 25, so repository is called with limit=26
            var entities = new List<LampEntity>();
            _mockRepository.Setup(r => r.ListAsync(26, 0, It.IsAny<CancellationToken>())).ReturnsAsync(entities);

            // Act
            var actionResult = await _controller.ListLampsAsync(string.Empty, 0);
            var response = actionResult.Value!;

            // Assert
            Assert.IsNotNull(response);
            _mockRepository.Verify(r => r.ListAsync(26, 0, It.IsAny<CancellationToken>()), Times.Once);
        }

        /// <summary>
        /// Test that ListLampsAsync clamps oversized pageSize to MaxPageSize.
        /// </summary>
        /// <returns>A task.</returns>
        [TestMethod]
        public async Task ListLampsAsync_WithExcessivePageSize_ShouldClampToMaxPageSize()
        {
            // Arrange - MaxPageSize=1000, so limit should be 1001 (1000+1)
            var entities = new List<LampEntity>();
            _mockRepository.Setup(r => r.ListAsync(1001, 0, It.IsAny<CancellationToken>())).ReturnsAsync(entities);

            // Act - pass a value far exceeding MaxPageSize
            var actionResult = await _controller.ListLampsAsync(string.Empty, int.MaxValue);
            var response = actionResult.Value!;

            // Assert
            Assert.IsNotNull(response);
            _mockRepository.Verify(r => r.ListAsync(1001, 0, It.IsAny<CancellationToken>()), Times.Once);
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

            _mockRepository.Setup(r => r.CreateAsync(It.IsAny<LampEntity>(), It.IsAny<CancellationToken>()))
                .ReturnsAsync((LampEntity lampEntity, CancellationToken _) =>
                {
                    // Return a LampEntity with the expected ID to simulate repository setting ID
                    return new LampEntity(expectedLamp.Id, lampEntity.Status, DateTimeOffset.UtcNow, DateTimeOffset.UtcNow);
                });

            // Act
            var actionResult = await _controller.CreateLampAsync(lampCreate);

            // The implementation may return an ActionResult with Value set or an
            // IActionResult (e.g., CreatedAtActionResult). Handle both cases.
            Lamp? result = actionResult.Value;
            if (result == null && actionResult.Result is Microsoft.AspNetCore.Mvc.CreatedAtActionResult createdResult)
            {
                result = createdResult.Value as Lamp;
            }

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(expectedLamp.Id, result.Id);
            Assert.AreEqual(lampCreate.Status, result.Status);
            _mockRepository.Verify(
                r => r.CreateAsync(It.Is<LampEntity>(l =>
                    l.Id != Guid.Empty && l.Status == lampCreate.Status), It.IsAny<CancellationToken>()),
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
            var capturedLampEntity = (LampEntity?)null;

            _mockRepository.Setup(r => r.CreateAsync(It.IsAny<LampEntity>(), It.IsAny<CancellationToken>()))
                .Callback<LampEntity, CancellationToken>((lampEntity, _) => capturedLampEntity = lampEntity)
                .ReturnsAsync((LampEntity lampEntity, CancellationToken _) => lampEntity);

            // Act
            await _controller.CreateLampAsync(lampCreate);

            // Assert
            Assert.IsNotNull(capturedLampEntity);
            Assert.AreNotEqual(Guid.Empty, capturedLampEntity.Id);
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
            _mockRepository.Setup(r => r.GetByIdAsync(lampId, It.IsAny<CancellationToken>())).ReturnsAsync((LampEntity?)null);

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
            var lampEntity = new LampEntity(lampId, true, DateTimeOffset.UtcNow, DateTimeOffset.UtcNow);
            _mockRepository.Setup(r => r.GetByIdAsync(lampId, It.IsAny<CancellationToken>())).ReturnsAsync(lampEntity);

            // Act
            var actionResult = await _controller.GetLampAsync(lampId.ToString());
            var result = actionResult.Value!;

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(expectedLamp.Id, result.Id);
            Assert.AreEqual(expectedLamp.Status, result.Status);
            _mockRepository.Verify(r => r.GetByIdAsync(lampId, It.IsAny<CancellationToken>()), Times.Once);
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
            _mockRepository.Setup(r => r.UpdateAsync(lampId, lampUpdate.Status, It.IsAny<CancellationToken>())).ReturnsAsync((LampEntity?)null);

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
            var lampUpdate = new LampUpdate { Status = true };
            var updatedLampEntity = new LampEntity(lampId, true, DateTimeOffset.UtcNow, DateTimeOffset.UtcNow);

            _mockRepository.Setup(r => r.UpdateAsync(lampId, lampUpdate.Status, It.IsAny<CancellationToken>())).ReturnsAsync(updatedLampEntity);

            // Act
            var actionResult = await _controller.UpdateLampAsync(lampId.ToString(), lampUpdate);
            var result = actionResult.Value!;

            // Assert
            Assert.IsNotNull(result);
            Assert.AreEqual(lampId, result.Id);
            Assert.AreEqual(lampUpdate.Status, result.Status);
            _mockRepository.Verify(r => r.UpdateAsync(lampId, lampUpdate.Status, It.IsAny<CancellationToken>()), Times.Once);
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
            var lampUpdate = new LampUpdate { Status = true };
            var capturedLampId = Guid.Empty;
            var capturedStatus = false;
            _mockRepository.Setup(r => r.UpdateAsync(It.IsAny<Guid>(), It.IsAny<bool>(), It.IsAny<CancellationToken>()))
                .Callback<Guid, bool, CancellationToken>((id, status, _) =>
                {
                    capturedLampId = id;
                    capturedStatus = status;
                })
                .ReturnsAsync(new LampEntity(lampId, lampUpdate.Status, DateTimeOffset.UtcNow, DateTimeOffset.UtcNow));

            // Act
            await _controller.UpdateLampAsync(lampId.ToString(), lampUpdate);

            // Assert
            Assert.AreEqual(lampId, capturedLampId);
            Assert.AreEqual(lampUpdate.Status, capturedStatus);
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
            _mockRepository.Setup(r => r.DeleteAsync(lampId, It.IsAny<CancellationToken>())).ReturnsAsync(false);

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
            _mockRepository.Setup(r => r.DeleteAsync(lampId, It.IsAny<CancellationToken>())).ReturnsAsync(true);

            // Act
            var actionResult = await _controller.DeleteLampAsync(lampId.ToString());

            // Assert - No exception should be thrown and check for NoContentResult
            Assert.IsInstanceOfType<Microsoft.AspNetCore.Mvc.NoContentResult>(actionResult);
            _mockRepository.Verify(r => r.DeleteAsync(lampId, It.IsAny<CancellationToken>()), Times.Once);
        }
    }
}
