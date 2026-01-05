using System;
using LampControlApi.Infrastructure.Database;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LampControlApi.Tests.Infrastructure
{
    /// <summary>
    /// Unit tests for LampDbEntity record.
    /// </summary>
    [TestClass]
    public class LampDbEntityTests
    {
        /// <summary>
        /// Test that LampDbEntity can be instantiated with init properties.
        /// </summary>
        [TestMethod]
        public void Constructor_ShouldInitializeProperties()
        {
            // Arrange
            var id = Guid.NewGuid();
            var createdAt = DateTimeOffset.UtcNow;
            var updatedAt = createdAt.AddMinutes(5);

            // Act
            var entity = new LampDbEntity
            {
                Id = id,
                IsOn = true,
                CreatedAt = createdAt,
                UpdatedAt = updatedAt,
                DeletedAt = null,
            };

            // Assert
            Assert.AreEqual(id, entity.Id);
            Assert.IsTrue(entity.IsOn);
            Assert.AreEqual(createdAt, entity.CreatedAt);
            Assert.AreEqual(updatedAt, entity.UpdatedAt);
            Assert.IsNull(entity.DeletedAt);
        }

        /// <summary>
        /// Test that LampDbEntity supports soft delete with DeletedAt timestamp.
        /// </summary>
        [TestMethod]
        public void DeletedAt_ShouldSupportSoftDelete()
        {
            // Arrange
            var deletedAt = DateTimeOffset.UtcNow;

            // Act
            var entity = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = false,
                CreatedAt = DateTimeOffset.UtcNow.AddHours(-1),
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = deletedAt,
            };

            // Assert
            Assert.IsNotNull(entity.DeletedAt);
            Assert.AreEqual(deletedAt, entity.DeletedAt);
        }

        /// <summary>
        /// Test that LampDbEntity supports with-expression for immutable updates.
        /// </summary>
        [TestMethod]
        public void WithExpression_ShouldCreateModifiedCopy()
        {
            // Arrange
            var original = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = false,
                CreatedAt = DateTimeOffset.UtcNow.AddHours(-1),
                UpdatedAt = DateTimeOffset.UtcNow.AddMinutes(-30),
                DeletedAt = null,
            };

            // Act
            var modified = original with { IsOn = true, UpdatedAt = DateTimeOffset.UtcNow };

            // Assert
            Assert.AreEqual(original.Id, modified.Id);
            Assert.AreEqual(original.CreatedAt, modified.CreatedAt);
            Assert.IsFalse(original.IsOn);
            Assert.IsTrue(modified.IsOn);
            Assert.AreNotEqual(original.UpdatedAt, modified.UpdatedAt);
        }

        /// <summary>
        /// Test that LampDbEntity record equality works by value.
        /// </summary>
        [TestMethod]
        public void RecordEquality_ShouldCompareByValue()
        {
            // Arrange
            var id = Guid.NewGuid();
            var createdAt = DateTimeOffset.UtcNow;
            var updatedAt = createdAt.AddMinutes(5);

            var entity1 = new LampDbEntity
            {
                Id = id,
                IsOn = true,
                CreatedAt = createdAt,
                UpdatedAt = updatedAt,
                DeletedAt = null,
            };

            var entity2 = new LampDbEntity
            {
                Id = id,
                IsOn = true,
                CreatedAt = createdAt,
                UpdatedAt = updatedAt,
                DeletedAt = null,
            };

            // Act & Assert
            Assert.AreEqual(entity1, entity2);
            Assert.IsTrue(entity1 == entity2);
        }

        /// <summary>
        /// Test that LampDbEntity records with different values are not equal.
        /// </summary>
        [TestMethod]
        public void RecordEquality_ShouldNotEqualDifferentValues()
        {
            // Arrange
            var entity1 = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            var entity2 = new LampDbEntity
            {
                Id = Guid.NewGuid(), // Different ID
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            // Act & Assert
            Assert.AreNotEqual(entity1, entity2);
            Assert.IsFalse(entity1 == entity2);
        }

        /// <summary>
        /// Test that LampDbEntity can be soft deleted using with-expression.
        /// </summary>
        [TestMethod]
        public void SoftDelete_ShouldSetDeletedAt()
        {
            // Arrange
            var original = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow.AddHours(-1),
                UpdatedAt = DateTimeOffset.UtcNow.AddMinutes(-30),
                DeletedAt = null,
            };

            // Act
            var deletedAt = DateTimeOffset.UtcNow;
            var softDeleted = original with { DeletedAt = deletedAt };

            // Assert
            Assert.IsNull(original.DeletedAt);
            Assert.IsNotNull(softDeleted.DeletedAt);
            Assert.AreEqual(deletedAt, softDeleted.DeletedAt);
        }

        /// <summary>
        /// Test that LampDbEntity GetHashCode works correctly for dictionary/set usage.
        /// </summary>
        [TestMethod]
        public void GetHashCode_ShouldBeConsistent()
        {
            // Arrange
            var id = Guid.NewGuid();
            var createdAt = DateTimeOffset.UtcNow;

            var entity1 = new LampDbEntity
            {
                Id = id,
                IsOn = true,
                CreatedAt = createdAt,
                UpdatedAt = createdAt,
                DeletedAt = null,
            };

            var entity2 = new LampDbEntity
            {
                Id = id,
                IsOn = true,
                CreatedAt = createdAt,
                UpdatedAt = createdAt,
                DeletedAt = null,
            };

            // Act
            var hash1 = entity1.GetHashCode();
            var hash2 = entity2.GetHashCode();

            // Assert
            Assert.AreEqual(hash1, hash2);
        }

        /// <summary>
        /// Test that LampDbEntity ToString provides meaningful output.
        /// </summary>
        [TestMethod]
        public void ToString_ShouldProvideReadableOutput()
        {
            // Arrange
            var entity = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            // Act
            var result = entity.ToString();

            // Assert
            Assert.IsNotNull(result);
            Assert.IsTrue(result.Contains("LampDbEntity"));
            Assert.IsTrue(result.Contains(entity.Id.ToString()));
        }

        /// <summary>
        /// Test that multiple with-expressions can be chained.
        /// </summary>
        [TestMethod]
        public void WithExpression_ShouldSupportChaining()
        {
            // Arrange
            var original = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = false,
                CreatedAt = DateTimeOffset.UtcNow.AddHours(-2),
                UpdatedAt = DateTimeOffset.UtcNow.AddHours(-1),
                DeletedAt = null,
            };

            // Act
            var step1 = original with { IsOn = true };
            var step2 = step1 with { UpdatedAt = DateTimeOffset.UtcNow };
            var step3 = step2 with { DeletedAt = DateTimeOffset.UtcNow };

            // Assert
            Assert.AreEqual(original.Id, step3.Id);
            Assert.IsFalse(original.IsOn);
            Assert.IsTrue(step3.IsOn);
            Assert.IsNull(original.DeletedAt);
            Assert.IsNotNull(step3.DeletedAt);
        }
    }
}
