using System;
using System.IO;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;

namespace LampControlApi.E2E
{
    /// <summary>
    /// Custom WebApplicationFactory for E2E tests.
    /// </summary>
    public class CustomWebApplicationFactory : WebApplicationFactory<Program>
    {
        protected override void ConfigureWebHost(IWebHostBuilder builder)
        {
            // Resolve the project directory from the runtime base path (bin/Debug/netX)
            var projectDir = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..", "..", ".."));

            // Ensure the process current directory is the project directory so
            // any solution-relative lookup by the test host will resolve correctly.
            Directory.SetCurrentDirectory(projectDir);

            // Set the content root explicitly to the project directory.
            builder.UseContentRoot(projectDir);
        }
    }
}
