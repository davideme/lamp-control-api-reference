<?php

namespace OpenAPIServer\Api;

use OpenAPIServer\Api\AbstractDefaultApi;
use PHPUnit\Framework\TestCase;
use Slim\Exception\HttpNotImplementedException;
use Slim\Psr7\Factory\StreamFactory;
use Slim\Psr7\Response;
use Slim\Psr7\Headers;
use Slim\Psr7\Request as SlimRequest;
use Slim\Psr7\Uri;

/**
 * AbstractDefaultApiTest Class Doc Comment
 *
 * @package OpenAPIServer\Api
 * @author  OpenAPI Generator team
 * @link    https://github.com/openapitools/openapi-generator
 *
 * @coversDefaultClass \OpenAPIServer\Api\AbstractDefaultApi
 */
class AbstractDefaultApiTest extends TestCase
{
    private AbstractDefaultApi $api;

    private function createJsonRequest(string $method, string $path, array $body = null): SlimRequest
    {
        $uri = new Uri('', '', 80, $path);
        $headers = new Headers([
            'Content-Type' => ['application/json'],
            'Accept' => ['application/json'],
        ]);
        $cookies = [];
        $serverParams = [];
        $streamFactory = new StreamFactory();
        $stream = $streamFactory->createStream($body ? (json_encode($body) ?: '{}') : '');
        return new SlimRequest($method, $uri, $headers, $cookies, $serverParams, $stream);
    }

    public function setUp(): void
    {
        parent::setUp();
        // Create a concrete implementation for testing
        $this->api = new class extends AbstractDefaultApi {
            // Concrete class for testing - no overrides so it uses parent methods
        };
    }

    /**
     * Test that createLamp throws HttpNotImplementedException
     *
     * @covers ::createLamp
     */
    public function testCreateLampThrowsException(): void
    {
        $request = $this->createJsonRequest('POST', '/lamps', ['status' => true]);
        $response = new Response();
        
        $this->expectException(HttpNotImplementedException::class);
        $this->expectExceptionMessage('How about implementing createLamp as a POST method');
        
        $this->api->createLamp($request, $response);
    }

    /**
     * Test that deleteLamp throws HttpNotImplementedException
     *
     * @covers ::deleteLamp
     */
    public function testDeleteLampThrowsException(): void
    {
        $request = $this->createJsonRequest('DELETE', '/lamps/123');
        $response = new Response();
        
        $this->expectException(HttpNotImplementedException::class);
        $this->expectExceptionMessage('How about implementing deleteLamp as a DELETE method');
        
        $this->api->deleteLamp($request, $response, '123');
    }

    /**
     * Test that getLamp throws HttpNotImplementedException
     *
     * @covers ::getLamp
     */
    public function testGetLampThrowsException(): void
    {
        $request = $this->createJsonRequest('GET', '/lamps/123');
        $response = new Response();
        
        $this->expectException(HttpNotImplementedException::class);
        $this->expectExceptionMessage('How about implementing getLamp as a GET method');
        
        $this->api->getLamp($request, $response, '123');
    }

    /**
     * Test that listLamps throws HttpNotImplementedException
     *
     * @covers ::listLamps
     */
    public function testListLampsThrowsException(): void
    {
        $request = $this->createJsonRequest('GET', '/lamps');
        $response = new Response();
        
        $this->expectException(HttpNotImplementedException::class);
        $this->expectExceptionMessage('How about implementing listLamps as a GET method');
        
        $this->api->listLamps($request, $response);
    }

    /**
     * Test that updateLamp throws HttpNotImplementedException
     *
     * @covers ::updateLamp
     */
    public function testUpdateLampThrowsException(): void
    {
        $request = $this->createJsonRequest('PUT', '/lamps/123', ['status' => false]);
        $response = new Response();
        
        $this->expectException(HttpNotImplementedException::class);
        $this->expectExceptionMessage('How about implementing updateLamp as a PUT method');
        
        $this->api->updateLamp($request, $response, '123');
    }
}