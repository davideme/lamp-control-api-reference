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
            // Set content root to the project directory
            var projectDir = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "../../../../LampControlApi"));
            builder.UseContentRoot(projectDir);
        }
    }
}
