using System.Net;
using System.Net.Http.Json;
using System.Threading.Tasks;
using LampControlApi.Controllers;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LampControlApi.E2E
{
    [TestClass]
    public class LampApiE2ETests
    {
        private static HttpClient _client = null!;

        /// <summary>
        /// Initializes the test class.
        /// </summary>
        /// <param name="context">Test context.</param>
        [ClassInitialize]
        public static void ClassInit(TestContext context)
        {
            // Ensure current directory is the project directory so the test host
            // can locate content/solution-relative files during SetContentRoot.
            var projectDir = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..", "..", ".."));
            System.IO.Directory.SetCurrentDirectory(projectDir);

            // Configure a factory instance and set the content root explicitly
            var factory = new Microsoft.AspNetCore.Mvc.Testing.WebApplicationFactory<Program>()
                .WithWebHostBuilder(builder => builder.UseContentRoot(projectDir));

            _client = factory.CreateClient();
        }

        /// <summary>
        /// Cleans up the test class.
        /// </summary>
        [ClassCleanup]
        public static void ClassCleanup()
        {
            _client.Dispose();
        }

        [TestMethod]
        public async Task GetLamps_ReturnsSuccessAndList()
        {
            var response = await _client.GetAsync("/v1/lamps");
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
            var resp = await response.Content.ReadFromJsonAsync<Response>();
            Assert.IsNotNull(resp);
            Assert.IsNotNull(resp.Data);
        }

        [TestMethod]
        public async Task CreateAndGetLamp_WorksCorrectly()
        {
            var create = new LampCreate { Status = true };
            var createResp = await _client.PostAsJsonAsync("/v1/lamps", create);
            Assert.AreEqual(HttpStatusCode.Created, createResp.StatusCode);
            var created = await createResp.Content.ReadFromJsonAsync<Lamp>();
            Assert.IsNotNull(created);
            Assert.IsTrue(created.Status);

            // Ensure Location header is present and points to the created resource
            Assert.IsTrue(createResp.Headers.Location != null);

            var getResp = await _client.GetAsync($"/v1/lamps/{created.Id}");
            Assert.AreEqual(HttpStatusCode.OK, getResp.StatusCode);
            var lamp = await getResp.Content.ReadFromJsonAsync<Lamp>();
            Assert.IsNotNull(lamp);
            Assert.AreEqual(created.Id, lamp.Id);
        }

        [TestMethod]
        public async Task UpdateLamp_ChangesStatus()
        {
            var create = new LampCreate { Status = false };
            var createResp = await _client.PostAsJsonAsync("/v1/lamps", create);
            var created = await createResp.Content.ReadFromJsonAsync<Lamp>();
            var update = new LampUpdate { Status = true };
            var updateResp = await _client.PutAsJsonAsync($"/v1/lamps/{created.Id}", update);
            Assert.AreEqual(HttpStatusCode.OK, updateResp.StatusCode);
            var updated = await updateResp.Content.ReadFromJsonAsync<Lamp>();
            Assert.IsTrue(updated.Status);
        }

        [TestMethod]
        public async Task DeleteLamp_RemovesLamp()
        {
            var create = new LampCreate { Status = true };
            var createResp = await _client.PostAsJsonAsync("/v1/lamps", create);
            var created = await createResp.Content.ReadFromJsonAsync<Lamp>();
            var delResp = await _client.DeleteAsync($"/v1/lamps/{created.Id}");
            Assert.AreEqual(HttpStatusCode.NoContent, delResp.StatusCode);
            var getResp = await _client.GetAsync($"/v1/lamps/{created.Id}");
            Assert.AreEqual(HttpStatusCode.NotFound, getResp.StatusCode);
        }

        [TestMethod]
        public async Task HealthEndpoint_ReturnsOkWithStatus()
        {
            var response = await _client.GetAsync("/health");
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);

            var healthResponse = await response.Content.ReadFromJsonAsync<object>();
            Assert.IsNotNull(healthResponse);

            // Check that the response contains status: "ok"
            var responseText = await response.Content.ReadAsStringAsync();
            Assert.IsTrue(responseText.Contains("\"status\":\"ok\""));
        }
    }
}
