using LampControlApi.Domain.Repositories;
using LampControlApi.Extensions;
using LampControlApi.Services;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.FileProviders;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Npgsql;

namespace LampControlApi.Tests.Extensions
{
    [TestClass]
    [DoNotParallelize]
    public class ServiceCollectionExtensionsTests
    {
        private const string ConnectionStringEnvVar = "ConnectionStrings__LampControl";
        private const string DatabaseUrlEnvVar = "DATABASE_URL";

        private string? originalConnectionStringEnvVar;
        private string? originalDatabaseUrlEnvVar;

        [TestInitialize]
        public void Setup()
        {
            this.originalConnectionStringEnvVar = Environment.GetEnvironmentVariable(ConnectionStringEnvVar);
            this.originalDatabaseUrlEnvVar = Environment.GetEnvironmentVariable(DatabaseUrlEnvVar);
            Environment.SetEnvironmentVariable(ConnectionStringEnvVar, null);
            Environment.SetEnvironmentVariable(DatabaseUrlEnvVar, null);
        }

        [TestCleanup]
        public void Cleanup()
        {
            Environment.SetEnvironmentVariable(ConnectionStringEnvVar, this.originalConnectionStringEnvVar);
            Environment.SetEnvironmentVariable(DatabaseUrlEnvVar, this.originalDatabaseUrlEnvVar);
        }

        [TestMethod]
        public void ResolveConnectionString_ConfigConnectionStringShouldTakePrecedence()
        {
            var configuration = BuildConfiguration(new Dictionary<string, string?>
            {
                ["ConnectionStrings:LampControl"] = "Host=config-host;Port=5432;Database=config-db;Username=config-user;Password=config-pass",
            });

            Environment.SetEnvironmentVariable(
                ConnectionStringEnvVar,
                "Host=env-host;Port=5432;Database=env-db;Username=env-user;Password=env-pass");
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://url-user:url-pass@url-host:5432/url-db");

            var result = ServiceCollectionExtensions.ResolveConnectionString(configuration);

            Assert.AreEqual(
                "Host=config-host;Port=5432;Database=config-db;Username=config-user;Password=config-pass",
                result);
        }

        [TestMethod]
        public void ResolveConnectionString_ConnectionStringsEnvironmentVariableShouldTakePrecedenceOverDatabaseUrl()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                ConnectionStringEnvVar,
                "Host=env-host;Port=5432;Database=env-db;Username=env-user;Password=env-pass");
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://url-user:url-pass@url-host:5432/url-db");

            var result = ServiceCollectionExtensions.ResolveConnectionString(configuration);

            Assert.AreEqual(
                "Host=env-host;Port=5432;Database=env-db;Username=env-user;Password=env-pass",
                result);
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldUseDatabaseUrlWhenOtherSourcesAreMissing()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://lampuser:lamppass@localhost:5432/lampcontrol?sslmode=Disable");

            var result = ServiceCollectionExtensions.ResolveConnectionString(configuration);
            var parsedConnectionString = new NpgsqlConnectionStringBuilder(result);

            Assert.AreEqual("localhost", parsedConnectionString.Host);
            Assert.AreEqual(5432, parsedConnectionString.Port);
            Assert.AreEqual("lampcontrol", parsedConnectionString.Database);
            Assert.AreEqual("lampuser", parsedConnectionString.Username);
            Assert.AreEqual("lamppass", parsedConnectionString.Password);
            Assert.AreEqual(SslMode.Disable, parsedConnectionString.SslMode);
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldAcceptPostgresScheme()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgres://lampuser:lamppass@localhost:5433/lampcontrol");

            var result = ServiceCollectionExtensions.ResolveConnectionString(configuration);
            var parsedConnectionString = new NpgsqlConnectionStringBuilder(result);

            Assert.AreEqual("localhost", parsedConnectionString.Host);
            Assert.AreEqual(5433, parsedConnectionString.Port);
            Assert.AreEqual("lampcontrol", parsedConnectionString.Database);
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldDecodeCredentialsFromDatabaseUrl()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://lamp%2Buser:p%40ss%3Aword@localhost:5432/lampcontrol");

