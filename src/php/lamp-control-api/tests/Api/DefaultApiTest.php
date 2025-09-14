<?php

declare(strict_types=1);

namespace Tests\Api;

use OpenAPIServer\Api\DefaultApi;
use PHPUnit\Framework\TestCase;
use Slim\Psr7\Factory\ServerRequestFactory;
use Slim\Psr7\Factory\StreamFactory;
use Slim\Psr7\Response;

class DefaultApiTest extends TestCase
{
    private DefaultApi $api;
    /**
     * Shared LampRepository for all tests
     */
    private static ?\OpenAPIServer\Repository\LampRepository $sharedRepo = null;

    protected function setUp(): void
    {
        if (self::$sharedRepo === null) {
            self::$sharedRepo = new \OpenAPIServer\Repository\LampRepository();
        }
        $this->api = new DefaultApi(self::$sharedRepo);
    }

    private function jsonRequest(string $method, string $body = '{}'): \Psr\Http\Message\ServerRequestInterface
    {
        $factory = new ServerRequestFactory();
        $streamFactory = new StreamFactory();
        $request = $factory->createServerRequest($method, '/v1/lamps');
        return $request->withBody($streamFactory->createStream($body))
            ->withHeader('Content-Type', 'application/json');
    }

    public function testCreateLampRejectsInvalidJson(): void
    {
        $req = $this->jsonRequest('POST', '{invalid');
        $res = new Response();
        $out = $this->api->createLamp($req, $res);
        $this->assertSame(400, $out->getStatusCode());
    }

    public function testCreateLampRejectsEmptyBody(): void
    {
        $req = $this->jsonRequest('POST', ''); // empty body -> null decode
        $res = new Response();
        $out = $this->api->createLamp($req, $res);
        $this->assertSame(400, $out->getStatusCode());
    }

    public function testCreateLampRejectsArrayBody(): void
    {
        $req = $this->jsonRequest('POST', '[true, false]');
        $res = new Response();
        $out = $this->api->createLamp($req, $res);
        $this->assertSame(400, $out->getStatusCode());
    }

    public function testCreateLampMissingStatus(): void
    {
        $req = $this->jsonRequest('POST', '{}');
        $res = new Response();
        $out = $this->api->createLamp($req, $res);
        $this->assertSame(400, $out->getStatusCode());
    }

    public function testCreateLampInvalidStatusType(): void
    {
        $req = $this->jsonRequest('POST', '{"status":"on"}');
        $res = new Response();
        $out = $this->api->createLamp($req, $res);
        $this->assertSame(400, $out->getStatusCode());
    }

    public function testCreateLampSuccess(): array
    {
        $req = $this->jsonRequest('POST', '{"status":true}');
        $res = new Response();
        $out = $this->api->createLamp($req, $res);
        $this->assertSame(201, $out->getStatusCode());
        $payload = json_decode((string)$out->getBody(), true);
        $this->assertIsArray($payload);
        $this->assertArrayHasKey('id', $payload);
        $this->assertArrayHasKey('status', $payload);
        return $payload; // provide id for dependent tests
    }

    /**
     * @depends testCreateLampSuccess
     */
    public function testGetLampFound(array $created): void
    {
        $res = new Response();
        $reqFactory = new ServerRequestFactory();
        $req = $reqFactory->createServerRequest('GET', '/v1/lamps/' . $created['id']);
        $out = $this->api->getLamp($req, $res, $created['id']);
        $this->assertSame(200, $out->getStatusCode());
    }

    public function testGetLampNotFound(): void
    {
        $res = new Response();
        $reqFactory = new ServerRequestFactory();
        $req = $reqFactory->createServerRequest('GET', '/v1/lamps/nonexistent');
        $out = $this->api->getLamp($req, $res, 'nonexistent');
        $this->assertSame(404, $out->getStatusCode());
    }

    /**
     * @depends testCreateLampSuccess
     */
    public function testListLamps(array $created): void
    {
        $res = new Response();
        $reqFactory = new ServerRequestFactory();
        $req = $reqFactory->createServerRequest('GET', '/v1/lamps');
        $out = $this->api->listLamps($req, $res);
        $this->assertSame(200, $out->getStatusCode());
        $body = json_decode((string)$out->getBody(), true);
        $this->assertArrayHasKey('data', $body);
        $this->assertNotEmpty($body['data']);
    }

    public function testUpdateLampValidationErrors(): void
    {
        $res = new Response();
        $reqFactory = new ServerRequestFactory();
        // invalid JSON
        $req = $this->jsonRequest('PATCH', '{invalid');
        $out = $this->api->updateLamp($req, $res, 'x');
        $this->assertSame(400, $out->getStatusCode());

        // array body
        $res = new Response();
        $req = $this->jsonRequest('PATCH', '[]');
        $out = $this->api->updateLamp($req, $res, 'x');
        $this->assertSame(400, $out->getStatusCode());

        // missing field
        $res = new Response();
        $req = $this->jsonRequest('PATCH', '{}');
        $out = $this->api->updateLamp($req, $res, 'x');
        $this->assertSame(400, $out->getStatusCode());

        // wrong type
        $res = new Response();
        $req = $this->jsonRequest('PATCH', '{"status":"on"}');
        $out = $this->api->updateLamp($req, $res, 'x');
        $this->assertSame(400, $out->getStatusCode());
    }

    /**
     * @depends testCreateLampSuccess
     */
    public function testUpdateLampNotFound(array $created): void
    {
        $res = new Response();
        $req = $this->jsonRequest('PATCH', '{"status":false}');
        $out = $this->api->updateLamp($req, $res, $created['id'] . 'missing');
        $this->assertSame(404, $out->getStatusCode());
    }

    /**
     * @depends testCreateLampSuccess
     */
    public function testUpdateLampSuccess(array $created): void
    {
        $res = new Response();
        $req = $this->jsonRequest('PATCH', '{"status":false}');
        $out = $this->api->updateLamp($req, $res, $created['id']);
        $this->assertSame(200, $out->getStatusCode());
        $payload = json_decode((string)$out->getBody(), true);
        $this->assertFalse($payload['status']);
    }

    /**
     * @depends testCreateLampSuccess
     */
    public function testDeleteLampNotFound(array $created): void
    {
        $res = new Response();
        $reqFactory = new ServerRequestFactory();
        $req = $reqFactory->createServerRequest('DELETE', '/v1/lamps/' . $created['id'] . 'missing');
        $out = $this->api->deleteLamp($req, $res, $created['id'] . 'missing');
        $this->assertSame(404, $out->getStatusCode());
    }

    /**
     * @depends testCreateLampSuccess
     */
    public function testDeleteLampSuccess(array $created): void
    {
        $res = new Response();
        $reqFactory = new ServerRequestFactory();
        $req = $reqFactory->createServerRequest('DELETE', '/v1/lamps/' . $created['id']);
        $out = $this->api->deleteLamp($req, $res, $created['id']);
        $this->assertSame(204, $out->getStatusCode());
    }
}
