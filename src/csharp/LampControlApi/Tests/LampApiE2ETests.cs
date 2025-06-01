using System.Net;
using System.Net.Http.Json;
using LampControlApi.Controllers;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LampControlApi.E2E
{
    [TestClass]
    public class LampApiE2ETests
    {
        private static WebApplicationFactory<Program> _factory = null!;
        private static HttpClient _client = null!;

        /// <summary>
        /// Initializes the test class.
        /// </summary>
        /// <param name="context">Test context.</param>
        [ClassInitialize]
        public static void ClassInit(TestContext context)
        {
            _factory = new WebApplicationFactory<Program>();
            _client = _factory.CreateClient();
        }

        /// <summary>
        /// Cleans up the test class.
        /// </summary>
        [ClassCleanup]
        public static void ClassCleanup()
        {
            _client.Dispose();
            _factory.Dispose();
        }

        [TestMethod]
        public async Task GetLamps_ReturnsSuccessAndList()
        {
            var response = await _client.GetAsync("/v1/lamps");
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
            var lamps = await response.Content.ReadFromJsonAsync<List<Lamp>>();
            Assert.IsNotNull(lamps);
        }

        [TestMethod]
        public async Task CreateAndGetLamp_WorksCorrectly()
        {
            var create = new LampCreate { Status = true };
            var createResp = await _client.PostAsJsonAsync("/v1/lamps", create);
            Assert.AreEqual(HttpStatusCode.OK, createResp.StatusCode);
            var created = await createResp.Content.ReadFromJsonAsync<Lamp>();
            Assert.IsNotNull(created);
            Assert.IsTrue(created.Status);

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
            Assert.AreEqual(HttpStatusCode.OK, delResp.StatusCode);
            var getResp = await _client.GetAsync($"/v1/lamps/{created.Id}");
            Assert.AreEqual(HttpStatusCode.NotFound, getResp.StatusCode);
        }
    }
}