            var result = ServiceCollectionExtensions.ResolveConnectionString(configuration);
            var parsedConnectionString = new NpgsqlConnectionStringBuilder(result);

            Assert.AreEqual("lamp+user", parsedConnectionString.Username);
            Assert.AreEqual("p@ss:word", parsedConnectionString.Password);
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldThrowWhenDatabaseUrlHasUnsupportedQueryParameter()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://lampuser:lamppass@localhost:5432/lampcontrol?unknown_param=42");

            var exception = Assert.ThrowsException<InvalidOperationException>(
                () => ServiceCollectionExtensions.ResolveConnectionString(configuration));

            StringAssert.Contains(exception.Message, "Unsupported DATABASE_URL query parameter");
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldThrowWhenDatabaseUrlHasInvalidScheme()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "mysql://lampuser:lamppass@localhost:5432/lampcontrol");

            var exception = Assert.ThrowsException<InvalidOperationException>(
                () => ServiceCollectionExtensions.ResolveConnectionString(configuration));

            StringAssert.Contains(exception.Message, "Invalid DATABASE_URL scheme");
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldThrowWhenDatabaseUrlHasMissingHost()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://lampuser:lamppass@/lampcontrol");

            var exception = Assert.ThrowsException<InvalidOperationException>(
                () => ServiceCollectionExtensions.ResolveConnectionString(configuration));

            StringAssert.Contains(exception.Message, "Host is required");
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldThrowWhenDatabaseUrlHasMissingDatabaseName()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://lampuser:lamppass@localhost:5432");

            var exception = Assert.ThrowsException<InvalidOperationException>(
                () => ServiceCollectionExtensions.ResolveConnectionString(configuration));

            StringAssert.Contains(exception.Message, "Database name is required");
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldThrowWhenDatabaseUrlIsMalformed()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "this-is-not-a-url");

            var exception = Assert.ThrowsException<InvalidOperationException>(
                () => ServiceCollectionExtensions.ResolveConnectionString(configuration));

            StringAssert.Contains(exception.Message, "Invalid DATABASE_URL value");
        }

        [TestMethod]
        public void ResolveConnectionString_ShouldThrowWhenPasswordIsMissing()
        {
            var configuration = BuildConfiguration();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://lampuser@localhost:5432/lampcontrol");

            var exception = Assert.ThrowsException<InvalidOperationException>(
                () => ServiceCollectionExtensions.ResolveConnectionString(configuration));

            StringAssert.Contains(exception.Message, "Password is required");
        }

        [TestMethod]
        public void AddLampControlServices_ShouldUsePostgresRepositoryWhenOnlyDatabaseUrlIsConfigured()
        {
            var services = new ServiceCollection();
            var configuration = BuildConfiguration();
            var environment = new TestWebHostEnvironment();
            Environment.SetEnvironmentVariable(
                DatabaseUrlEnvVar,
                "postgresql://lampuser:lamppass@localhost:5432/lampcontrol");

            var usePostgres = services.AddLampControlServices(configuration, environment);

            Assert.IsTrue(usePostgres);
            Assert.IsTrue(
                services.Any(descriptor =>
                    descriptor.ServiceType == typeof(ILampRepository) &&
                    descriptor.ImplementationType == typeof(PostgresLampRepository)));
        }

        private static IConfiguration BuildConfiguration(Dictionary<string, string?>? values = null)
        {
            return new ConfigurationBuilder()
                .AddInMemoryCollection(values ?? new Dictionary<string, string?>())
                .Build();
        }

        private sealed class TestWebHostEnvironment : IWebHostEnvironment
        {
            public string ApplicationName { get; set; } = "LampControlApi.Tests";

            public IFileProvider ContentRootFileProvider { get; set; } = NullFileProvider.Instance;

            public string ContentRootPath { get; set; } = Environment.CurrentDirectory;

            public string EnvironmentName { get; set; } = "Production";

            public IFileProvider WebRootFileProvider { get; set; } = NullFileProvider.Instance;

            public string WebRootPath { get; set; } = Environment.CurrentDirectory;
        }
    }
}
