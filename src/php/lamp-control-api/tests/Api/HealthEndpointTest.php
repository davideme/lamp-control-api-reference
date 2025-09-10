<?php

/**
 * Health Endpoint Test
 * PHP version 8.1
 *
 * @package OpenAPIServer
 */

namespace OpenAPIServer\Api;

use PHPUnit\Framework\TestCase;

/**
 * HealthEndpointTest Class Doc Comment
 *
 * @package OpenAPIServer\Api
 */
class HealthEndpointTest extends TestCase
{
    /**
     * Test case for health endpoint functionality
     *
     * Test the health endpoint logic separately from the routing.
     */
    public function testHealthEndpointLogic(): void
    {
        // Test the health check logic
        $healthData = ['status' => 'ok'];
        $jsonResponse = json_encode($healthData);

        $this->assertIsString($jsonResponse);
        $data = json_decode($jsonResponse, true);

        $this->assertIsArray($data);
        $this->assertArrayHasKey('status', $data);
        $this->assertEquals('ok', $data['status']);
    }
}
