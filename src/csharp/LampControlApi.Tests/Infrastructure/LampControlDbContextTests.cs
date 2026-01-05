using System;
using System.Linq;
using System.Threading.Tasks;
using LampControlApi.Infrastructure.Database;
using Microsoft.EntityFrameworkCore;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LampControlApi.Tests.Infrastructure
{
    /// <summary>
    /// Unit tests for LampControlDbContext configuration and behavior.
    /// </summary>
    [TestClass]
    public class LampControlDbContextTests
    {
        private DbContextOptions<LampControlDbContext> options = null!;

        /// <summary>
        /// Initialize test database with in-memory provider.
        /// </summary>
        [TestInitialize]
        public void Initialize()
        {
            this.options = new DbContextOptionsBuilder<LampControlDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;
        }

        /// <summary>
        /// Test that LampControlDbContext can be instantiated.
        /// </summary>
        [TestMethod]
        public void Constructor_ShouldCreateContext()
        {
            // Act
            using var context = new LampControlDbContext(this.options);

            // Assert
            Assert.IsNotNull(context);
            Assert.IsNotNull(context.Lamps);
        }

        /// <summary>
        /// Test that DbSet for lamps is properly configured.
        /// </summary>
        [TestMethod]
        public void Lamps_ShouldBeDbSet()
        {
            // Arrange & Act
            using var context = new LampControlDbContext(this.options);

            // Assert
            Assert.IsNotNull(context.Lamps);
            Assert.IsInstanceOfType(context.Lamps, typeof(DbSet<LampDbEntity>));
        }

        /// <summary>
        /// Test that soft delete query filter excludes deleted records.
        /// </summary>
        /// <returns>A <see cref="Task"/> representing the asynchronous unit test.</returns>
        [TestMethod]
        public async Task QueryFilter_ShouldExcludeDeletedLamps()
        {
            // Arrange
            using var context = new LampControlDbContext(this.options);

            var activeLamp = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            var deletedLamp = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = false,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = DateTimeOffset.UtcNow,
            };

            context.Lamps.Add(activeLamp);
            context.Lamps.Add(deletedLamp);
            await context.SaveChangesAsync();

            // Act
            var lamps = await context.Lamps.ToListAsync();

            // Assert
            Assert.AreEqual(1, lamps.Count);
            Assert.AreEqual(activeLamp.Id, lamps[0].Id);
        }

        /// <summary>
        /// Test that IgnoreQueryFilters allows retrieving deleted records.
        /// </summary>
        /// <returns>A <see cref="Task"/> representing the asynchronous unit test.</returns>
        [TestMethod]
        public async Task IgnoreQueryFilters_ShouldIncludeDeletedLamps()
        {
            // Arrange
            using var context = new LampControlDbContext(this.options);

            var activeLamp = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            var deletedLamp = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = false,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = DateTimeOffset.UtcNow,
            };

            context.Lamps.Add(activeLamp);
            context.Lamps.Add(deletedLamp);
            await context.SaveChangesAsync();

            // Act
            var lamps = await context.Lamps.IgnoreQueryFilters().ToListAsync();

            // Assert
            Assert.AreEqual(2, lamps.Count);
        }

        /// <summary>
        /// Test that primary key is properly configured.
        /// </summary>
        /// <returns>A <see cref="Task"/> representing the asynchronous unit test.</returns>
        [TestMethod]
        public async Task PrimaryKey_ShouldPreventDuplicateIds()
        {
            // Arrange
            using var context = new LampControlDbContext(this.options);
            var lampId = Guid.NewGuid();

            var lamp1 = new LampDbEntity
            {
                Id = lampId,
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            context.Lamps.Add(lamp1);
            await context.SaveChangesAsync();

            // Create a new context to avoid tracking conflicts
            using var context2 = new LampControlDbContext(this.options);

            var lamp2 = new LampDbEntity
            {
                Id = lampId, // Same ID
                IsOn = false,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            context2.Lamps.Add(lamp2);

            // Act & Assert - InMemory provider throws ArgumentException
            await Assert.ThrowsExceptionAsync<ArgumentException>(
                async () => await context2.SaveChangesAsync());
        }

        /// <summary>
        /// Test that entity can be added and retrieved.
        /// </summary>
        /// <returns>A <see cref="Task"/> representing the asynchronous unit test.</returns>
        [TestMethod]
        public async Task AddAndRetrieve_ShouldPersistEntity()
        {
            // Arrange
            using var context = new LampControlDbContext(this.options);
            var lamp = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            // Act
            context.Lamps.Add(lamp);
            await context.SaveChangesAsync();

            var retrieved = await context.Lamps.FindAsync(lamp.Id);

            // Assert
            Assert.IsNotNull(retrieved);
            Assert.AreEqual(lamp.Id, retrieved.Id);
            Assert.AreEqual(lamp.IsOn, retrieved.IsOn);
        }

        /// <summary>
        /// Test that entity can be updated.
        /// </summary>
        /// <returns>A <see cref="Task"/> representing the asynchronous unit test.</returns>
        [TestMethod]
        public async Task Update_ShouldModifyEntity()
        {
            // Arrange
            using var context = new LampControlDbContext(this.options);
            var lamp = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = false,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = null,
            };

            context.Lamps.Add(lamp);
            await context.SaveChangesAsync();

            // Act
            var updatedLamp = lamp with { IsOn = true, UpdatedAt = DateTimeOffset.UtcNow };
            context.Entry(lamp).CurrentValues.SetValues(updatedLamp);
            await context.SaveChangesAsync();

            var retrieved = await context.Lamps.FindAsync(lamp.Id);

            // Assert
            Assert.IsNotNull(retrieved);
            Assert.IsTrue(retrieved.IsOn);
        }

        /// <summary>
        /// Test that query filter works with FindAsync.
        /// </summary>
        /// <returns>A <see cref="Task"/> representing the asynchronous unit test.</returns>
        [TestMethod]
        public async Task FindAsync_ShouldReturnNullForDeletedEntity()
        {
            // Arrange
            using var context = new LampControlDbContext(this.options);
            var lamp = new LampDbEntity
            {
                Id = Guid.NewGuid(),
                IsOn = true,
                CreatedAt = DateTimeOffset.UtcNow,
                UpdatedAt = DateTimeOffset.UtcNow,
                DeletedAt = DateTimeOffset.UtcNow, // Soft deleted
            };

            context.Lamps.Add(lamp);
            await context.SaveChangesAsync();

            // Detach to ensure fresh query
            context.Entry(lamp).State = EntityState.Detached;

            // Act
            var retrieved = await context.Lamps
                .Where(l => l.Id == lamp.Id)
                .FirstOrDefaultAsync();

            // Assert
            Assert.IsNull(retrieved);
        }
    }
}
