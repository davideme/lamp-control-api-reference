using LampControlApi.Controllers;
using LampControlApi.Domain.Repositories;
using LampControlApi.Infrastructure.Database;
using LampControlApi.Services;
using Microsoft.EntityFrameworkCore;
using Npgsql;

namespace LampControlApi.Extensions
{
    /// <summary>
    /// Extension methods for configuring application services.
    /// </summary>
    public static class ServiceCollectionExtensions
    {
        /// <summary>
        /// Adds Lamp Control API services to the service collection, including
        /// controllers, database context, repository, health checks, and Swagger.
        /// </summary>
        /// <param name="services">The service collection.</param>
        /// <param name="configuration">The application configuration.</param>
        /// <param name="environment">The web host environment.</param>
        /// <returns>True if PostgreSQL is configured, false if using in-memory storage.</returns>
        public static bool AddLampControlServices(
            this IServiceCollection services,
            IConfiguration configuration,
            IWebHostEnvironment environment)
        {
            services.AddControllers();

            var connectionString = ResolveConnectionString(configuration);
            var usePostgres = !string.IsNullOrWhiteSpace(connectionString);

            if (usePostgres)
            {
                services.AddDbContext<LampControlDbContext>(options =>
                {
                    options.UseNpgsql(connectionString!, npgsqlOptions =>
                    {
                        npgsqlOptions.EnableRetryOnFailure(
                            maxRetryCount: 3,
                            maxRetryDelay: TimeSpan.FromSeconds(5),
                            errorCodesToAdd: null);
                    });

                    if (environment.IsDevelopment())
                    {
                        options.EnableSensitiveDataLogging();
                        options.EnableDetailedErrors();
                    }
                });

                services.AddScoped<ILampRepository, PostgresLampRepository>();

                services.AddHealthChecks()
                    .AddDbContextCheck<LampControlDbContext>("database");
            }
            else
            {
                services.AddSingleton<ILampRepository, InMemoryLampRepository>();
                services.AddHealthChecks();
            }

            services.AddScoped<IController, LampControllerImplementation>();

            services.AddEndpointsApiExplorer();
            services.AddSwaggerGen();

            return usePostgres;
        }

        /// <summary>
        /// Resolves the database connection string from configuration or environment variables.
        /// </summary>
        /// <param name="configuration">The application configuration.</param>
        /// <returns>The connection string, or null if not configured.</returns>
        internal static string? ResolveConnectionString(IConfiguration configuration)
        {
            var connectionString = configuration.GetConnectionString("LampControl");

            if (string.IsNullOrWhiteSpace(connectionString))
            {
                connectionString = Environment.GetEnvironmentVariable("ConnectionStrings__LampControl");
            }

            if (string.IsNullOrWhiteSpace(connectionString))
            {
                var databaseUrl = Environment.GetEnvironmentVariable("DATABASE_URL");
                if (!string.IsNullOrWhiteSpace(databaseUrl))
                {
                    connectionString = ConvertDatabaseUrlToNpgsqlConnectionString(databaseUrl);
                }
            }

            return connectionString;
        }

        private static string ConvertDatabaseUrlToNpgsqlConnectionString(string databaseUrl)
        {
            if (!TryParseDatabaseUrl(databaseUrl, out var parsedUrl))
            {
                throw new InvalidOperationException(
                    "Invalid DATABASE_URL value. Expected a valid absolute URI like " +
                    "'postgresql://user:password@host:5432/database'.");
            }

            if (!string.Equals(parsedUrl.Scheme, "postgresql", StringComparison.OrdinalIgnoreCase) &&
                !string.Equals(parsedUrl.Scheme, "postgres", StringComparison.OrdinalIgnoreCase))
            {
                throw new InvalidOperationException(
                    $"Invalid DATABASE_URL scheme '{parsedUrl.Scheme}'. Supported schemes are 'postgresql' and 'postgres'.");
            }

            var databaseName = parsedUrl.DatabaseName.Trim('/');
            if (string.IsNullOrWhiteSpace(databaseName))
            {
                throw new InvalidOperationException(
                    "Invalid DATABASE_URL value. Database name is required in the path, for example: " +
                    "'postgresql://user:password@localhost:5432/lampcontrol'.");
            }

            var (username, password) = ParseUserInfo(parsedUrl.UserInfo);
            if (!string.IsNullOrWhiteSpace(username) && string.IsNullOrWhiteSpace(password))
            {
                throw new InvalidOperationException(
                    "Invalid DATABASE_URL value. Password is required when a username is provided.");
            }

            var builder = new NpgsqlConnectionStringBuilder
            {
                Host = parsedUrl.Host,
                Port = parsedUrl.Port > 0 ? parsedUrl.Port.Value : 5432,
                Database = Uri.UnescapeDataString(databaseName),
            };

            if (!string.IsNullOrWhiteSpace(username))
            {
                builder.Username = username;
            }

            if (!string.IsNullOrWhiteSpace(password))
            {
                builder.Password = password;
            }

            ApplySupportedDatabaseUrlQueryParameters(builder, parsedUrl.Query);

            if (string.IsNullOrWhiteSpace(builder.Host))
            {
                throw new InvalidOperationException(
                    "Invalid DATABASE_URL value. Host is required, for example: " +
                    "'postgresql://user:password@localhost:5432/lampcontrol', or via " +
                    "'?host=/cloudsql/project:region:instance' for Unix sockets.");
            }

            return builder.ConnectionString;
        }

