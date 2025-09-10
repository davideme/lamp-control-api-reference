using System;
using LampControlApi.Controllers;
using LampControlApi.Domain.Entities;
using LampControlApi.Domain.Mappers;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LampControlApi.Tests.Domain
{
    /// <summary>
    /// Unit tests for the LampMapper.
    /// </summary>
    [TestClass]
    public class LampMapperTests
    {
        [TestMethod]
        public void ToApiModel_ShouldConvertEntityToApiModel()
        {
            // Arrange
            var id = Guid.NewGuid();
            var createdAt = DateTimeOffset.UtcNow.AddDays(-1);
            var updatedAt = DateTimeOffset.UtcNow;
            var entity = new LampEntity(id, true, createdAt, updatedAt);

            // Act
            var apiModel = LampMapper.ToApiModel(entity);

            // Assert
            Assert.AreEqual(id, apiModel.Id);
            Assert.IsTrue(apiModel.Status);
            Assert.AreEqual(createdAt, apiModel.CreatedAt);
            Assert.AreEqual(updatedAt, apiModel.UpdatedAt);
        }

        [TestMethod]
        [ExpectedException(typeof(ArgumentNullException))]
        public void ToApiModel_WithNullEntity_ShouldThrowArgumentNullException()
        {
            // Act
            LampMapper.ToApiModel(null!);
        }

        [TestMethod]
        public void ToDomainEntity_ShouldConvertApiModelToEntity()
        {
            // Arrange
            var id = Guid.NewGuid();
            var createdAt = DateTimeOffset.UtcNow.AddDays(-1);
            var updatedAt = DateTimeOffset.UtcNow;
            var apiModel = new Lamp
            {
                Id = id,
                Status = false,
                CreatedAt = createdAt,
                UpdatedAt = updatedAt
            };

            // Act
            var entity = LampMapper.ToDomainEntity(apiModel);

            // Assert
            Assert.AreEqual(id, entity.Id);
            Assert.IsFalse(entity.Status);
            Assert.AreEqual(createdAt, entity.CreatedAt);
            Assert.AreEqual(updatedAt, entity.UpdatedAt);
        }

        [TestMethod]
        [ExpectedException(typeof(ArgumentNullException))]
        public void ToDomainEntity_WithNullApiModel_ShouldThrowArgumentNullException()
        {
            // Act
            LampMapper.ToDomainEntity(null!);
        }

        [TestMethod]
        public void ToDomainEntityCreate_ShouldCreateEntityFromCreateModel()
        {
            // Arrange
            var createModel = new LampCreate { Status = true };

            // Act
            var entity = LampMapper.ToDomainEntityCreate(createModel);

            // Assert
            Assert.AreNotEqual(Guid.Empty, entity.Id);
            Assert.IsTrue(entity.Status);
            Assert.IsTrue(entity.CreatedAt <= DateTimeOffset.UtcNow);
            Assert.IsTrue(entity.UpdatedAt <= DateTimeOffset.UtcNow);
        }

        [TestMethod]
        public void ToDomainEntityCreate_WithFalseStatus_ShouldCreateEntityWithFalseStatus()
        {
            // Arrange
            var createModel = new LampCreate { Status = false };

            // Act
            var entity = LampMapper.ToDomainEntityCreate(createModel);

            // Assert
            Assert.IsFalse(entity.Status);
        }

        [TestMethod]
        [ExpectedException(typeof(ArgumentNullException))]
        public void ToDomainEntityCreate_WithNullCreateModel_ShouldThrowArgumentNullException()
        {
            // Act
            LampMapper.ToDomainEntityCreate(null!);
        }

        [TestMethod]
        public void UpdateDomainEntity_ShouldUpdateEntityFromUpdateModel()
        {
            // Arrange
            var originalEntity = LampEntity.Create(false);
            var updateModel = new LampUpdate { Status = true };

            // Act
            var updatedEntity = LampMapper.UpdateDomainEntity(originalEntity, updateModel);

            // Assert
            Assert.AreEqual(originalEntity.Id, updatedEntity.Id);
            Assert.IsTrue(updatedEntity.Status);
            Assert.AreEqual(originalEntity.CreatedAt, updatedEntity.CreatedAt);
            Assert.IsTrue(updatedEntity.UpdatedAt >= originalEntity.UpdatedAt);
        }

        [TestMethod]
        public void UpdateDomainEntity_WithFalseStatus_ShouldUpdateStatus()
        {
            // Arrange
            var originalEntity = LampEntity.Create(true);
            var updateModel = new LampUpdate { Status = false };

            // Act
            var updatedEntity = LampMapper.UpdateDomainEntity(originalEntity, updateModel);

            // Assert
            Assert.IsFalse(updatedEntity.Status); // Should update to false
        }

        [TestMethod]
        [ExpectedException(typeof(ArgumentNullException))]
        public void UpdateDomainEntity_WithNullEntity_ShouldThrowArgumentNullException()
        {
            // Arrange
            var updateModel = new LampUpdate { Status = true };

            // Act
            LampMapper.UpdateDomainEntity(null!, updateModel);
        }

        [TestMethod]
        [ExpectedException(typeof(ArgumentNullException))]
        public void UpdateDomainEntity_WithNullUpdateModel_ShouldThrowArgumentNullException()
        {
            // Arrange
            var entity = LampEntity.Create(true);

            // Act
            LampMapper.UpdateDomainEntity(entity, null!);
        }
    }
}
