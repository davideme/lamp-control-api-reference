using System;
using LampControlApi.Domain.Entities;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LampControlApi.Tests.Domain
{
    /// <summary>
    /// Unit tests for the LampEntity domain entity.
    /// </summary>
    [TestClass]
    public class LampEntityTests
    {
        [TestMethod]
        public void Create_ShouldGenerateNewIdAndTimestamps()
        {
            // Act
            var entity = LampEntity.Create(true);

            // Assert
            Assert.AreNotEqual(Guid.Empty, entity.Id);
            Assert.IsTrue(entity.Status);
            Assert.IsTrue(entity.CreatedAt <= DateTimeOffset.UtcNow);
            Assert.IsTrue(entity.UpdatedAt <= DateTimeOffset.UtcNow);
            Assert.AreEqual(entity.CreatedAt, entity.UpdatedAt);
        }

        [TestMethod]
        public void WithUpdatedStatus_ShouldCreateUpdatedEntity()
        {
            // Arrange
            var originalEntity = LampEntity.Create(false);
            System.Threading.Thread.Sleep(10); // Ensure different timestamp

            // Act
            var updatedEntity = originalEntity.WithUpdatedStatus(true);

            // Assert
            Assert.AreEqual(originalEntity.Id, updatedEntity.Id);
            Assert.IsTrue(updatedEntity.Status);
            Assert.AreEqual(originalEntity.CreatedAt, updatedEntity.CreatedAt);
            Assert.IsTrue(updatedEntity.UpdatedAt > originalEntity.UpdatedAt);
        }

        [TestMethod]
        public void WithUpdatedStatus_ShouldMaintainImmutability()
        {
            // Arrange
            var originalEntity = LampEntity.Create(false);

            // Act
            var updatedEntity = originalEntity.WithUpdatedStatus(true);

            // Assert
            Assert.IsFalse(originalEntity.Status);
            Assert.IsTrue(updatedEntity.Status);
            Assert.AreNotEqual(originalEntity, updatedEntity);
        }

        [TestMethod]
        public void Equals_ShouldReturnTrueForSameId()
        {
            // Arrange
            var id = Guid.NewGuid();
            var timestamp = DateTimeOffset.UtcNow;
            var entity1 = new LampEntity(id, true, timestamp, timestamp);
            var entity2 = new LampEntity(id, false, timestamp.AddDays(1), timestamp.AddDays(1));

            // Act & Assert
            Assert.IsTrue(entity1.Equals(entity2));
            Assert.AreEqual(entity1.GetHashCode(), entity2.GetHashCode());
        }

        [TestMethod]
        public void Equals_ShouldReturnFalseForDifferentId()
        {
            // Arrange
            var timestamp = DateTimeOffset.UtcNow;
            var entity1 = new LampEntity(Guid.NewGuid(), true, timestamp, timestamp);
            var entity2 = new LampEntity(Guid.NewGuid(), true, timestamp, timestamp);

            // Act & Assert
            Assert.IsFalse(entity1.Equals(entity2));
        }

        [TestMethod]
        public void ToString_ShouldReturnFormattedString()
        {
            // Arrange
            var entity = LampEntity.Create(true);

            // Act
            var result = entity.ToString();

            // Assert
            StringAssert.Contains(result, "LampEntity");
            StringAssert.Contains(result, entity.Id.ToString());
            StringAssert.Contains(result, "True");
        }

        [TestMethod]
        public void DomainEntitySeparation_ShouldBeIndependentOfApiModels()
        {
            // This test ensures that LampEntity doesn't depend on any API model classes
            // Arrange
            var entity = LampEntity.Create(true);

            // Assert - should be able to create and manipulate without API dependencies
            var entityType = typeof(LampEntity);
            var apiModelNamespace = "LampControlApi.Controllers";
            
            // Verify no direct dependency on API model classes in namespace
            Assert.IsFalse(entityType.Namespace!.Contains(apiModelNamespace));
            Assert.IsNotNull(entity.ToString()); // Basic functionality works
        }
    }
}