        private static bool TryParseDatabaseUrl(string databaseUrl, out ParsedDatabaseUrl parsedUrl)
        {
            parsedUrl = default!;
            if (string.IsNullOrWhiteSpace(databaseUrl))
            {
                return false;
            }

            var schemeSeparatorIndex = databaseUrl.IndexOf("://", StringComparison.Ordinal);
            if (schemeSeparatorIndex <= 0)
            {
                return false;
            }

            var scheme = databaseUrl[..schemeSeparatorIndex];
            var remainder = databaseUrl.Substring(schemeSeparatorIndex + 3);

            var querySeparatorIndex = remainder.IndexOf('?');
            var authorityAndPath = querySeparatorIndex >= 0 ? remainder[..querySeparatorIndex] : remainder;
            var query = querySeparatorIndex >= 0 ? remainder.Substring(querySeparatorIndex + 1) : string.Empty;

            var pathSeparatorIndex = authorityAndPath.IndexOf('/');
            var authority = pathSeparatorIndex >= 0 ? authorityAndPath[..pathSeparatorIndex] : authorityAndPath;
            var rawPath = pathSeparatorIndex >= 0
                ? authorityAndPath.Substring(pathSeparatorIndex + 1)
                : string.Empty;

            string? userInfo = null;
            var hostAndPort = authority;
            var userInfoSeparatorIndex = authority.LastIndexOf('@');
            if (userInfoSeparatorIndex >= 0)
            {
                userInfo = authority[..userInfoSeparatorIndex];
                hostAndPort = authority.Substring(userInfoSeparatorIndex + 1);
            }

            var (host, port) = ParseHostAndPort(hostAndPort);
            parsedUrl = new ParsedDatabaseUrl
            {
                Scheme = scheme,
                UserInfo = userInfo,
                Host = host ?? string.Empty,
                Port = port,
                DatabaseName = Uri.UnescapeDataString(rawPath),
                Query = query,
            };
            return true;
        }

        private static (string? Host, int? Port) ParseHostAndPort(string hostAndPort)
        {
            if (string.IsNullOrWhiteSpace(hostAndPort))
            {
                return (string.Empty, null);
            }

            if (hostAndPort.StartsWith('['))
            {
                var closingBracketIndex = hostAndPort.IndexOf(']');
                if (closingBracketIndex <= 0)
                {
                    return (hostAndPort, null);
                }

                var host = hostAndPort[1..closingBracketIndex];
                if (closingBracketIndex == hostAndPort.Length - 1)
                {
                    return (host, null);
                }

                if (hostAndPort[closingBracketIndex + 1] != ':')
                {
                    return (host, null);
                }

                var portPart = hostAndPort.Substring(closingBracketIndex + 2);
                return int.TryParse(portPart, out var port) && port > 0 ? (host, port) : (host, null);
            }

            var lastColonIndex = hostAndPort.LastIndexOf(':');
            if (lastColonIndex <= 0 || lastColonIndex == hostAndPort.Length - 1 || hostAndPort.Count(c => c == ':') > 1)
            {
                return (hostAndPort, null);
            }

            var hostPart = hostAndPort[..lastColonIndex];
            var portCandidate = hostAndPort.Substring(lastColonIndex + 1);
            return int.TryParse(portCandidate, out var parsedPort) && parsedPort > 0
                ? (hostPart, parsedPort)
                : (hostAndPort, null);
        }

        private static (string? Username, string? Password) ParseUserInfo(string? userInfo)
        {
            if (string.IsNullOrWhiteSpace(userInfo))
            {
                return (null, null);
            }

            var separatorIndex = userInfo.IndexOf(':');
            if (separatorIndex < 0)
            {
                return (Uri.UnescapeDataString(userInfo), null);
            }

            var username = userInfo[..separatorIndex];
            var password = userInfo.Substring(separatorIndex + 1);
            return (Uri.UnescapeDataString(username), Uri.UnescapeDataString(password));
        }

