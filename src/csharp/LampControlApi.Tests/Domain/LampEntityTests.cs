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
        public void Create_ShouldGenerateNewIdAndDeferTimestampsToDatabase()
        {
            // Act
            var entity = LampEntity.Create(true);

            // Assert
            Assert.AreNotEqual(Guid.Empty, entity.Id);
            Assert.IsTrue(entity.Status);

            // Timestamps are managed by the database (DEFAULT CURRENT_TIMESTAMP + trigger).
            // Before a DB write they hold the default sentinel value.
            Assert.AreEqual(default(DateTimeOffset), entity.CreatedAt);
            Assert.AreEqual(default(DateTimeOffset), entity.UpdatedAt);
        }

        [TestMethod]
        public void WithUpdatedStatus_ShouldCreateUpdatedEntity()
        {
            // Arrange
            var originalEntity = LampEntity.Create(false);

            // Act
            var updatedEntity = originalEntity.WithUpdatedStatus(true);

            // Assert
            Assert.AreEqual(originalEntity.Id, updatedEntity.Id);
            Assert.IsTrue(updatedEntity.Status);
            Assert.AreEqual(originalEntity.CreatedAt, updatedEntity.CreatedAt);

            // UpdatedAt is owned by the database BEFORE UPDATE trigger;
            // the domain entity preserves the current value unchanged.
            Assert.AreEqual(originalEntity.UpdatedAt, updatedEntity.UpdatedAt);
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

            // Verify immutability - a new object should have been created
            Assert.IsFalse(ReferenceEquals(originalEntity, updatedEntity));

            // Both entities represent the same domain object (same ID) so they should be equal
            Assert.AreEqual(originalEntity, updatedEntity);
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

        /// <summary>
        /// Test that Equals returns false when comparing with null.
        /// </summary>
        [TestMethod]
        public void Equals_ShouldReturnFalseForNull()
        {
            // Arrange
            var entity = LampEntity.Create(true);

            // Act & Assert — tests the Equals(object?) override handles null
            Assert.IsFalse(entity.Equals((object?)null));
        }

        /// <summary>
        /// Test that Equals returns false when comparing with a different type.
        /// </summary>
        [TestMethod]
        public void Equals_ShouldReturnFalseForDifferentType()
        {
            // Arrange
            var entity = LampEntity.Create(true);

            // Act & Assert — tests the Equals(object?) override handles non-LampEntity types
            Assert.IsFalse(entity.Equals((object)"not a lamp entity"));
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