        private static void ApplySupportedDatabaseUrlQueryParameters(
            NpgsqlConnectionStringBuilder builder,
            string queryString)
        {
            if (string.IsNullOrWhiteSpace(queryString))
            {
                return;
            }

            var query = queryString.StartsWith('?') ? queryString[1..] : queryString;
            if (string.IsNullOrWhiteSpace(query))
            {
                return;
            }

            foreach (var pair in query.Split('&', StringSplitOptions.RemoveEmptyEntries))
            {
                var separatorIndex = pair.IndexOf('=');
                if (separatorIndex <= 0 || separatorIndex == pair.Length - 1)
                {
                    throw new InvalidOperationException(
                        $"Invalid DATABASE_URL query parameter '{pair}'. Expected key=value format.");
                }

                var rawKey = pair[..separatorIndex];
                var rawValue = pair.Substring(separatorIndex + 1);
                var key = Uri.UnescapeDataString(rawKey);
                var value = Uri.UnescapeDataString(rawValue);

                switch (key.ToLowerInvariant())
                {
                    case "sslmode":
                        builder.SslMode = ParseSslMode(value);
                        break;
                    case "trust server certificate":
                    case "trust_server_certificate":
                    case "trustservercertificate":
                        // Npgsql 8+ treats this as obsolete/no-op, but keep validation for compatibility.
                        _ = ParseBoolean(value, key);
                        break;
                    case "pooling":
                        builder.Pooling = ParseBoolean(value, key);
                        break;
                    case "maximum pool size":
                    case "max pool size":
                    case "maxpoolsize":
                    case "pool_max_conns":
                        builder.MaxPoolSize = ParsePositiveInteger(value, key);
                        break;
                    case "minimum pool size":
                    case "min pool size":
                    case "minpoolsize":
                    case "pool_min_conns":
                        builder.MinPoolSize = ParseNonNegativeInteger(value, key);
                        break;
                    case "timeout":
                    case "connect_timeout":
                        builder.Timeout = ParsePositiveInteger(value, key);
                        break;
                    case "command timeout":
                    case "command_timeout":
                    case "commandtimeout":
                        builder.CommandTimeout = ParsePositiveInteger(value, key);
                        break;
                    case "keepalive":
                        builder.KeepAlive = ParseNonNegativeInteger(value, key);
                        break;
                    case "search_path":
                    case "search path":
                        builder.SearchPath = value;
                        break;
                    case "application_name":
                    case "application name":
                        builder.ApplicationName = value;
                        break;
                    case "host":
                        builder.Host = value;
                        break;
                    case "port":
                        builder.Port = ParsePositiveInteger(value, key);
                        break;
                    default:
                        throw new InvalidOperationException(
                            $"Unsupported DATABASE_URL query parameter '{key}'. Supported parameters: " +
                            "sslmode, trust_server_certificate, pooling, pool_max_conns, pool_min_conns, " +
                            "connect_timeout, command_timeout, keepalive, search_path, application_name, " +
                            "host, port.");
                }
            }
        }

        private static SslMode ParseSslMode(string value)
        {
            if (Enum.TryParse<SslMode>(value, true, out var sslMode))
            {
                return sslMode;
            }

            throw new InvalidOperationException(
                $"Invalid DATABASE_URL sslmode value '{value}'.");
        }

        private static bool ParseBoolean(string value, string key)
        {
            if (bool.TryParse(value, out var parsedValue))
            {
                return parsedValue;
            }

            throw new InvalidOperationException(
                $"Invalid DATABASE_URL value for '{key}': '{value}'. Expected 'true' or 'false'.");
        }

        private static int ParsePositiveInteger(string value, string key)
        {
            if (int.TryParse(value, out var parsedValue) && parsedValue > 0)
            {
                return parsedValue;
            }

            throw new InvalidOperationException(
                $"Invalid DATABASE_URL value for '{key}': '{value}'. Expected a positive integer.");
        }

        private static int ParseNonNegativeInteger(string value, string key)
        {
            if (int.TryParse(value, out var parsedValue) && parsedValue >= 0)
            {
                return parsedValue;
            }

            throw new InvalidOperationException(
                $"Invalid DATABASE_URL value for '{key}': '{value}'. Expected a non-negative integer.");
        }

        private sealed class ParsedDatabaseUrl
        {
            public string Scheme { get; init; } = string.Empty;

            public string? UserInfo { get; init; }

            public string Host { get; init; } = string.Empty;

            public int? Port { get; init; }

            public string DatabaseName { get; init; } = string.Empty;

            public string Query { get; init; } = string.Empty;
        }
    }
}
